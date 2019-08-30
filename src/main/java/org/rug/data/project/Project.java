package org.rug.data.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a project with multiple versions of JAR Files.
 */
public class Project extends AbstractProject {

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
                    .forEach(j -> addVersion(j, true));
        }else{
            Files.list(jarDirPath)
                    .filter(Files::isDirectory)
                    .forEach(j -> addVersion(j, true));
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
        graphMlFiles.forEach(f -> addVersion(f, false));
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
     * @param isJar whether f is to be added as a Jar(true) or as a graphML (false).
     */
    private void addVersion(Path f, boolean isJar){
        IVersion version = versionedSystem.getOrDefault(Version.parseVersion(f), new Version(f));
        if (version instanceof Version) {
            if(isJar) {
                ((Version) version).setJarPath(f);
            } else {
                ((Version) version).setGraphMLPath(f);
            }
        }
        versionedSystem.putIfAbsent(version.getVersionString(), version);
    }
}
