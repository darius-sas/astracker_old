package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.project.Version;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedMap;


class ASmellTrackerTest {

    private final static Logger logger = LoggerFactory.getLogger(ASmellTrackerTest.class);

    @Test
    void trackTest(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr");

        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);

        PersistenceWriter.register(new SmellSimilarityDataGenerator("test-data/output/trackASOutput/antlr/jaccard-scores-consecutives-only.csv"));
        PersistenceWriter.register(new SmellCharacteristicsGenerator("test-data/output/trackASOutput/antlr/smells-characteristics.csv", null)); // this test is out of date, added null to allow compilation

        int counter = 1;
        for (var entry : versionedSystem.entrySet()){
            var version = new Version();
            version.setVersionPosition(counter++);
            var graph = entry.getValue();
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            logger.info("Tracking version {}", version);
            tracker.track(smells, version);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        }
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
        //logger.info("Tracking completed. Generating simplified graph...");
        //tracker.writeCondensedGraph("src/test/graphimages/simplified-trackgraph-consecutives.graphml");
        //tracker.writeTrackGraph("src/test/graphimages/trackgraph-consecutives.graphml");

    }

}