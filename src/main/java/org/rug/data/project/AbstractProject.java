package org.rug.data.project;

import org.rug.data.smells.ArchitecturalSmell;

import java.util.*;
import java.util.function.Consumer;

/**
 * Provides an implementation for the common operations on a project.
 */
public abstract class AbstractProject implements IProject {

    protected SortedMap<String, Version> versionedSystem;
    protected String name;

    /**
     * Instantiates this project and sets the given name.
     * @param name the name of the project.
     */
    public AbstractProject(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the project as set up at instantiation time.
     * @return the name of the project.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the architectural smells in the given version.
     * @param version the version of the system to parse smells from
     * @return the smells as a list.
     */
    public List<ArchitecturalSmell> getArchitecturalSmellsIn(IVersion version){
        var smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(version.getGraph());
        var versionString = version.getVersionString();
        smells.forEach(as -> as.setAffectedVersion(versionString));
        return smells;
    }

    /**
     * Returns the architectural smells in the given version.
     * @param version the version of the system to parse smells from
     * @return the smells as a list.
     */
    public List<ArchitecturalSmell> getArchitecturalSmellsIn(String version){
        return getArchitecturalSmellsIn(versionedSystem.get(version));
    }

    @Override
    public Iterator<IVersion> iterator() {
        return (Iterator<IVersion>)(Iterator<?>)versionedSystem.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super IVersion> action) {
        versionedSystem.values().forEach(action);
    }

    @Override
    public Spliterator<IVersion> spliterator() {
        return (Spliterator<IVersion>)(Spliterator<?>) versionedSystem.values().spliterator();
    }

    /**
     * Returns the version of the system with the given version string.
     * @param version the string denoting the version to retrieve.
     * @return the version object mapped to the given version string.
     */
    public Version getVersion(String version){
        return versionedSystem.get(version);
    }

    /**
     * Returns the number of versions in this project.
     * @return the counting of the versions.
     */
    public long numberOfVersions(){
        return versionedSystem.size();
    }

    /**
     * Returns a copy of the sorted set of versions in this system.
     * @return a sorted set of versions.
     */
    public SortedSet<IVersion> versions(){
        return new TreeSet<>(versionedSystem.values());
    }

    /**
     * Returns the index in the order list of versions of this project.
     * This collection is automatically updated when the system's versions change.
     * @param version the version to return the position of.
     * @return the position of the given version in the ordered list of versions of this system.
     */
    public Long getVersionIndex(String version){
        return versionedSystem.get(version).getVersionPosition();
    }

    /**
     * Returns a sorted map where keys are versions of the system and values are triples
     * where the first element is the directory, or jar file, corresponding to the graphml file, saved as the second
     * element, and also to corresponding system graph, saved as third element.
     * @return a sorted map as described above.
     */
    public SortedMap<String, Version> getVersionedSystem() {
        return versionedSystem;
    }
}
