package org.rug.data.project;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a project with multiple versions.
 */
public class Project implements Iterable<Version> {

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    private String name;
    private boolean isFolderOfFolderOfJars;
    private boolean hasJars;
    private boolean hasGraphMLs;
    private SortedMap<String, Version> versionedSystem;

    public Project(String name){
        this.versionedSystem = new TreeMap<>(new StringVersionComparator());
        this.name = name;
        this.isFolderOfFolderOfJars = false;
        this.hasJars = false;
        this.hasGraphMLs = false;
    }

    /**
     * Add the jars contained in the given folder to the given project. The folder may point to either a folder
     * of jars or a folder of folders of jars.
     * @param mainJarProjectDir the home folder of the project.
     * @throws IOException if cannot read the given directory.
     */
    public void addJars(String mainJarProjectDir) throws IOException {
        Path jarDirPath = Paths.get(mainJarProjectDir);
        this.isFolderOfFolderOfJars = !containsJars(jarDirPath);

        if (!isFolderOfFolderOfJars){
            Files.list(jarDirPath)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().matches(".*\\.jar"))
                    .forEach(j -> addVersion(j, version -> version.setJarPath(j)));
        }else{
            Files.list(jarDirPath)
                    .filter(Files::isDirectory)
                    .forEach(j -> addVersion(j, version -> version.setJarPath(j)));
        }
        hasJars = true;
        initVersionPositions();
    }

    /**
     * Adds the given directory of graphML files to the current versioned system.
     * If directory does not exist, this method will fill the current versioned systems
     * with the paths to the ghost graphMl files. In that case, the paths will have
     * the following format: graphMLDir/name/version/name-version.graphml.
     * @param graphMLDir the directory where to read graph files from, or where they should be written.
     * @throws IOException
     */
    public void addGraphMLs(String graphMLDir) throws IOException{
        File dir = new File(graphMLDir);

        var graphMlFiles = getGraphMls(dir.toPath());
        if (!graphMlFiles.isEmpty() && !hasJars)
            graphMlFiles.forEach(f -> addVersion(f, version -> version.setGraphMLPath(f)));
        else
            versionedSystem.values().forEach(version -> {
                var graphmlFile = Paths.get(graphMLDir, name + "-" + version.getVersionString() + ".graphml");
                version.setGraphMLPath(graphmlFile);
            });
        hasGraphMLs = true;
        initVersionPositions();
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
     * Gets the name of the project as set up at instantiation time.
     * @return the name of the project.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the nature of the project represented by this instance.
     * In a project where the main folder is full of jars, every version is represented by a single jar file.
     * On the other side, a project that is a folder of folders of jars, every version is a folder of jars.
     * NOTE: This value is by default false. You need to call {@link #addJars(String)} in order to correctly set
     * this flag.
     * @return true if this project is a folder of folder of jars.
     */
    public boolean isFolderOfFoldersOfJarsProject() {
        return isFolderOfFolderOfJars;
    }

    /**
     * Indicates whether any graphML file has been added to this project.
     * @return true if there are graphML files in the project, false otherwise
     */
    public boolean hasGraphMLs() {
        return hasGraphMLs;
    }

    /**
     * Indicates whether any JAR file has been added to this project.
     * @return true if there are JAR files in the project, false otherwise
     */
    public boolean hasJars(){
        return hasJars;
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

    /**
     * Returns the architectural smells in the given version.
     * @param version the version of the system to parse smells from
     * @return the smells as a list.
     */
    public List<ArchitecturalSmell> getArchitecturalSmellsIn(Version version){
        return ArcanDependencyGraphParser.getArchitecturalSmellsIn(version.getGraph());
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
    public Iterator<Version> iterator() {
        return versionedSystem.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super Version> action) {
        versionedSystem.values().forEach(action);
    }

    @Override
    public Spliterator<Version> spliterator() {
        return versionedSystem.values().spliterator();
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
    public SortedSet<Version> versions(){
        return new TreeSet<>(versionedSystem.values());
    }

    private boolean containsJars(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.jar"));
    }

    private List<Path> getGraphMls(Path dir) throws IOException{
        return Files.list(dir).filter(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.graphml")).collect(Collectors.toList());
    }

    /**
     * Initializes the version positions.
     */
    private void initVersionPositions(){
        long counter = 1;
        for (var version : versionedSystem.values()){
            version.setVersionPosition(counter++);
        }
    }

    /**
     * Helper method that adds a file to the versions of the system.
     * @param f the file to add.
     * @param versionPathSetter a function that given a Version object, sets the path parameter(s) based
     *                          on their file format.
     */
    private void addVersion(Path f, Consumer<Version> versionPathSetter){
        var versionString = Version.parseVersion(f);
        var version = versionedSystem.getOrDefault(versionString, new Version(f));
        versionPathSetter.accept(version);
        versionedSystem.putIfAbsent(version.getVersionString(), version);
    }
}
