package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.GitVersion;
import org.rug.data.project.IVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Characteristics that calculates the metric PCCC (Percentage of Commits a Class has Changed)
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PCCCMetric.class);

    private SourceCodeRetriever retriever;
    private Map<String, Long> changeHistory;
    private GitVersion previousVersion;
    private GitVersion currentVersion;

    public PCCCMetric() {
        super("pccc",
                VertexLabel.allFiles(),
                EnumSet.noneOf(EdgeLabel.class));
        this.changeHistory = new HashMap<>();
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

            super.calculate(version);
            previousVersion = currentVersion;
        }
    }

    @Override
    protected void calculate(Vertex vertex) {
        // the path starts from the src directory indicated by the user (sourcePath of SourceCodeRetriever).
        var pathFile = retriever.getPathOf(vertex);
        if (pathFile.isEmpty() || !pathFile.get().toFile().exists()){
            return;
        }
        var pathFileStr = pathFile.get().toString();
        var change = getDifference(currentVersion.getRepository(),
                previousVersion.getCommitObjectId(),
                currentVersion.getCommitObjectId(),
                pathFileStr);

        if (change == null){return;}

        switch (change.getChangeType()){
            case ADD:
            case MODIFY:
                changeHistory.merge(pathFileStr, 1L, Long::sum);
                break;
            case COPY:
            case RENAME:
                // Handle key switching
            case DELETE:
            default:
                break;
        }
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

    /**
     * Checks if a file has changed from previous commit.
     * @param file the file name to check if it changed
     * @return true if it changed, false otherwise
     */
    private boolean didFileChange(Path file) {
        for(DiffEntry diffEntry : diffEntries) {
            if(file.endsWith(diffEntry.getOldPath()) &&
                    diffEntry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                return true;
            }
        }
        return false;
    }

}
