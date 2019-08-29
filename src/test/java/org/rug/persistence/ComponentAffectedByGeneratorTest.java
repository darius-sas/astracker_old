package org.rug.persistence;

import org.junit.jupiter.api.Test;
import org.rug.data.project.Project;
import org.rug.tracker.ASmellTracker;

import java.io.IOException;

class ComponentAffectedByGeneratorTest {

    @Test
    void testAffectedElements() throws IOException {
        var project = new Project("antlr");
        project.addGraphMLs("./test-data/output/arcanOutput/antlr/");

        var gen = new ComponentAffectedByGenerator("./test-data/output/trackASOutput/antlr/affectedComponents.csv");
        var tracker = new ASmellTracker();
        project.forEach(v -> {
            var smells = project.getArchitecturalSmellsIn(v);
            System.out.println("Smells " + smells.size());
            tracker.track(smells, v);
            System.out.println("Tracking version " + v);
        });
        gen.accept(tracker);
        PersistenceWriter.writeCSV(gen);
    }
}