package org.rug.data.project;

import org.rug.data.smells.ArchitecturalSmell;

import java.util.List;
import java.util.SortedSet;

/**
 * Represents a project that can be analysed.
 */
public interface IProject extends Iterable<IVersion> {

    /**
     * Gets the name of the project as set up at instantiation time.
     * @return the name of the project.
     */
    String getName();

    /**
     * Retrieves the architectural smells detected in the given version.
     * @param version The version where to retrieve smells from.
     * @return a list containing the smells found in the given version.
     */
    List<ArchitecturalSmell> getArchitecturalSmellsIn(IVersion version);

    /**
     * See {@link #getArchitecturalSmellsIn(IVersion)}.
     */
    List<ArchitecturalSmell> getArchitecturalSmellsIn(String version);

    /**
     * Retrieves a IVersion instance of the version represented by the given string.
     * @param version a version string.
     * @return an instance of IVersion.
     */
    IVersion getVersion(String version);

    /**
     * Returns the versions in the system ordered by their natural order.
     * @return a sorted set.
     */
    SortedSet<IVersion> versions();

    /**
     * Returns the number of versions currently stored in this project instance.
     * @return the number of versions as a long.
     */
    long numberOfVersions();


}
