package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Test;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmellCPP;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.rug.simpletests.TestData.pure;

public class ArchitecturalSmellCppParsingTest {
    @Test
    void testCDParsing(){
        var g = pure.getVersion("1.0.0.0").getGraph().traversal();
        var cds = g.V().hasLabel("smell").has("smellType", ArchitecturalSmell.Type.CD.toString()).toSet();
        var smells = new ArrayList<>();
        cds.forEach(smell -> {
            var s = new CDSmellCPP(smell);
            assertNotEquals(0, s.getAffectedElements().size());
            System.out.println(s);
            smells.add(s);
        });
    }
}
