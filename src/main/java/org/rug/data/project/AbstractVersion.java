package org.rug.data.project;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.characteristics.comps.ClassSourceCodeRetriever;
import org.rug.data.labels.EdgeLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractVersion implements IVersion {

    private final static Logger logger = LoggerFactory.getLogger(Version.class);

    protected String versionString;
    protected long versionPosition;
    protected Path sourcePath;
    protected Path graphMLPath;
    protected Graph graph;
    protected ClassSourceCodeRetriever sourceCodeRetrieval;

    /**
     * Partially builds this version by setting the version string starting from the given path.
     * @param path the path to parse the version string from.
     * @param sourceCodeRetrieval the source code retriever object.
     */
    public AbstractVersion(Path path, ClassSourceCodeRetriever sourceCodeRetrieval){
        this.versionString = parseVersionString(path);
        this.sourceCodeRetrieval = sourceCodeRetrieval;
        this.sourceCodeRetrieval.setClassPath(path);
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
    public long getVersionPosition() {
        return versionPosition;
    }

    /**
     * Set the order position of this versionString.
     * @param versionPosition The versionString position.
     */
    public void setVersionPosition(long versionPosition) {
        this.versionPosition = versionPosition;
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
    public Graph getGraph() {
        if (graph == null) {
            graph = TinkerGraph.open();
            try {
                var graphMLfile = graphMLPath.toFile();
                if (graphMLfile.isFile() && graphMLfile.canRead()) {
                    this.graph.traversal().io(graphMLPath.toAbsolutePath().toString())
                            .read().with(IO.reader, IO.graphml).iterate();
                    // This statement ensures compatibility between graphml files produced
                    // with Arcan and Arcan for C/C++
                    this.graph.traversal().E()
                            .hasLabel(EdgeLabel.DEPENDSON.toString())
                            .has("weight")
                            .forEachRemaining(e -> {
                                e.property("Weight", e.value("weight"));
                                e.property("weight").remove();
                            });
                }else {
                    throw new IOException("");
                }
            } catch (IOException e) {
                logger.error("Could not read graph file {}", graphMLPath.toAbsolutePath().toString());
            }
        }
        return graph;
    }

    @Override
    public void clearGraph(){
        graph = null;
    }

    @Override
    public ClassSourceCodeRetriever getSourceCodeRetriever() {
        return sourceCodeRetrieval;
    }

    @Override
    public String toString() {
        return String.format("%s: %s, %s", versionString, sourcePath, graphMLPath);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(versionPosition);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractVersion))
            return false;
        AbstractVersion other = (AbstractVersion)obj;
        return other.versionString.equals(versionString) &&
                other.versionPosition == versionPosition;
    }

    @Override
    public int compareTo(IVersion version) {
        return Long.compare(this.getVersionPosition(), version.getVersionPosition());
    }

    /**
     * Return the parsed string version for the given Path.
     * @param f the path object to use for parsing the string version from the name.
     * @return the string version of the version
     */
    public String parseVersionString(Path f){
        int endIndex = f.toFile().isDirectory() ? f.toString().length() : f.toString().lastIndexOf('.');
        String version = f.toString().substring(
                f.toString().lastIndexOf('-') + 1,
                endIndex);
        return version;
    }

}
