package org.rug.args;

import com.beust.jcommander.Parameter;

import java.io.File;

public class ProjectArgsManager {
    @Parameter(names = {"-projectName", "-p"}, description = "The name of the project being analyzed, this name will be used to build the path within the given output folder.", required = true)
    public String name;

    @Parameter(names = {"-cppProject", "-cppP"}, description = "Flag this as a C++ project (i.e. project analysed with Arcan for C/C++).")
    public boolean isCPP = false;

    @Parameter(names = {"-cProject", "-cP"}, description = "Flag this project as C project (i.e. project analysed with Arcan for C/C++).")
    public boolean isC = false;

    @Parameter(names = {"-gitRepo"}, description = "Enable history-related metrics by passing the git repository where to read the sources from.", converter = InputDirManager.class)
    public File gitRepo;

    @Parameter(names = {"-javaProject", "-jP"}, description = "Flag this as a Java project (i.e. project analysed with Arcan for Java).")
    public boolean isJava = true;

    @Parameter(names = {"-jarProject", "-jar"}, description = "Flag to denote a project of JAR files (either directories of Jar files or Jar files only).")
    public boolean isJar = false;

}
