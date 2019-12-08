package org.rug.data.project;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.characteristics.comps.SourceCodeRetriever;
import org.rug.data.labels.EdgeLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractVersion implements IVersion {

    private final static Logger logger = LoggerFactory.getLogger(Version.class);
    protected String versionDate;

    private String versionString;
    protected long versionIndex;
    private transient Path sourcePath;
    private transient Path graphMLPath;
    protected transient Graph graph;
    private SourceCodeRetriever sourceCodeRetrieval;

    /**
     * Partially builds this version by setting the version string starting from the given path.
     * @param path the path to parse the version string from.
     * @param sourceCodeRetrieval the source code retriever object.
     */
    public AbstractVersion(Path path, SourceCodeRetriever sourceCodeRetrieval){
        this.versionString = parseVersionString(path);
        this.sourceCodeRetrieval = sourceCodeRetrieval;
    }

    /**
     * Retrieve the versionString string.
     * @return a string representing the versionString.
     */
    public String getVersionString() {
        return versionString;
    }

    /**
     * Set the versionString string of this versionString.
     * @param versionString the new versionString string.
     */
    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    /**
     * The position order of this versionString in the overall system.
     * Namely, assuming that v0.3 is the first versionString analysed of the system the
     * versionString position is 1. If the second versionString analysed is v0.5, then
     * the versionString position is 2, etc.
     * @return a long representing the order of this versionString.
     */
    public long getVersionIndex() {
        return versionIndex;
    }

    /**
     * Set the order position of this versionString.
     * @param versionIndex The versionString position.
     */
    public void setVersionIndex(long versionIndex) {
        this.versionIndex = versionIndex;
    }

    /**
     * Set the path to the source code folder or file.
     * @param p the path to the source code.
     */
    public void setSourceCodePath(Path p) {
        this.sourcePath = p;
    }

    public Path getGraphMLPath() {
        return graphMLPath;
    }

    public void setGraphMLPath(Path graphMLPath) {
        this.graphMLPath = graphMLPath;
    }

    @Override
    public Path getSourceCodePath() {
        return sourcePath;
    }

    /**
     * Lazily loads the Graph from the given GraphML file at the first invocation.
     * The Graph is then cached for future access.
     * @return the dependency graph of this version.
     */
    @Override
    public synchronized Graph getGraph() {
        if (graph == null) {
            graph = TinkerGraph.open();
            try {
                var graphMLfile = graphMLPath.toFile();
                if (graphMLfile.isFile() && graphMLfile.canRead()) {
                    this.graph.traversal().io(graphMLPath.toAbsolutePath().toString())
                            .read().with(IO.reader, IO.graphml).iterate();
                    ensureCompatibility();
                }else {
                    throw new IOException("");
                }
            } catch (IOException e) {
                logger.error("Could not read graph file {}", graphMLPath.toAbsolutePath().toString());
            }
        }
        return graph;
    }

    /**
     * Renames labels and properties to fit the graph model adopted by AStracker.
     */
    private void ensureCompatibility(){
        this.graph.traversal().E()
                .hasLabel(EdgeLabel.DEPENDSON.toString())
                .has("weight")
                .forEachRemaining(e -> {
                    e.property("Weight", e.value("weight"));
                    e.property("weight").remove();
                });

        switchEdgeLabel("isPartOfComponent", EdgeLabel.BELONGSTO.toString());
        switchEdgeLabel("afferent", EdgeLabel.PACKAGEISAFFERENTOF.toString());
        this.graph.traversal().V()
                .has("Type", TextP.containing("retrieved"))
                .drop().iterate();
        this.graph.traversal().V()
                .has("PackageType", TextP.containing("Retrieved"))
                .drop().iterate();
        this.graph.traversal().V()
                .has("ClassType", TextP.containing("Retrieved"))
                .drop().iterate();
    }

    private void switchEdgeLabel(String oldLabel, String newLabel){
        this.graph.traversal().E().hasLabel(oldLabel).forEachRemaining(edge -> {
            var newEdge = this.graph.traversal().addE(newLabel).from(edge.outVertex()).to(edge.inVertex()).next();
            edge.properties().forEachRemaining(p -> newEdge.property(p.key(), p.value()));
        });
        this.graph.traversal().E().hasLabel(oldLabel).drop().iterate();
    }

    @Override
    public void clearGraph(){
        graph = null;
        getSourceCodeRetriever().clearCache();
    }

    @Override
    public SourceCodeRetriever getSourceCodeRetriever() {
        return sourceCodeRetrieval;
    }

    /**
     * Returns the date string of this version.
     * @return a date in the format %dd-%mm-%yyyy. If no date was assigned to this version, an empty string is returned.
     */
    public String getVersionDate() {
        return versionDate;
    }

    @Override
    public String toString() {
        return String.format("%s: %s, %s", versionString, sourcePath, graphMLPath);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(versionIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractVersion))
            return false;
        AbstractVersion other = (AbstractVersion)obj;
        return other.versionString.equals(versionString) &&
                other.versionIndex == versionIndex;
    }

    @Override
    public int compareTo(IVersion version) {
        return Long.compare(this.getVersionIndex(), version.getVersionIndex());
    }

    /**
     * Return the parsed string version for the given Path.
     * @param f the path object to use for parsing the string version from the name.
     * @return the string version of the version
     */
    public String parseVersionString(Path f){
        var fileName = f.getFileName().toString();
        int endIndex = f.toFile().isDirectory() ? fileName.length() : fileName.lastIndexOf('.');

        var splits = fileName.substring(0, endIndex).split("-");
        String version;
        if (splits.length == 4){
            setVersionIndex(Long.parseLong(splits[1]));
            versionDate = String.join("-", splits[2].split("_"));
            version = splits[3];
        } else {
            version = splits[1];
        }
        return version;
    }

}
