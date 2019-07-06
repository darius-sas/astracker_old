package org.rug;

import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.data.project.Project;
import org.rug.data.project.Version;
import org.rug.persistence.*;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ProjecSizeRunner;
import org.rug.runners.ToolRunner;
import org.rug.runners.TrackASRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Analysis {

    private Args args;
    private IProject project;
    private List<ToolRunner> runners;


    public Analysis(Args args) throws Exception {
        this.args = args;
        this.runners = new ArrayList<>();
        init();
    }

    private void init() throws Exception{
        if (project == null){
            if (isJarProject()){
                Project p = new Project(args.projectName);
                p.addJars(args.getHomeProjectDirectory());
                var outputDir = args.getArcanOutDir();
                p.forEach(version -> {
                    Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                    outputDirVers.toFile().mkdirs();
                    var arcan = new ArcanRunner(args.getArcanJarFile(), (Version)version, outputDirVers.toString(), p.isFolderOfFoldersOfJarsProject(), false);
                    arcan.setHomeDir(args.getHomeProjectDirectory());
                    arcan.inheritOutput(args.showArcanOutput);
                    runners.add(arcan);
                });
                args.adjustProjDirToArcanOutput();
                p.addGraphMLs(args.getHomeProjectDirectory());
                project = p;
            }else if (isGraphMLProject()){
                Project p = new Project(args.projectName);
                p.addGraphMLs(args.getHomeProjectDirectory());
                project = p;
            }else {
                throw new Exception("Cannot parse project files.");
            }

            if (args.runTracker()){
                runners.add(new TrackASRunner(project, args.trackNonConsecutiveVersions));

                if (args.similarityScores)
                    PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));

                if (args.smellCharacteristics) {
                    PersistenceWriter.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile(), project));
                    PersistenceWriter.register(new ComponentAffectedByGenerator(args.getAffectedComponentsFile()));
                }

                PersistenceWriter.register(new CondensedGraphGenerator(args.getCondensedGraphFile()));
                PersistenceWriter.register(new TrackGraphGenerator(args.getTrackGraphFileName()));
            }

            if (args.runProjectSizes()){
                runners.add(new ProjecSizeRunner(project));
                PersistenceWriter.register(new ProjectSizeGenerator(args.getProjectSizesFile()));
            }
        }
    }

    public IProject getProject() {
        return project;
    }

    public List<ToolRunner> getRunners() {
        return runners;
    }

    private boolean isJarProject() throws IOException {
        return Files.walk(args.inputDirectory.toPath()).anyMatch(path -> path.getFileName().toString().toLowerCase().matches(".*\\.jar"));
    }

    private boolean isGraphMLProject() throws IOException{
        return Files.walk(args.inputDirectory.toPath()).anyMatch(path -> path.getFileName().toString().toLowerCase().matches(".*\\.graphml"));
    }

}
