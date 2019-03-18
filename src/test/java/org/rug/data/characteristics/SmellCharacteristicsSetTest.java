package org.rug.data.characteristics;

import org.junit.jupiter.api.Test;
import org.rug.data.Project;
import org.rug.data.characteristics.smells.*;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.UDSmell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SmellCharacteristicsSetTest {

    Project project;

    public SmellCharacteristicsSetTest() throws IOException{
        project = new Project("antlr");
        project.addGraphMLs("./qualitas-corpus/output/arcanOutput/antlr");
    }

    @Test
    void testUDCharacteristics() {
        var strength = new Strength();
        var instaGap = new InstabilityGap();

        var uds = project.getArchitecturalSmellsIn("3.3").stream()
                .filter(a -> a.getType() == ArchitecturalSmell.Type.UD)
                .map(a -> (UDSmell)a)
                .collect(Collectors.toList());

        assertTrue(uds.size() > 0);

        uds.forEach(ud -> {
            var result = strength.visit(ud);
            assertNotEquals(0, Double.parseDouble(result));
            result = instaGap.visit(ud);
            assertNotEquals(0, Double.parseDouble(result));
        });

    }

    @Test
    void testCDCharacteristics(){
        var avrgWeight = new AverageEdgeWeight();
        var numOfEdges = new NumberOfEdges();
        var numOfInher = new NumberOfInheritanceEdges();

        var cds = project.getArchitecturalSmellsIn("3.3").stream()
                .filter(a -> a.getType() == ArchitecturalSmell.Type.CD)
                .map(a -> (CDSmell)a)
                .collect(Collectors.toList());

        assertTrue(cds.size() > 0);

        boolean resultB = false;
        for (CDSmell cd : cds) {
            var result = avrgWeight.visit(cd);
            assertNotEquals(0, Double.parseDouble(result));

            result = numOfEdges.visit(cd);
            assertNotEquals(0, Double.parseDouble(result));

            resultB = resultB || Double.parseDouble(numOfInher.visit(cd)) > 0;
        }
        assertTrue(resultB);
    }

    @Test
    void testOverlapRatio(){
        var overlapCD = new OverlapRatio(ArchitecturalSmell.Type.CD);
        var overlapUD = new OverlapRatio(ArchitecturalSmell.Type.UD);
        var overlapHL = new OverlapRatio(ArchitecturalSmell.Type.HL);
        var overlapAll= new OverlapRatio(null);

        var as = project.getArchitecturalSmellsIn("3.4");
        for (ArchitecturalSmell a : as){
            var olR = Double.parseDouble(a.accept(overlapAll));
            var olC = Double.parseDouble(a.accept(overlapCD));
            var olH = Double.parseDouble(a.accept(overlapHL));
            var olU = Double.parseDouble(a.accept(overlapUD));
            assertTrue(olR >= olC);
            assertTrue(olR >= olH);
            assertTrue(olR >= olU);
            System.out.printf("Smell: %s\tOlR: %.2f\tOlC: %.2f\tOlH: %.2f\tOlU: %.2f\n", a, olR, olC, olH, olU);
        }
    }

}