package org.rug.data.project;

import org.rug.data.smells.ArchitecturalSmell;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Provides an implementation for the common operations on a project.
 */
public abstract class AbstractProject implements IProject {

    protected SortedMap<String, IVersion> versionedSystem;
    protected String name;
    protected Type projectType;
    /**
     * Instantiates this project and sets the given name.
     * @param name the name of the project.
     */
    public AbstractProject(String name, Type projectType) {
        this.name = name;
        this.projectType = projectType;
        this.versionedSystem = new TreeMap<>(projectType.getVersionComparator());
        ArcanDependencyGraphParser.PROJECT_TYPE = projectType;
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
        return versionedSystem.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super IVersion> action) {
        versionedSystem.values().forEach(action);
    }

    @Override
    public Spliterator<IVersion> spliterator() {
        return versionedSystem.values().spliterator();
    }

    /**
     * Returns the version of the system with the given version string.
     * @param version the string denoting the version to retrieve.
     * @return the version object mapped to the given version string.
     */
    public IVersion getVersion(String version){
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
    @Override
    public Long getVersionIndex(String version){
        return versionedSystem.get(version).getVersionPosition();
    }

    /**
     * Returns a sorted map where keys are versions of the system and values are triples
     * where the first element is the directory, or jar file, corresponding to the graphml file, saved as the second
     * element, and also to corresponding system graph, saved as third element.
     * @return a sorted map as described above.
     */
    public SortedMap<String, IVersion> getVersionedSystem() {
        return versionedSystem;
    }

    /**
     * Initializes the version positions.
     */
    protected void initVersionPositions(){
        long counter = 1;
        for (var version : getVersionedSystem().values()){
            version.setVersionPosition(counter++);
        }
    }

    /**
     * Returns the type of the project under analysis. Namely, the programming language used.
     * @return the programming language of the analysed project.
     */
    public Type getProjectType() {
        return projectType;
    }

    /**
     * Defines the project type under analysis (programming language).
     * A project type instantiates the version instance object based
     * on the type of project.
     */
    public enum Type {
        C("C", Version::new, StringVersionComparator::new, Pattern.compile("^.*\\.([ch])$")),
        CPP("C++", Version::new, StringVersionComparator::new, Pattern.compile("^.*\\.((cpp)|[ch])$")),
        JAVA("Java", Version::new, StringVersionComparator::new, Pattern.compile("^.*\\.jar$"));

        private String typeName;
        private Function<Path, IVersion> versionSupplier;
        private Supplier<Comparator<String>> versionComparatorSupplier;
        private Pattern sourcesFileExt;

        Type(String typeName, Function<Path, IVersion> versionSupplier,
             Supplier<Comparator<String>> versionComparatorSupplier, Pattern sourcesFileExt){
            this.typeName = typeName;
            this.versionSupplier = versionSupplier;
            this.versionComparatorSupplier = versionComparatorSupplier;
            this.sourcesFileExt = sourcesFileExt;
        }

        /**
         * The name of the project type.
         * @return the project type.
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * An empty instance of the version under analysis
         * @param p the path to use to instantiate the version.
         * @return an {@link IVersion} object.
         */
        public IVersion getVersionInstance(Path p) {
            return versionSupplier.apply(p);
        }

        /**
         * Returns version the string  comparator for this type of project.
         * @return a version string comparator.
         */
        public Comparator<String> getVersionComparator() {
            return versionComparatorSupplier.get();
        }

        /**
         * Returns the regular expression pattern matching the files for this project type.
         * @return a compiled Regex pattern.
         */
        public boolean sourcesMatch(Path p){
            return sourcesFileExt.matcher(p.toString()).matches();
        }
    }
}
