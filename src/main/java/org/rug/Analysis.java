package org.rug;

import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.data.project.Project;
import org.rug.data.project.Version;
import org.rug.persistence.ComponentAffectedByGenerator;
import org.rug.persistence.CondensedGraphGenerator;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.ProjectSizeGenerator;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.persistence.TrackGraphGenerator;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ProjecSizeRunner;
import org.rug.runners.ToolRunner;
import org.rug.runners.TrackASRunner;
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


    public Analysis(Args args) throws IOException, IllegalArgumentException {
        this.args = args;
        this.runners = new ArrayList<>();
        init();
    }

    private void init() throws IllegalArgumentException, IOException{
        if (project == null){
            if (isJarProject()){
                Project p = new Project(args.projectName);
                p.addJars(args.getHomeProjectDirectory());
                var outputDir = args.getArcanOutDir();
                p.forEach(version -> {
                    Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                    if (outputDirVers.toFile().mkdirs()) {
                        var arcan = new ArcanRunner(args.getArcanJarFile(), (Version) version, outputDirVers.toString(), p.isFolderOfFoldersOfJarsProject(), false);
                        arcan.setHomeDir(args.getHomeProjectDirectory());
                        arcan.inheritOutput(args.showArcanOutput);
                        runners.add(arcan);
                    }else{
                        logger.error("Could not create output directories for Arcan analysis for version {}", version.getVersionString());
                    }
                });
                args.adjustProjDirToArcanOutput();
                p.addGraphMLs(args.getHomeProjectDirectory());
                project = p;
            }else if (isGraphMLProject()){
                Project p = new Project(args.projectName);
                p.addGraphMLs(args.getHomeProjectDirectory());
                project = p;
            }else {
                throw new IllegalArgumentException("Cannot parse project files.");
            }

            if (args.runTracker()){
                runners.add(new TrackASRunner(project, args.trackNonConsecutiveVersions));

                if (args.similarityScores) {
                    PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));
                }

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
        return Files.walk(args.inputDirectory.toPath()).anyMatch(path -> path.getFileName().toString().matches(".*\\.jar"));
    }

    private boolean isGraphMLProject() throws IOException{
        return Files.walk(args.inputDirectory.toPath()).anyMatch(path -> path.getFileName().toString().matches(".*\\.graphml"));
    }

}
