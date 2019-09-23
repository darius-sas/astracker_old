package org.rug.simpletests.persistence;

import org.junit.jupiter.api.Test;
import org.rug.persistence.PersistenceHub;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.tracker.ASmellTracker;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.rug.simpletests.TestData.*;

public class PersistenceHubTest {

    @Test
    void testSendTo() throws IOException {
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
    }
}
