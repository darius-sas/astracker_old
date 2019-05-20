package org.rug.data.project;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a version of a system.
 */
public class Version implements Comparable<Version>{

    private final static Logger logger = LoggerFactory.getLogger(Version.class);

    private String versionString;
    private long versionPosition;
    private Path jarPath;
    private Path graphMLPath;
    private Graph graph;

    public Version(){}

    /**
     * Partially builds this instance by parsing the version string from the given path.
     * @param path the directory or file that respect {@link #parseVersion(Path)} version formatting.
     */
    public Version(Path path){
        this.versionString = parseVersion(path);
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

    public Path getJarPath() {
        return jarPath;
    }

    public void setJarPath(Path jarPath) {
        this.jarPath = jarPath;
    }

    public Path getGraphMLPath() {
        return graphMLPath;
    }

    public void setGraphMLPath(Path graphMLPath) {
        this.graphMLPath = graphMLPath;
    }

    /**
     * Lazily loads the Graph from the given GraphML file at the first invocation.
     * The Graph is then cached for future access.
     * @return the dependency graph of this version.
     */
    public Graph getGraph() {
        if (graph == null) {
            graph = TinkerGraph.open();
            try {
                var graphMLfile = graphMLPath.toFile();
                if (graphMLfile.isFile() && graphMLfile.canRead())
                    this.graph.traversal().io(graphMLPath.toAbsolutePath().toString())
                            .read().with(IO.reader, IO.graphml).iterate();
                else
                    throw new IOException("");
            } catch (IOException e) {
                logger.error("Could not read file {}", graphMLPath.toAbsolutePath().toString());
            }
        }
        return graph;
    }

    @Override
    public String toString() {
        return String.format("%s: %s, %s", versionString, jarPath, graphMLPath);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(versionPosition);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version))
            return false;
        Version other = (Version)obj;
        return other.versionString.equals(versionString) &&
                other.versionPosition == versionPosition;
    }

    @Override
    public int compareTo(Version version) {
        return Long.compare(this.versionPosition, version.versionPosition);
    }

    /**
     * Parse a versionString from a file. The versionString must be preceeded by '-' and be the suffix of the filename.
     * For example project-0.2.1.ext.
     * @param f The path to the file to parse.
     * @return a string versionString.
     */
    public static String parseVersion(Path f){
        int endIndex = f.toFile().isDirectory() ? f.toString().length() : f.toString().lastIndexOf('.');
        String version = f.toString().substring(
                f.toString().lastIndexOf('-') + 1,
                endIndex);
        return version;
    }

}
