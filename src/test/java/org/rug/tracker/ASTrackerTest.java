package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.factories.*;
import org.rug.data.VSetPair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ASTrackerTest {

    @Test
    void trackAS() throws IOException {
        int startingLeaves = 10;
        int extLeaves = 5;
        Graph g1 = SyntethicSystemFactory.extendWithDummyNodes(SyntethicSystemFactory.createStarSmell(startingLeaves), 3, 3, 0.01, SyntethicSystemFactory.DUMMYSYSSEED);
        Graph g2 = SyntethicSystemFactory.simpleExtendEvolution(g1, extLeaves);

        g1.io(IoCore.graphml()).writeGraph("src/test/graphimages/starsmell.graphml");
        g2.io(IoCore.graphml()).writeGraph("src/test/graphimages/starsmell-evolved.graphml");

        ASTracker tracker = new ASTracker();

        Map<String, Map<Vertex, List<VSetPair>>> mappingsPerType = tracker.trackCD(g1, g2);

        for (Map<Vertex, List<VSetPair>> mapping : mappingsPerType.values()) {
            for (List<VSetPair> pairs : mapping.values())
                for (VSetPair entry : pairs) {
                    assertEquals(startingLeaves + 1, entry.getA().size());
                    assertEquals(startingLeaves + extLeaves + 1, entry.getB().size());
                }
        }

        // Invert graphs to check whether the algorithm works with smells that lose components
        mappingsPerType = tracker.trackCD(g2, g1);

        for (Map<Vertex, List<VSetPair>> mapping : mappingsPerType.values())
            for (List<VSetPair> pairs : mapping.values())
                for (VSetPair entry : pairs){
                    assertEquals(startingLeaves + 1, entry.getB().size());
                    assertEquals(startingLeaves + extLeaves + 1, entry.getA().size());
                }
    }

    @Test
    void trackAS2() throws IOException{
        SyntethicSystemFactory f1 = SyntethicSystemFactory.createRandomSystemGraph(100);
        Graph g1 = f1.addChain(4).addClique(4).addCircle(4).getGraph();
        SyntethicSystemFactory f2 = new SyntethicSystemFactory(SyntethicSystemFactory.clone(g1));
        Graph g2 = f2.getGraph();
        f2.addStar(4);

        ChainCDEvolver chainEv = new ChainCDEvolver(g2);
        StarCDEvolver starEv = new StarCDEvolver(g2);
        CircleCDEvolver circEv = new CircleCDEvolver(g2);
        List<CDSmell> smellsInV1 = ArchitecturalSmell.getArchitecturalSmellsIn(g1).stream().filter(smell -> smell instanceof CDSmell).map(s -> (CDSmell)s).collect(Collectors.toList());

        int elementsToAddtoEachSmell = 3;
        for (CDSmell.Shape shape : CDSmell.Shape.values()){
            Set<ArchitecturalSmell> smellOfShape = smellsInV1.stream().filter(smell -> smell.getShape().equals(shape)).collect(Collectors.toSet());
            for (ArchitecturalSmell smell : smellOfShape){
                switch (shape.toString()) {
                    case "chain":
                        chainEv.addElements(smell, elementsToAddtoEachSmell);
                        break;
                    case "star":
                        starEv.addElements(smell, elementsToAddtoEachSmell);
                        break;
                    case "circle":
                        circEv.addElements(smell, elementsToAddtoEachSmell);
                        break;
                }
            }
        }

        List<ArchitecturalSmell> smellsInV2 = ArchitecturalSmell.getArchitecturalSmellsIn(g2);
        ASTracker tracker = new ASTracker();

        tracker.trackCD2(g1, g2, smellsInV1, smellsInV2);

        tracker.getVersionMap().forEach((key, value) -> {
            System.out.println("Key:\n" + key);
            value.forEach(smell -> System.out.println("Val:\n" + smell));
            System.out.println("---\n");
        });

        // Tracker seems to work fine. However the addition of smells elements seems bugged as fuck
        for (Map.Entry<ArchitecturalSmell, List<ArchitecturalSmell>> e : tracker.getVersionMap().entrySet()) {
            e.getValue().forEach(smell ->
                    assertTrue(smell.getAffectedElements().containsAll(e.getKey().getAffectedElements())));
        }

    }
}