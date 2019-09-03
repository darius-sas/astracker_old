package org.rug.simpletests.tracker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.project.IProject;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.ComponentAffectedByGenerator;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.ISimilarityLinker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rug.simpletests.TestData.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unitTests")
public class ASmellTrackerTest {

    private Map<String, Integer> antlrOracle;
    private Map<String, Integer> pureOracle;
//    private Map<String, Integer> antOracle;

    @BeforeAll
    void init(){
        antlrOracle = new HashMap<>();
        var oracle = new int[]{0, 3, 3, 4, 5, 3, 26, 61, 61, 26, 35, 0, 113, 36, 183, 24, 44,
                               95, 125, 117, 151, 29};
        int i = 0;
        for(var v : antlr){
            antlrOracle.put(v.getVersionString(), oracle[i++]);
        }

        pureOracle = new HashMap<>();
        oracle = new int[]{ 0, 132, 132, 132, 132};
        i = 0;
        for (var v : pure){
            pureOracle.put(v.getVersionString(), oracle[i++]);
        }
//        antOracle = new HashMap<>();
//        oracle = new int[]{0, 9, 29, 42, 55, 27, 97, 94, 94, 100, 53, 143, 151, 222, 234, 234, 210,
//                           205, 167, 181, 145, 348, 231};
//        for(var v : ant){
//            antlrOracle.put(v.getVersionString(), oracle[i++]);
//        }

    }

    @Test
    void trackTestAntlr() {
        trackTestProject(antlr, antlrOracle);
    }

    @Test
    void trackTestPure() {
        trackTestProject(pure, pureOracle);
    }

    private void trackTestProject(IProject project, Map<String, Integer> oracle) {

        ISimilarityLinker scorer = new SimpleNameJaccardSimilarityLinker();
        ASmellTracker tracker = new ASmellTracker(scorer, false);
        PersistenceWriter.clearAll();
        PersistenceWriter.register(new SmellSimilarityDataGenerator(Paths.get(trackASOutputDir, project.getName(), "jaccard-scores-consecutives-only.csv").toString()));
        PersistenceWriter.register(new SmellCharacteristicsGenerator(Paths.get(trackASOutputDir, project.getName(), "smells-characteristics.csv").toString(), project)); // this test is out of date, added null to allow compilation
        var gen = new ComponentAffectedByGenerator(Paths.get(trackASOutputDir, project.getName(), "affectedComponents.csv").toString());

        for (var version : project){
            List<ArchitecturalSmell> smells = project.getArchitecturalSmellsIn(version);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            assertEquals(Long.valueOf(oracle.get(version.getVersionString())), tracker.smellsLinked());
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        }
        gen.accept(tracker);
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.writeAllCSV();
    }

}