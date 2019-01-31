package org.rug;

import com.beust.jcommander.JCommander;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.args.Args;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.runners.ToolRunner;
import org.rug.tracker.ASTracker2;
import org.rug.tracker.JaccardSimilarityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] argv) {

        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build().parse(argv);

        List<ToolRunner> tools = new ArrayList<>();
        if (args.runArcan){
            Pattern p = Pattern.compile("\\d+\\.\\d+(\\.\\d+\\w*)?");
            args.getJarFilesList().forEach(jar -> {
                String version = p.matcher(jar).group();
                String arcanCsv = Paths.get(args.getOutput().getAbsolutePath(), "arcanOutput", version, "csv").toString();
                String neo4jOutDir = Paths.get(args.getOutput().getAbsolutePath(), "arcanOutput", version, "neo4jDb").toString();
                tools.add(ToolRunner.getTool("arcan", "-p", jar,
                        "-jar", "-CD", "-HL", "-UD", "-CM", "-PM",
                        "-out", arcanCsv,
                        "-neo4j", "-d", neo4jOutDir));
            });
        }

        tools.forEach(t -> {
            try {
                logger.info("Running: ", String.join(" ", t.getBuilder().command()));
                t.start().waitFor();
            } catch (InterruptedException e) {
                logger.error("Error while executing tool: ", String.join(" ", t.getBuilder().command()));
                System.exit(-1);
            }
        });

        if (args.similarityScores)
            PersistenceWriter.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));

        if (args.smellCharacteristics)
            PersistenceWriter.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile()));

        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML(args.inputDirectory);

        ASTracker2 tracker = new ASTracker2(new JaccardSimilarityLinker(), args.trackNonConsecutiveVersions);

        versionedSystem.forEach( (version, graph) -> {
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            logger.info("Tracking version {}", version);
            tracker.track(smells, version);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });
        logger.info("Tracking complete, writing output...");
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
        tracker.writeSimplifiedGraph(args.getCondensedGraphFile());
        tracker.writeTrackGraph(args.getTrackGraphFileName());
    }
}
