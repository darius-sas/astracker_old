package org.rug.data.project;

import org.rug.data.smells.ArchitecturalSmell;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.BiConsumer;

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

    /**
     * Returns the index in the order list of versions of this project.
     * This collection is automatically updated when the system's versions change.
     * @param version the version to return the position of.
     * @return the position of the given version in the ordered list of versions of this system.
     */
    Long getVersionIndex(String version);

    /**
     * Add the directory where the sources of the different versions are stored.
     * @param sourceMainDir a path to the sources.
     * @exception IOException raised when the given directory {@code sourceMainDir} does not exist.
     */
    void addSourceDirectory(String sourceMainDir) throws IOException;

    /**
     * Wethere the source directory set up with {@link #addSourceDirectory(String)} is a folder of folders of sources.
     * @return true if the folder contains folders of sources, false otherwise.
     */
    boolean isFolderOfFoldersOfSourcesProject();

    /**
     * The directory where the GraphML files for all the versions to analyse are stored.
     * @param dir the directory containing the GraphML files.
     * @throws IOException raised if {@code dir} does not exist.
     */
    void addGraphMLfiles(String dir) throws IOException;

    /**
     * Returns the version with the given versionPosition.
     * @param versionPosition the position of the version as an index.
     * @return the version object for which {@link IVersion#getVersionIndex()} equals the given value.
     */
    IVersion getVersionWith(long versionPosition);

    /**
     * Returns a sorted map where keys are versions of the system and values are triples
     * where the first element is the directory, or jar file, corresponding to the graphml file, saved as the second
     * element, and also to corresponding system graph, saved as third element.
     * @return a sorted map as described above.
     */
    SortedMap<String, IVersion> getVersionedSystem();

    /**
     * Set the sorted map representing the versions of this project.
     * @param versionedSystem the map.
     * @see #getVersionedSystem()
     */
    void setVersionedSystem(SortedMap<String, IVersion> versionedSystem);

    /**
     * Iterates over the versions of the system and returns an index of the version.
     * Note that this index might differ from the versionPosition.
     * @param action the function to execute.
     */
    void forEach(BiConsumer<? super IVersion, Long> action);
}
