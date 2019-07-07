package org.rug.data.project;

import org.rug.data.characteristics.comps.ClassSourceCodeRetriever;
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
 * Represents a project with multiple versions of JAR Files.
 */
public class Project extends AbstractProject {

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    private boolean isFolderOfFolderOfJars;
    private boolean hasJars;
    private boolean hasGraphMLs;

    public Project(String name){
        super(name);
        this.isFolderOfFolderOfJars = false;
        this.hasJars = false;
        this.hasGraphMLs = false;
        this.versionedSystem = new TreeMap<>(new StringVersionComparator());
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
        hasJars = true;

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
        for (var version : getVersionedSystem().values()){
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
