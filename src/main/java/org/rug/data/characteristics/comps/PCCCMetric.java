package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
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
 * Characteristics that calculates the metric PCCC (Percentage of Commits a Class has Changed)
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PCCCMetric.class);

    private SourceCodeRetriever retriever;
    private Map<String, Long> changeHistory;
    private Set<String> updateStatus;
    private GitVersion previousVersion;
    private GitVersion currentVersion;
    private long totalCommits = 1;

    public PCCCMetric() {
        super("percCommClassChanged",
                VertexLabel.allFiles(),
                EnumSet.noneOf(EdgeLabel.class));
        this.changeHistory = new HashMap<>(100);
        this.updateStatus = new HashSet<>();
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
                super.calculate(version);
            }
            this.updateStatus.clear();
            totalCommits++;
            previousVersion = currentVersion;
        }
    }

    @Override
    protected void calculate(Vertex vertex) {
        // the path starts from the src directory indicated by the user (sourcePath of SourceCodeRetriever).
        var pathFile = retriever.getPathOf(vertex);
        if (pathFile.isEmpty() || updateStatus.contains(pathFile.get().toString())){
            return;
        }
        var pathFileStr = pathFile.get().toString();
        var change = getDifference(currentVersion.getRepository(),
                previousVersion.getCommitObjectId(),
                currentVersion.getCommitObjectId(),
                pathFileStr);

        if (change == null){
            logger.info("Could not retrieve changes for: {}", pathFileStr);
            return;
        }else {
            switch (change.getChangeType()) {
                case ADD:
                case MODIFY:
                    changeHistory.merge(change.getNewPath(), 1L, Long::sum);
                    break;
                case COPY:
                case RENAME:
                    var oldValue = changeHistory.remove(change.getOldPath());
                    changeHistory.put(change.getNewPath(), oldValue + 1);
                    break;
                case DELETE:
                default:
                    break;
            }
            updateStatus.add(pathFile.get().toString());
        }
        vertex.property(this.name, (changeHistory.getOrDefault(change.getNewPath(), 0L) * 100d) / totalCommits);
    }

    @Override
    protected void calculate(Edge edge) {

    }

    /**
     * Returns a List of DiffEntry that contains all the differences between the 2 commits for the given file.
     * @param repo The repository in which the commits are.
     * @param parent The parent commit to which needs to be compared.
     * @param child The child commit that needs to be compared to the parent commit.
     * @param fileSuffixFilter The string that will be used to filter the files in the commits as a suffix.
     * @return a list of DiffEntries which contains the differences of the given file
     */
    private DiffEntry getDifference(Repository repo, ObjectId parent, ObjectId child, String fileSuffixFilter) {
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setPathFilter(PathSuffixFilter.create(fileSuffixFilter));
        List<DiffEntry> entries = new ArrayList<>();
        try {
            entries = diffFormatter.scan(parent, child);
        } catch (IOException e) {
            logger.error("Could not perform diff between parent commit {} and child {}.", parent.getName(), child.getName());
        }
        diffFormatter.close();
        return entries.isEmpty() ? null : entries.get(0);
    }

}
