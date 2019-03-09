package org.rug.persistence;

import org.junit.jupiter.api.Test;
import org.rug.data.Project;
import org.rug.tracker.ASmellTracker;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ComponentAffectedByGeneratorTest {

    @Test
    void testAffectedElements() throws IOException {
        var project = new Project("antlr");
        project.addGraphMLs("./qualitas-corpus/output/arcanOutput/antlr/");

        var gen = new ComponentAffectedByGenerator("./qualitas-corpus/output/trackASOutput/antlr/affectedComponents.csv");
        var tracker = new ASmellTracker();
        project.getVersionedSystem().forEach( (v, t) -> {
            var smells = project.getArchitecturalSmellsIn(v);
            System.out.println("Smells " + smells.size());
            tracker.track(smells, v);
            System.out.println("Tracking version " + v);
        });
        gen.accept(tracker);
        PersistenceWriter.writeCSV(gen);
    }
}