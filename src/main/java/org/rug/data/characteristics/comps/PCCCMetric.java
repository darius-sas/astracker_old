package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.GitVersion;
import org.rug.data.project.IVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Characteristics that calculates the metric PCCC (Percentage of Commits a Class has Changed)
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    private SourceCodeRetriever retriever;
    private Map<GitVersion, Map<String, Long>> changeHistory;
    private GitVersion previousVersion;
    private GitVersion currentVersion;
    private List<DiffEntry> diffEntries;

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
            diffEntries = getDifference(previousVersion.getRepository(),
                    previousVersion.getCommitObjectId(),
                    currentVersion.getCommitObjectId(),
                    ".java");
            super.calculate(version);
            previousVersion = currentVersion;
        }
    }

    @Override
    protected void calculate(Vertex vertex) {
        var pathFile = retriever.getPathOf(vertex);
        if (pathFile.isEmpty() || !pathFile.get().toFile().exists()){
            return;
        }
        var fileHasChanged = didFileChange(pathFile.get());

        if (fileHasChanged){
            
        }
    }

    @Override
    protected void calculate(Edge edge) {

    }

    /**
     * Returns a List of DiffEntry that contains all the differences between the 2 commits.
     * @param repo The repository in which the commits are.
     * @param parent The parent commit to which needs to be compared.
     * @param child The child commit that needs to be compared to the parent commit.
     * @param suffixFilter The string that will be used to filter the files in the commits as a suffix. E.g. ".java" to filter .java files.
     * @return a list of DiffEntries which contains the differences
     */
    private List<DiffEntry> getDifference(Repository repo, ObjectId parent, ObjectId child, String suffixFilter) {
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setPathFilter(PathSuffixFilter.create(suffixFilter));
        List<DiffEntry> entries = null;
        try {
            entries = diffFormatter.scan(parent, child);
        } catch (IOException e) {
            e.printStackTrace();
        }
        diffFormatter.close();
        return entries;
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
