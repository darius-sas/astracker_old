package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.GitVersion;
import org.rug.data.project.IVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Characteristics that calculates the metric PCCC (Percentage of Commits a Class has Changed).
 * This class also automatically calculates the PCPC (Percentage of Commits a Package has Changed) metric.
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PCCCMetric.class);
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

    public static final String PCCCMetricName = "percCommClassChanged";

    private SourceCodeRetriever retriever;
    private Map<String, Long> changeHistory;
    private GitVersion previousVersion;
    private GitVersion currentVersion;
    private long totalCommits = 1;
    private List<DiffEntry> entries;
    private PCPCMetric pcpcMetric;

    public PCCCMetric() {
        super(PCCCMetricName,
                VertexLabel.allFiles(),
                EnumSet.noneOf(EdgeLabel.class));
        this.changeHistory = new HashMap<>(100);
        this.pcpcMetric = new PCPCMetric(this.name);
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
                pcpcMetric.calculate(version);
            }
            totalCommits++;
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
            switch (change.getChangeType()) {
                case ADD:
                case MODIFY:
                    oldValue = changeHistory.getOrDefault(key, 0L);
                    changeHistory.put(key, oldValue + 1);
                    break;
                case COPY:
                case RENAME:
                    oldValue = changeHistory.remove(String.format("%s#%s", change.getOldPath(), vertex.value("name")));
                    oldValue = oldValue == null ? 0L : oldValue;
                    changeHistory.put(key, oldValue + 1);
                    break;
                case DELETE:
                default:
                    break;
            }
        }else {
            key = String.format("%s#%s", pathFileStr, vertex.value("name"));
        }
        vertex.property(this.name, (changeHistory.getOrDefault(key, 0L) * 100d) / totalCommits);
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
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setDetectRenames(true);
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

    /**
     * Looks in the diff entry of between the current commits to find the given path.
     * @param pathSuffix the path to use as a suffix.
     * @return an optional containing a DiffEntry if any path was matched.
     */
    private Optional<DiffEntry> getDiffOf(String pathSuffix){
        return entries.stream().filter(e -> e.getNewPath().endsWith(pathSuffix)).findFirst();
    }
}
