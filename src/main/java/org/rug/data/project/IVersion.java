package org.rug.data.project;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.characteristics.comps.SourceCodeRetriever;

import java.nio.file.Path;

/**
 * Represents a version of the system under analysis. The version is a single analyzable unit.
 */
public interface IVersion extends Comparable<IVersion> {

    /**
     * Retrieve the string representing this version. This may differ from the
     * representation offered by {@link #toString()} and shall be used to uniquely identify this version.
     * @return a string representing this version.
     */
    String getVersionString();

    /**
     * The position order of this version in comparison to the other versions in the system
     * @return a long integer representing the position of this version.
     */
    long getVersionPosition();

    /**
     * Sets the position of this version in comparison to the rest of the versions in the system.
     * @param versionPosition The position of this version.
     */
    void setVersionPosition(long versionPosition);

    /**
     * Retrieves the dependency graph for the version represented by this instance.
     * @return a dependency graph containing also the smells detected by the system.
     */
    Graph getGraph();

    /**
     * Get the source code retriever for this version.
     * @return a source code retriever that returns the source code of this version.
     */
    SourceCodeRetriever getSourceCodeRetriever();

    /**
     * Set the path to the GraphML file corresponding to this version.
     * @param f the path to the file.
     */
    void setGraphMLPath(Path f);

    /**
     * Retrieve GraphML files folder path.
     * @return the path of the GraphML folder.
     */
    Path getGraphMLPath();

    /**
     * Set the path to the source code folder or file.
     * @param p the path to the source code.
     */
    void setSourceCodePath(Path p);

    /**
     * Get the source code path, which might be either a file or a folder.
     */
    Path getSourceCodePath();

    /**
     * Sets the current graph to null in order for the JVM to remove the graph from memory as soon
     * as it is not required anymore within the system.
     * Additionally, cleanses the source code loaded by the retrievers.
     * This method is supposed to be used to save memory.
     */
    void clearGraph();
}
