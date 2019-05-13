package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.rug.data.Project;
import org.rug.data.characteristics.smells.*;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.io.IOException;
import java.util.*;
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
            var result = Double.parseDouble(strength.visit(ud));
            assertNotEquals(0, result);
            assertTrue(result <= 1);
            result = Double.parseDouble(instaGap.visit(ud));
            assertNotEquals(0, result);
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

    @Test
    void testAvrgInternalPathLength() throws IOException{
        var avrgPathLength = new AverageInternalPathLength();
        HLSmell hl = createMockHL();
        System.out.println(avrgPathLength.visit(hl));
    }

    HLSmell createMockHL() throws IOException{
        var graph = TinkerGraph.open();
        var g = graph.traversal();

        var n = 8;
        var affectedPackageV = g.addV(VertexLabel.PACKAGE.toString()).property("name", "test.packageAffected").next();
        var afferentPackageV = g.addV(VertexLabel.PACKAGE.toString()).property("name", "test.packageAfferent").next();
        var efferentPackageV = g.addV(VertexLabel.PACKAGE.toString()).property("name", "test.packageEfferent").next();

        Vertex s1 = null, s2 = null, e1 = null, e2 = null;

        List<Vertex> classesV = new ArrayList<>();
        for (int i = 0; i<= n; i++) {
            var v = g.addV(VertexLabel.CLASS.toString()).property("name", "class" + i).next();
            if (i == 0 || i == 1) {
                g.addE(EdgeLabel.BELONGSTO.toString()).from(v).to(afferentPackageV).next();
                g.addE(EdgeLabel.ISAFFERENTOF.toString()).from(v).to(affectedPackageV).next();
                s1 = s1 == null && i == 0 ? v : s1;
                s2 = s2 == null && i == 1 ? v : s2;
            }else if (i == n - 1 || i == n - 2){
                g.addE(EdgeLabel.BELONGSTO.toString()).from(v).to(efferentPackageV).next();
                g.addE(EdgeLabel.ISEFFERENTOF.toString()).from(v).to(affectedPackageV).next();
                e1 = e1 == null && i == n - 1 ? v : e1;
                e2 = e2 == null && i == n - 2 ? v : e2;
            }else {
                g.addE(EdgeLabel.BELONGSTO.toString()).from(v).to(affectedPackageV).next();
                classesV.add(v);
            }
        }

        var perms = generatePerm(classesV);
        var rng = new Random(0);
        Collections.shuffle(perms, rng);
        var edgeProb = 0.001;
        var nPaths = 0;
        var maxPaths = 3;
        // Randomly generate maxPaths paths between source and exit nodes
        for (var path : perms){
            if (nPaths <=maxPaths &&
                    (path.get(0).equals(s1) || path.get(0).equals(s2))){
                var to = rng.nextDouble() > edgeProb ? e1 : e2;
                for (int i = 1; i < path.size(); i++){
                    if (!path.get(i).equals(to)){
                        g.addE(EdgeLabel.DEPENDSON.toString()).from(path.get(i - 1)).to(path.get(i))
                                .property("Weight", rng.nextInt(10))
                                .next();
                    }else{
                        nPaths++;
                    }
                }
            }
        }

        var smell = g.addV(VertexLabel.SMELL.toString()).property("smellType", ArchitecturalSmell.Type.HL.toString()).next();
        smell.property("vertexType", "package");
        g.addE(EdgeLabel.HLAFFECTEDPACK.toString()).from(smell).to(affectedPackageV).next();
        g.addE(EdgeLabel.HLIN.toString()).from(smell).to(efferentPackageV).next();
        g.addE(EdgeLabel.HLOUT.toString()).from(smell).to(afferentPackageV).next();

        graph.io(IoCore.graphml()).writeGraph("test-data/hl-avrg.graphml");

        return new HLSmell(smell);
    }


    <E> List<List<E>> generatePerm(List<E> original) {
        if (original.size() == 0) {
            List<List<E>> result = new ArrayList<List<E>>();
            result.add(new ArrayList<E>());
            return result;
        }
        E firstElement = original.remove(0);
        List<List<E>> returnValue = new ArrayList<List<E>>();
        List<List<E>> permutations = generatePerm(original);
        for (List<E> smallerPermutated : permutations) {
            for (int index=0; index <= smallerPermutated.size(); index++) {
                List<E> temp = new ArrayList<E>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }
}