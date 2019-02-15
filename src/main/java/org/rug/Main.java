package org.rug;

import com.beust.jcommander.JCommander;
import org.rug.args.Args;
import org.rug.args.InputDirManager;
import org.rug.data.Project;
import org.rug.persistence.*;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ToolRunner;
import org.rug.runners.TrackASRunner;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * The main of this tool sets up the computation of the necessary information in order to produce
     * the tracking output.
     * @param argv args to parse
     */
    public static void main(String[] argv)  {

        Args args = new Args();
        JCommander jc = JCommander.newBuilder()
                .addObject(args)
                .build();

        jc.setProgramName("java -jar trackas.jar");
        jc.parse(argv);

        if (args.help){
            jc.usage();
            System.exit(0);
        }

        Project project = new Project(args.projectName);
        List<ToolRunner> runners = new ArrayList<>();
        try {
            if (args.runArcan) {
                project.addJars(args.inputDirectory.toString());
                String outputDir = Paths.get(args.getOutputDir().toString(), "arcanOutput").toString();
                project.getVersionedSystem().forEach((version, t) -> {
                    String outputDirVers = Paths.get(outputDir, project.getName(), version).toString();
                    runners.add(new ArcanRunner(t.getA().toString(), project.isFolderOfJarsProject(), project.getName(), version, outputDirVers));
                });
                File outDir = new File(outputDir);
                outDir.mkdirs();
                args.inputDirectory = new InputDirManager().convert(outputDir);
            }
            project.addGraphMLs(args.inputDirectory.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        runners.add(new TrackASRunner(project, args.trackNonConsecutiveVersions));

        if (args.similarityScores)
            PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));

        if (args.smellCharacteristics)
            PersistenceWriter.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile()));

        PersistenceWriter.register(new CondensedGraphGenerator(args.getCondensedGraphFile()));
        PersistenceWriter.register(new TrackGraphGenerator(args.getTrackGraphFileName()));

        runners.forEach(ToolRunner::start);

        PersistenceWriter.writeAllCSV();
        PersistenceWriter.writeAllGraphs();
    }
}
