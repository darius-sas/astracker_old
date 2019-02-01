package org.rug;

import com.beust.jcommander.JCommander;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.args.Args;
import org.rug.args.InputDirManager;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ToolRunner;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.JaccardSimilarityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] argv) {

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

        List<ToolRunner> runners = new ArrayList<>();
        if (args.runArcan){
            String outputDir = Paths.get(args.getOutputDir().toString(), "arcanOutput").toString();
            args.getInputTriples(Args.JAR_FILES_REGEX).forEach(t -> {
                String outputDirVers = Paths.get(outputDir, t.getB(), t.getC()).toString();
                runners.add(new ArcanRunner(t.getA(), t.getB(), t.getC(), outputDirVers));
            });
            File outDir = new File(outputDir);
            outDir.mkdirs();
            args.inputDirectory = new InputDirManager().convert(outputDir);
        }

        runners.forEach(ToolRunner::start);

        if (args.similarityScores)
            PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));

        if (args.smellCharacteristics)
            PersistenceWriter.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile()));

        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML(args.inputDirectory.getAbsolutePath());

        ASmellTracker tracker = new ASmellTracker(new JaccardSimilarityLinker(), args.trackNonConsecutiveVersions);

        versionedSystem.forEach( (version, graph) -> {
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            logger.info("Tracking version {}", version);
            tracker.track(smells, version);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });
        logger.info("Tracking complete, writing outputDir...");
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
        tracker.writeCondensedGraph(args.getCondensedGraphFile());
        tracker.writeTrackGraph(args.getTrackGraphFileName());
    }
}
