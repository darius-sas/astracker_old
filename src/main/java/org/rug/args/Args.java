package org.rug.args;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Args {

    @Parameter(names = {"-projectName", "-p"}, description = "The name of the project being analyzed, this name will be used to build the path within the given output folder.", required = true)
    public String projectName;

    @Parameter(names = {"-outputDir", "-o"}, description = "This name will be used to generate an outputDir directory where the outputDir will be saved.", required = true, converter = OutputDirManager.class)
    public File outputDir;

    @Parameter(names = {"-input", "-i"}, description = "The input directory containing a folder named as the given -projectName.", required = true, converter = InputDirManager.class)
    public File inputDirectory;

    @Parameter(names = {"-runArcan", "-rA"}, description = "Analyse files with Arcan. This parameter shall point to the command to the JAR containing Arcan, without any parameters. Ex. ./path/to/Arcan.jar.")
    private String runArcan = null;

    @Parameter(names = {"-doNotRunTracker", "-dRT"}, description = "Do not execute the tracking algorithm runner.")
    private boolean disableTrackerRunner = false;

    @Parameter(names = {"-runProjectSize", "-rS"}, description = "Whether to run the project size runner.")
    private boolean runProjectSizes = false;

    @Parameter(names = {"-cppProject", "-cppP"}, descriptionKey = "Flag this as a C++ project (i.e. project analysed with Arcan for C/C++).")
    public boolean isCPPproject = false;

    @Parameter(names = {"-cProject", "-cP"}, descriptionKey = "Flag this project as C project (i.e. project analysed with Arcan for C/C++).")
    public boolean isCProject = false;

    @Parameter(names = {"-gitRepo"}, descriptionKey = "Enable history-related metrics by passing to the git repository where to read the sources from.", converter = InputDirManager.class)
    public File gitRepo;

    @Parameter(names = {"-javaProject", "-jP"}, descriptionKey = "Flag this as a Java project (i.e. project analysed with Arcan for Java).")
    public boolean isJavaProject = true;

    @Parameter(names = {"-showArcanOutput", "-sAO"}, description = "Whether or not to show Arcan's output to the console.")
    public boolean showArcanOutput = false;

    @Parameter(names = {"-pSimilarity", "-pS"}, description = "Print similarity scores of the matched smells. This file is saved within the outputDir directory.")
    public boolean similarityScores = false;

    @Parameter(names = {"-pCharacteristics", "-pC"}, description = "Print the characteristics of the tracked smells for every analyzed version.")
    public boolean smellCharacteristics = false;

    @Parameter(names = {"-pCompoCharact", "-pCC"}, description = "Print the component characteristics/metrics for every analyzed version. As an argument, it expects the classpath of where to retrieve the classes (as JAR files) in the same way as provided to -inputDir. It is executed implicitly when -rA is set.", converter = InputDirManager.class)
    private File componentCharacteristics;

    @Parameter(names = {"-enableNonConsec", "-eNC"}, description = "Whether to track smells across non consecutive versions. This allows to track re-appeared smells, denoted by a special edge in the output track graph.")
    public boolean trackNonConsecutiveVersions = false;

    @Parameter(names = {"--help", "-h", "-help", "-?"}, help = true)
    public boolean help;

    public boolean runArcan(){
        return runArcan != null;
    }

    public boolean runTracker(){ return !disableTrackerRunner; }

    public boolean runProjectSizes(){ return runProjectSizes; }

    public String getArcanJarFile(){
        return new File(runArcan).getAbsolutePath();
    }

    public String getSimilarityScoreFile(){
        return getOutputFileName("similarity-scores", "csv");
    }

    public String getSmellCharacteristicsFile(){
        return getOutputFileName("smell-characteristics", "csv");
    }

    public String getAffectedComponentsFile(){
        return getOutputFileName("affected-components", "csv");
    }

    public String getCondensedGraphFile(){
        return getOutputFileName("condensed-graph", "graphml");
    }

    public String getTrackGraphFileName(){
        return getOutputFileName("track-graph", "graphml");
    }

    public String getProjectSizesFile(){return getOutputFileName("project-sizes", "csv");}

    public String getComponentCharacteristicsFile(){
        return getOutputFileName("component-characteristics", "csv");
    }

    private String getOutputFileName(String name, String format){
        String fileName = String.format("%s-%s.%s", name, (!trackNonConsecutiveVersions ? "consecOnly" : "nonConsec"), format);
        return Paths.get(getTrackASOutDir(), fileName).toString();
    }

    public String getHomeProjectDirectory(){
        return Paths.get(inputDirectory.getAbsolutePath(), projectName).toAbsolutePath().toString();
    }

    public void adjustProjDirToArcanOutput(){
        inputDirectory = new InputDirManager().convert(Paths.get(outputDir.getAbsolutePath(), "arcanOutput").toAbsolutePath().toString());
    }

    public String getArcanOutDir(){
        Path p = Paths.get(outputDir.getAbsolutePath(), "arcanOutput", projectName);
        p.toFile().mkdirs();
        return p.toAbsolutePath().toString();
    }

    private String getTrackASOutDir(){
        Path p = Paths.get(outputDir.getAbsolutePath(), "trackASOutput", projectName);
        p.toFile().mkdirs();
        return p.toAbsolutePath().toString();
    }

    public File getGitRepo() {
        return gitRepo;
    }

    public boolean isGitProject(){
        return gitRepo != null;
    }

    public String getClasspathComponentCharact(){
        if (componentCharacteristics == null)
            return "";
        return Paths.get(componentCharacteristics.getAbsolutePath(), projectName).toAbsolutePath().toString();
    }

}
