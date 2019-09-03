package org.rug.data.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a project with multiple versions of JAR Files.
 */
public class Project extends AbstractProject {

    private boolean isFolderOfFolderOfJars;
    private boolean hasJars;
    private boolean hasGraphMLs;

    /**
     * Instantiates a project with the default type set to {@code Type.JAVA}, see {@link org.rug.data.project.AbstractProject.Type}.
     * @param name the name of the project.
     */
    public Project(String name){
        this(name, Type.JAVA);
    }
    /**
     * Instantiates a project with the given {@link org.rug.data.project.AbstractProject.Type}.
     * @param name the name of the project.
     * @param projectType the type of the project (i.e. the programming language).
     */
    public Project(String name, Type projectType){
        super(name, projectType);
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
    public void addSourceDirectory(String mainJarProjectDir) throws IOException {
        Path jarDirPath = Paths.get(mainJarProjectDir);
        this.isFolderOfFolderOfJars = !containsJars(jarDirPath);
        hasJars = true;

        if (!isFolderOfFolderOfJars){
            Files.list(jarDirPath)
                    .filter(Files::isRegularFile)
                    .filter(projectType::sourcesMatch)
                    .forEach(j -> {
                        var version = addVersion(j);
                        version.setSourceCodePath(j);
                    });
        }else{
            Files.list(jarDirPath)
                    .filter(Files::isDirectory)
                    .forEach(j -> {
                        var version = addVersion(j);
                        version.setSourceCodePath(j);
                    });
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
    public void addGraphMLfiles(String graphMLDir) throws IOException{
        File dir = new File(graphMLDir);

        var graphMlFiles = getGraphMls(dir.toPath());
        graphMlFiles.forEach(f -> {
            var version = addVersion(f);
            version.setGraphMLPath(f);
        });
        hasGraphMLs = true;
        initVersionPositions();
    }

    /**
     * Returns the nature of the project represented by this instance.
     * In a project where the main folder is full of jars, every version is represented by a single jar file.
     * On the other side, a project that is a folder of folders of jars, every version is a folder of jars.
     * NOTE: This value is by default false. You need to call {@link #addSourceDirectory(String)} in order to correctly set
     * this flag.
     * @return true if this project is a folder of folder of jars.
     */
    public boolean isFolderOfFoldersOfSourcesProject() {
        return isFolderOfFolderOfJars;
    }

    private boolean containsJars(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && Type.JAVA.sourcesMatch(f));
    }

    private List<Path> getGraphMls(Path dir) throws IOException{
        return Files.list(dir).filter(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.graphml")).collect(Collectors.toList());
    }


    /**
     * Helper method that adds a file to the versions of the system.
     * @param f the file to add.
     */
    private IVersion addVersion(Path f){
        IVersion version = new Version(f);
        version = versionedSystem.getOrDefault(version.getVersionString(), version);
        versionedSystem.putIfAbsent(version.getVersionString(), version);
        return version;
    }
}
