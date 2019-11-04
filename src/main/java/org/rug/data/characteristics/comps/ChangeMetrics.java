package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.rug.data.characteristics.IComponentCharacteristic;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.GitVersion;
import org.rug.data.project.IVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Calculates metrics related to file changes:
 * - PCCC: Percentage of Commits a Class has Changed
 * - PCPC: Percentage of Commits a Package has Changed
 * - TACH: Total Amount of Changes
 * - FRCH: Frequency of Changes
 * - CHO: Change has Occurred
 */
public class ChangeMetrics extends AbstractComponentCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(ChangeMetrics.class);
    /**
     * The similarity measured as a percentage of the bytes between two files to count them as a rename.
     * Default value used by git is 60.
     */
    protected static final int RENAME_SCORE = 50;
    /**
     * The maximum number of files to compare within a rename to not reduce performance.
     * Default value used by git is 1000.
     */
    protected static final int RENAME_LIMIT = 500;


    private SourceCodeRetriever retriever;
    private Map<String, Long> changeHistory;
    private GitVersion previousVersion;
    private GitVersion currentVersion;

    private DiffFormatter diffFormatter;
    private List<DiffEntry> entries;
    private List<IComponentCharacteristic> changeMetrics;

    public ChangeMetrics() {
        super("freqOfChanges",
                VertexLabel.allFiles(),
                EnumSet.noneOf(EdgeLabel.class));
        this.changeHistory = new HashMap<>(1000);
        this.changeMetrics = new ArrayList<>();
        this.changeMetrics.add(new PCCCMetric(this.name));
        this.changeMetrics.add(new CHOMetricPackage());
        this.changeMetrics.add(new PCPCMetric());
        this.changeMetrics.add(new TACHMetricPackage());
    }

    /**
     * Triggers the calculation of this characteristic given that the given version is
     * a {@link GitVersion} object.
     * @param version the version from which to retrieve the components (must be of type {@link GitVersion}).
     */
    @Override
    public void calculate(IVersion version) {
        if (version instanceof GitVersion) {
            currentVersion = (GitVersion)version;
            retriever = version.getSourceCodeRetriever();
            if (previousVersion != null) {
                initDiff(currentVersion.getRepository(),
                        previousVersion.getCommitObjectId(),
                        currentVersion.getCommitObjectId());
                super.calculate(version);
                this.changeMetrics.forEach(m -> m.calculate(version));
            }
            previousVersion = currentVersion;
        }
    }

    @Override
    protected void calculate(Vertex vertex) {
        var pathFile = retriever.relativize(retriever.getPathOf(vertex));
        if (pathFile.isEmpty()){
            vertex.property(this.name, 0d);
            return;
        }
        var pathFileStr = pathFile.get().toString();
        var changeOpt = getDiffOf(pathFileStr);

        String key;
        if (changeOpt.isPresent()){
            var change = changeOpt.get();
            Long oldValue;
            key = String.format("%s#%s", change.getNewPath(), vertex.value("name"));
            var hasChanged = false;
            switch (change.getChangeType()) {
                case ADD:
                case MODIFY:
                    hasChanged = true;
                    oldValue = changeHistory.getOrDefault(key, 0L);
                    changeHistory.put(key, oldValue + 1);
                    break;
                case COPY:
                case RENAME:
                    hasChanged = true;
                    oldValue = changeHistory.remove(String.format("%s#%s", change.getOldPath(), vertex.value("name")));
                    oldValue = oldValue == null ? 0L : oldValue;
                    changeHistory.put(key, oldValue + 1);
                    break;
                case DELETE:
                default:
                    break;
            }
            vertex.property(TACHMetricPackage.NAME, countTotalAmountOfChanges(change));
            vertex.property(CHOMetricPackage.NAME, hasChanged);
        }else {
            key = String.format("%s#%s", pathFileStr, vertex.value("name"));
        }
        vertex.property(this.name, changeHistory.getOrDefault(key, 0L));
    }

    @Override
    protected void calculate(Edge edge) {

    }

    /**
     * Computes the list of DiffEntry that contains all the differences between the 2 commits.
     * Note that this implementation only returns the differences between the two given commit, ignoring any
     * commit in between.
     * @param repo The repository in which the commits are.
     * @param parent The parent commit to which needs to be compared.
     * @param child The child commit that needs to be compared to the parent commit.
     */
    private void initDiff(Repository repo, ObjectId parent, ObjectId child) {
        if (diffFormatter == null) {
            diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repo);
            diffFormatter.setDetectRenames(true);
        }
        var renameDetector = diffFormatter.getRenameDetector();
        renameDetector.setRenameScore(RENAME_SCORE);
        renameDetector.setRenameLimit(RENAME_LIMIT);
        try {
            entries = diffFormatter.scan(parent, child);
        } catch (IOException e) {
            logger.error("Could not perform diff between parent commit {} and child {}.", parent.getName(), child.getName());
            entries = new ArrayList<>();
        }
        diffFormatter.close();
    }

    private long countTotalAmountOfChanges(DiffEntry entry){
        int linesDeleted = 0, linesAdded = 0, linesModified = 0;
        try {
            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
            for (Edit edit : fileHeader.toEditList()) {
                switch (edit.getType()){
                    case INSERT:
                        linesAdded += edit.getEndB() - edit.getBeginB();
                        break;
                    case DELETE:
                        linesDeleted += edit.getEndA() - edit.getBeginA();
                        break;
                    case REPLACE:
                        linesModified += edit.getEndA() - edit.getBeginA();
                        break;
                    case EMPTY:
                        break;
                }
            }
        } catch (IOException e) {
            logger.error("Cannot convert to file header: {}", entry.getNewPath());
        }
        return linesAdded + linesDeleted + 2 * linesModified;
    }

    /**
     * Looks in the diff entry of between the current commits to find the given path.
     * @param pathSuffix the path to use as a suffix.
     * @return an optional containing a DiffEntry if any path was matched.
     */
    private Optional<DiffEntry> getDiffOf(String pathSuffix){
        return entries.stream().filter(e -> e.getNewPath().endsWith(pathSuffix)).findFirst();
    }
}
