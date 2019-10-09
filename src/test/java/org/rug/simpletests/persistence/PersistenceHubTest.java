package org.rug.simpletests.persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceHub;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.tracker.ASmellTracker;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.rug.simpletests.TestData.*;

@Tag("unitTests")
public class PersistenceHubTest {


    ASmellTracker getTracker() throws IOException, ClassNotFoundException {

        ASmellTracker tracker;
            tracker = new ASmellTracker();
            antlr.forEach(version -> {
                var smells = antlr.getArchitecturalSmellsIn(version);
                smells.forEach(ArchitecturalSmell::calculateCharacteristics);
                tracker.track(smells, version);
            });

        return tracker;
    }

    @Test
    void testSendTo() throws IOException {
        PersistenceHub.clearAll();
        var outfile = Paths.get(trackASOutputDir,antlr.getName(), "smells-characteristics.csv");
        PersistenceHub.register(new SmellCharacteristicsGenerator(outfile.toString(), antlr));
        var tracker = new ASmellTracker();
        var v1 = antlr.getVersionWith(1);
        var v2 = antlr.getVersionWith(2);
        tracker.track(antlr.getArchitecturalSmellsIn(v1), v1);
        tracker.track(antlr.getArchitecturalSmellsIn(v2), v2);
        PersistenceHub.sendToAndWrite(SmellCharacteristicsGenerator.class, tracker);
        PersistenceHub.closeAll();
        var lines = Files.readAllLines(outfile);
        assertEquals(7, lines.size());
        outfile.toFile().delete();
        PersistenceHub.clearAll();
    }

    @Test
    void testPerformanceCharacteristicsFileWriting() throws IOException, ClassNotFoundException {
        PersistenceHub.clearAll();
        var outfile = Paths.get(trackASOutputDir,antlr.getName(), "smells-characteristics.csv");
        PersistenceHub.register(new SmellCharacteristicsGenerator(outfile.toString(), antlr));

        var tracker = getTracker();

        var start = System.nanoTime();
        PersistenceHub.sendToAndWrite(SmellCharacteristicsGenerator.class, tracker);
        PersistenceHub.closeAll();
        var end = System.nanoTime();
        var elapsedSecs = (end - start) / 1e9d;
        System.out.println(String.format("Elapsed saving time: %.3f", elapsedSecs));

        var lines = Files.readAllLines(outfile);
        System.out.println(String.format("Elapsed saving time per record: %.5f", elapsedSecs/lines.size()));
        assertEquals(2328, lines.size());
        assertTrue(elapsedSecs < 3.1); // 3.7 with single-off writing
        PersistenceHub.clearAll();
    }
}
