package org.rug.data.project;

import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * Represents a version of the system under analysis. The version is a single analyzable unit.
 */
public interface IVersion extends Comparable<Version> {

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
}
