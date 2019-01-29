package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.util.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;


class ASTracker2Test {

    private final static Logger logger = LoggerFactory.getLogger(ASTracker2Test.class);

    @Test
    void trackTest(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr");

        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASTracker2 tracker = new ASTracker2(scorer, false);
        var generator = new SmellSimilarityDataGenerator("data/jaccard-scores-antlr-consecutives-only.csv");
        var generator2 = new SmellCharacteristicsGenerator("data/smells-characteristics.csv");
        versionedSystem.forEach( (version, graph) -> {
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            logger.info("Tracking version {}", version);
            tracker.track(smells, version);
            generator.accept(tracker);
        });
        generator2.accept(tracker);
        PersistenceWriter.addCSVGenerator(generator);
        PersistenceWriter.addCSVGenerator(generator2);
        PersistenceWriter.writeAllCSV();
        //logger.info("Tracking completed. Generating simplified graph...");
        //tracker.writeSimplifiedGraph("src/test/graphimages/simplified-trackgraph-consecutives.graphml");
        //tracker.writeTrackGraph("src/test/graphimages/trackgraph-consecutives.graphml");

    }

}