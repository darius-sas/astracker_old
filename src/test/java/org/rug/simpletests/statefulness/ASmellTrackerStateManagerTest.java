package org.rug.simpletests.statefulness;

import org.junit.jupiter.api.Test;
import org.rug.data.project.IProject;
import org.rug.simpletests.tracker.ASmellTrackerTest;
import org.rug.statefulness.ASmellTrackerStateManager;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rug.simpletests.TestData.antlr;
import static org.rug.simpletests.TestData.pure;

public class ASmellTrackerStateManagerTest extends ASmellTrackerTest {

    @Test
    void testStatefulness() throws IOException, ClassNotFoundException {
        var tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);

        var v1 = antlr.getVersionWith(1);
        var v2 = antlr.getVersionWith(2);
        var v3 = antlr.getVersionWith(3);
        var v4 = antlr.getVersionWith(4);

        var stateManager = new ASmellTrackerStateManager("test-data/output/states");

        tracker.track(antlr.getArchitecturalSmellsIn(v1), v1);
        tracker.track(antlr.getArchitecturalSmellsIn(v2), v2);
        assertEquals(antlrOracle.get(v2.getVersionString()), tracker.smellsLinked());

        stateManager.saveState(tracker);
        tracker = stateManager.loadState(antlr, v2);

        tracker.track(antlr.getArchitecturalSmellsIn(v3), v3);
        tracker.track(antlr.getArchitecturalSmellsIn(v4), v4);
        assertEquals(antlrOracle.get(v4.getVersionString()), tracker.smellsLinked());
    }

    @Test
    void testStatefulnessAntlr() throws IOException, ClassNotFoundException {
        testStatefulness(antlr, antlrOracle);
    }


    void testStatefulnessPure() throws IOException, ClassNotFoundException {
        testStatefulness(pure, pureOracle);
    }

    void testStatefulness(IProject project, Map<String, Long> oracle) throws IOException, ClassNotFoundException {
        var tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);
        int nVersions = (int)project.numberOfVersions();

        IntStream.range(1, nVersions/2).forEach(i -> {
            var version = project.getVersionWith(i);
            tracker.track(project.getArchitecturalSmellsIn(version), version);
            assertEquals(oracle.get(version.getVersionString()), tracker.smellsLinked());
        });

        var stateManager = new ASmellTrackerStateManager("test-data/output/states");
        stateManager.saveState(tracker);
        var recoveredTracker = stateManager.loadState(project, project.getVersionWith(nVersions/2 - 1));

        IntStream.range(nVersions/2, nVersions).forEach(i -> {
            var version = project.getVersionWith(i);
            recoveredTracker.track(project.getArchitecturalSmellsIn(version), version);
            assertEquals(oracle.get(version.getVersionString()), recoveredTracker.smellsLinked(),
                    String.format("Assertion error at i = %d version = %s", i, version.getVersionString()));
        });
    }

}
