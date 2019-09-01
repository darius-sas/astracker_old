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

import java.nio.file.Paths;
import java.util.List;
import static org.rug.simpletests.TestData.*;

public class ASmellTrackerTest {

    @Test
    void trackTestAntlr() {

        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);
        PersistenceWriter.clearAll();
        PersistenceWriter.register(new SmellSimilarityDataGenerator(Paths.get(trackASOutputDir, antlr.getName(), "jaccard-scores-consecutives-only.csv").toString()));
        PersistenceWriter.register(new SmellCharacteristicsGenerator(Paths.get(trackASOutputDir, antlr.getName(), "smells-characteristics.csv").toString(), antlr)); // this test is out of date, added null to allow compilation
        var gen = new ComponentAffectedByGenerator(Paths.get(trackASOutputDir, antlr.getName(), "affectedComponents.csv").toString());

        for (var version : antlr){
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

    @Test
    void trackTestPure() {
        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);

        PersistenceWriter.register(
                new SmellSimilarityDataGenerator(Paths.get(trackASOutputDir, pure.getName(), "jaccard-scores-consecutives-only.csv").toString()));
        PersistenceWriter.register(
                new SmellCharacteristicsGenerator(Paths.get(trackASOutputDir, pure.getName(), "smells-characteristics.csv").toString(), pure));

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