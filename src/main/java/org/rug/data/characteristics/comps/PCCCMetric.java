package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
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
    private GitVersion previousVersion;
    private GitVersion currentVersion;
    private long totalCommits = 1;

    public PCCCMetric() {
        super("percCommClassChanged",
                VertexLabel.allFiles(),
                EnumSet.noneOf(EdgeLabel.class));
        this.changeHistory = new HashMap<>(100);
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
            totalCommits++;
            previousVersion = currentVersion;
        }
    }

    @Override
    protected void calculate(Vertex vertex) {
        var pathFile = retriever.getPathOf(vertex); // edu.rug.pyne.api.parser.Parser has been added after first commit
        if (pathFile.isEmpty()){
            vertex.property(this.name, 0d);
            return;
        }
        var pathFileStr = pathFile.get().toString();
        var change = getDifference(currentVersion.getRepository(),
                previousVersion.getCommitObjectId(),
                currentVersion.getCommitObjectId(),
                pathFileStr);

        String key = String.format("%s#%s", pathFileStr, vertex.value("name"));
        if (change != null){
            long oldValue;
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
                    changeHistory.put(key, oldValue + 1);
                    break;
                case DELETE:
                default:
                    break;
            }
        }
        vertex.property(this.name, (changeHistory.getOrDefault(key, 0L) * 100d) / totalCommits);
    }

    @Override
    protected void calculate(Edge edge) {

    }

    /**
     * Returns a List of DiffEntry that contains all the differences between the 2 commits for the given file.
     * Note that this implementation only returns the differences between the two given commit, ignoring any
     * commit in between.
     * @param repo The repository in which the commits are.
     * @param parent The parent commit to which needs to be compared.
     * @param child The child commit that needs to be compared to the parent commit.
     * @param fileSuffixFilter The string that will be used to filter the files in the commits as a suffix.
     * @return a list of DiffEntries which contains the differences of the given file
     */
    private DiffEntry getDifference(Repository repo, ObjectId parent, ObjectId child, String fileSuffixFilter) {
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setDetectRenames(true);
        diffFormatter.setPathFilter(PathSuffixFilter.create(fileSuffixFilter.substring(fileSuffixFilter.lastIndexOf("/"))));
        List<DiffEntry> entries = new ArrayList<>();
        try {
            entries = diffFormatter.scan(parent, child);
        } catch (IOException e) {
            logger.error("Could not perform diff between parent commit {} and child {}.", parent.getName(), child.getName());
        }
        diffFormatter.close(); //The reason why we do not detect a all the changes for the parser is because the suffix
                                // will filter moved files as used at the moment, so better not use it at all
                               // and then parse all the changes for that file or use only the last one (or whatever)
        return entries.isEmpty() ? null : entries.get(0);
    }

}
