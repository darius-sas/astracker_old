package org.rug;

import org.rug.args.Args;
import org.rug.data.project.*;
import org.rug.data.project.AbstractProject.Type;
import org.rug.persistence.*;
import org.rug.runners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The class defines an analysis of a project. The class initializes objects of itself based on the arguments
 * provided by the command line.
 */
public class Analysis {

    private static final Logger logger = LoggerFactory.getLogger(Analysis.class.getName());

    private final Args args;
    private IProject project;
    private final List<ToolRunner> runners;


    public Analysis(Args args) throws IOException {
        this.args = args;
        this.runners = new ArrayList<>();
        init();
    }

    private void init() throws IOException{
        if (project == null){
            project = getProject();
            if (isGraphMLProject()){
                project.addGraphMLfiles(args.getHomeProjectDirectory());
            }else if (args.runArcan()) {
                var arcan = GitArcanRunner.newGitRunner(project, args);
                runners.add(arcan);
            }else if (args.isJarProject) {
                project.addSourceDirectory(args.getHomeProjectDirectory());
                var outputDir = args.getArcanOutDir();
                project.forEach(version -> {
                    Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                    if (outputDirVers.toFile().mkdirs() && version instanceof Version) {
                        var arcan = new ArcanRunner(args.getArcanJarFile(), (Version) version,
                                outputDirVers.toString(), project.isFolderOfFoldersOfSourcesProject(), false);
                        arcan.setHomeDir(args.getHomeProjectDirectory());
                        arcan.inheritOutput(args.showArcanOutput);
                        runners.add(arcan);
                    }
                });
                args.adjustProjDirToArcanOutput();
                project.addGraphMLfiles(args.getHomeProjectDirectory());
            } else {
                throw new IllegalArgumentException("Cannot parse project files.");
            }

            if (args.runTracker()){
                runners.add(new TrackASRunner(project, args.trackNonConsecutiveVersions));

                if (args.similarityScores) {
                    PersistenceHub.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));
                }

                if (args.smellCharacteristics) {
                    PersistenceHub.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile(), project));
                    PersistenceHub.register(new ComponentAffectedByGenerator(args.getAffectedComponentsFile()));
                }

                if (args.componentCharacteristics){
                    PersistenceHub.register(new ComponentMetricGenerator(args.getComponentCharacteristicsFile()));
                }

                PersistenceHub.register(new CondensedGraphGenerator(args.getCondensedGraphFile()));
                PersistenceHub.register(new TrackGraphGenerator(args.getTrackGraphFileName()));
            }

            if (args.runProjectSizes()){
                runners.add(new ProjecSizeRunner(project));
                PersistenceHub.register(new ProjectSizeGenerator(args.getProjectSizesFile()));
            }
        }
    }

    public IProject getProject() throws IOException {
        if (project == null) {
            Type pType;
            if (args.isCPPproject) {
                pType = Type.CPP;
            } else if (args.isCProject) {
                pType = Type.C;
            } else {
                pType = Type.JAVA;
            }

            if (args.isGitProject()) {
                project = new GitProject(args.projectName, args.getGitRepo(), pType);
                project.addSourceDirectory(args.getGitRepo().getAbsolutePath());
            } else {
                project = new Project(args.projectName, pType);
            }

        }
        return project;
    }

    public List<ToolRunner> getRunners() {
        return runners;
    }

    private boolean isGraphMLProject() throws IOException{
        try(var files = Files.walk(args.inputDirectory.toPath())){
            return files.anyMatch(path -> path.getFileName().toString().matches(".*\\.graphml"));
        }
    }

}
