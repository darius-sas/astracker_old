package org.rug.simpletests.tracker;

import org.junit.jupiter.api.Test;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.rug.simpletests.TestData.antlr;

public class ASmellTrackerSerialization extends ASmellTrackerTest {

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        ASmellTracker smellTracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);
        var v1 = antlr.getVersion("3.1");
        var v2 = antlr.getVersion("3.2");
        var v3 = antlr.getVersion("3.3");
        var v4 = antlr.getVersion("3.4");

        smellTracker.track(antlr.getArchitecturalSmellsIn(v1), v1);
        smellTracker.track(antlr.getArchitecturalSmellsIn(v2), v2);
        //assertEquals(antlrOracle.get(v2.getVersionString()), smellTracker.smellsLinked());

        var serFile = new File("astracker.seo");
        serFile.deleteOnExit();

        var outfs = new FileOutputStream(serFile);
        var objos = new ObjectOutputStream(outfs);

        objos.writeObject(smellTracker);
        objos.flush();
        objos.close();

        var infs = new FileInputStream(serFile);
        var objin = new ObjectInputStream(infs);
        ASmellTracker serializedSmellTracker = (ASmellTracker)objin.readObject();
        objin.close();
        assertNotNull(serializedSmellTracker);
    }
}
