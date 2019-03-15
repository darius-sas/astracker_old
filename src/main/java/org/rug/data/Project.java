package org.rug.data;

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
public class Project {

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    private String name;
    private boolean isFolderOfFolderOfJars;
    private boolean hasJars;
    private boolean hasGraphMLs;
    private SortedMap<String, Triple<Path, Path, Graph>> versionedSystem;
    private Map<String, Integer> versionsIndexes = new HashMap<>();

    public Project(String name){
        this.versionedSystem = new TreeMap<>(new VersionComparator());
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

        Consumer<Path> addVersion = j ->{
            var version = parseVersion(j);
            var t = versionedSystem.getOrDefault(version, new InputTriple(null, null));
            t.setA(j);
            versionedSystem.putIfAbsent(version, t);
        };

        if (!isFolderOfFolderOfJars){
            Files.list(jarDirPath)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().matches(".*\\.jar"))
                    .forEach(addVersion);
        }else{
            Files.list(jarDirPath)
                    .filter(Files::isDirectory)
                    .forEach(addVersion);
        }
        hasJars = true;
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
        if (!graphMlFiles.isEmpty() && !hasJars) {
            graphMlFiles.forEach(f -> {
                var version = parseVersion(f);
                var t = versionedSystem.getOrDefault(version, new InputTriple(null, null));
                t.setB(f);
                versionedSystem.putIfAbsent(version, t);
            });
        } else {
            versionedSystem.forEach((version, inputTriple) -> {
                var graphmlFile = Paths.get(graphMLDir, name + "-" + version + ".graphml");
                inputTriple.setB(graphmlFile);
            });
        }
        hasGraphMLs = true;
    }

    /**
     * Returns the index in the order list of versions of this project.
     * This collection is automatically updated when the system's versions change.
     * @param version the version to return the position of.
     * @return the position of the given version in the ordered list of versions of this system.
     */
    public Integer getVersionIndex(String version){
        if (!versionsIndexes.keySet().equals(versionedSystem.keySet())) {
            versionsIndexes.clear();
            int order = 1;
            for(var v : versionedSystem.keySet()){
                versionsIndexes.put(v, order++);
            }
        }
        return versionsIndexes.get(version);
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
    public SortedMap<String, Triple<Path, Path, Graph>> getVersionedSystem() {
        return versionedSystem;
    }

    /**
     * Returns the architectural smells in the given version.
     * @param version the version of the system to parse smells from
     * @return the smells as a list.
     */
    public List<ArchitecturalSmell> getArchitecturalSmellsIn(String version){
        return ArcanDependencyGraphParser.getArchitecturalSmellsIn(versionedSystem.get(version).getC());
    }

    private String parseVersion(Path f){
        int endIndex = f.toFile().isDirectory() ? f.toString().length() : f.toString().lastIndexOf('.');
        String version = f.toString().substring(
                f.toString().lastIndexOf('-') + 1,
                endIndex);
        return version;
    }

    private boolean containsJars(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.jar"));
    }

    private List<Path> getGraphMls(Path dir) throws IOException{
        return Files.list(dir).filter(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.graphml")).collect(Collectors.toList());
    }

    /**
     * This input triple lazily loads the graphml when it is first requested. This allows for Arcan to
     * calculate the graphml and avoids errors.
     */
    static class InputTriple extends Triple<Path, Path, Graph>{

        public InputTriple(Path jarPath, Path graphMLpath) {
            super(jarPath, graphMLpath, null);
        }

        @Override
        public Graph getC() {
            if (super.getC() == null) {
                this.c = TinkerGraph.open();
                try {
                    this.c.io(IoCore.graphml()).readGraph(getB().toAbsolutePath().toString());
                } catch (IOException e) {
                    logger.error("Could not read file {}", getB());
                }
            }
            return super.getC();
        }
    }
}
