package org.rug;

import com.beust.jcommander.JCommander;
import org.rug.args.Args;
import org.rug.data.project.Project;
import org.rug.persistence.*;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ProjecSizeRunner;
import org.rug.runners.ToolRunner;
import org.rug.runners.TrackASRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * The main of this tool sets up the computation of the necessary information in order to produce
     * the tracking output.
     * @param argv args to parse
     */
    public static void main(String[] argv)  {
        try {
            Args args = new Args();
            JCommander jc = JCommander.newBuilder()
                    .addObject(args)
                    .build();

            jc.setProgramName("java -jar trackas.jar");
            jc.parse(argv);

            if (args.help) {
                jc.usage();
                System.exit(0);
            }

            Project project = new Project(args.projectName);
            List<ToolRunner> runners = new ArrayList<>();
            if (args.runArcan()) {
                project.addJars(args.getHomeProjectDirectory());
                var outputDir = args.getArcanOutDir();
                project.forEach(version -> {
                    Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                    outputDirVers.toFile().mkdirs();
                    var arcan = new ArcanRunner(args.getArcanJarFile(), version, outputDirVers.toString(), project.isFolderOfFoldersOfJarsProject(), false);
                    arcan.setHomeDir(args.getHomeProjectDirectory());
                    arcan.inheritOutput(args.showArcanOutput);
                    runners.add(arcan);
                });
                args.adjustProjDirToArcanOutput();
            }
            project.addGraphMLs(args.getHomeProjectDirectory());

            if (args.runTracker()) {
                var ccclasshpath = args.getClasspathComponentCharact();
                if (!project.hasJars() && !ccclasshpath.isEmpty())
                    project.addJars(ccclasshpath);

                runners.add(new TrackASRunner(project, args.trackNonConsecutiveVersions));

                if (args.similarityScores)
                    PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));

                if (args.smellCharacteristics) {
                    PersistenceWriter.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile(), project));
                    PersistenceWriter.register(new ComponentAffectedByGenerator(args.getAffectedComponentsFile()));
                }

                if (!ccclasshpath.isEmpty())
                    PersistenceWriter.register(new ComponentMetricGenerator(args.getComponentCharacteristicsFile()));

                PersistenceWriter.register(new CondensedGraphGenerator(args.getCondensedGraphFile()));
                PersistenceWriter.register(new TrackGraphGenerator(args.getTrackGraphFileName()));
            }

            if (args.runProjectSizes()) {
                runners.add(new ProjecSizeRunner(project));
                PersistenceWriter.register(new ProjectSizeGenerator(args.getProjectSizesFile()));
            }

            boolean errorsOccurred = false;
            for (var r : runners) {
                int exitCode = r.start();
                errorsOccurred = exitCode != 0;
                if (errorsOccurred) {
                    break;
                }
            }
            if (!errorsOccurred) {
                logger.info("Writing to output directory...");
                PersistenceWriter.writeAllCSV();
                PersistenceWriter.writeAllGraphs();
            } else {
                System.exit(-1);
            }
        }catch (Exception e){
            logger.error("Unhandled error: {}", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
