package org.rug.simpletests.tracker;

import org.junit.jupiter.api.Test;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.ComponentAffectedByGenerator;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.ISimilarityLinker;
import org.rug.tracker.JaccardSimilarityLinker;
import java.util.List;
import static org.rug.simpletests.TestData.*;

public class ASmellTrackerTest {

    @Test
    void trackTestAntlr() {

        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);
        PersistenceWriter.clearAll();
        PersistenceWriter.register(new SmellSimilarityDataGenerator("test-data/output/trackASOutput/antlr/jaccard-scores-consecutives-only.csv"));
        PersistenceWriter.register(new SmellCharacteristicsGenerator("test-data/output/trackASOutput/antlr/smells-characteristics.csv", antlr)); // this test is out of date, added null to allow compilation
        var gen = new ComponentAffectedByGenerator("./test-data/output/trackASOutput/antlr/affectedComponents.csv");

        for (var version : antlr.versions()){
            var graph = version.getGraph();
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        }
        gen.accept(tracker);
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
    }


    void trackTestPure() {
        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);

        PersistenceWriter.register(
                new SmellSimilarityDataGenerator("test-data/output/trackASOutput/pure/jaccard-scores-consecutives-only.csv"));
        PersistenceWriter.register(
                new SmellCharacteristicsGenerator("test-data/output/trackASOutput/pure/smells-characteristics.csv", pure));

        pure.forEach(v -> {
            var smells = pure.getArchitecturalSmellsIn(v);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, v);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });

        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
    }

}