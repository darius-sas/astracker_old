package org.rug.data.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a project with multiple versions of JAR Files.
 */
public class Project extends AbstractProject {

    private boolean isFolderOfFolderOfJars;

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
        super(name, projectType, new StringVersionComparator());
        this.isFolderOfFolderOfJars = false;
        this.versionInitializer = Version::new;
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

        if (!isFolderOfFolderOfJars){
            try(var files = Files.list(jarDirPath)) {
                    files.filter(Files::isRegularFile)
                        .filter(projectType::sourcesMatch)
                        .forEach(j -> {
                            var version = addVersion(j);
                            version.setSourceCodePath(j);
                        });
            }
        }else{
            try(var files = Files.list(jarDirPath)) {
                files.filter(Files::isDirectory)
                        .forEach(j -> {
                            var version = addVersion(j);
                            version.setSourceCodePath(j);
                        });
            }
        }

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
        try(var files = Files.list(dir)) {
            return files.anyMatch(f -> Files.isRegularFile(f) && Type.JAVA.sourcesMatch(f));
        }
    }


}
