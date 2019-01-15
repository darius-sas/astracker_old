package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.factories.SyntethicSystemFactory;
import org.rug.data.VSetPair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
        int startingLeaves = 10;
        int extLeaves = 5;

        SyntethicSystemFactory f1 = SyntethicSystemFactory.createRandomSystemGraph(100);
        Graph g1 = f1.addChain(4).addTiny(4).addClique(4).addCircle(4).getGraph();
        SyntethicSystemFactory f2 = new SyntethicSystemFactory(SyntethicSystemFactory.clone(g1));



        Graph g2 = SyntethicSystemFactory.clone(g1);



        //g1.io(IoCore.graphml()).writeGraph("src/test/graphimages/starsmell.graphml");
        //g2.io(IoCore.graphml()).writeGraph("src/test/graphimages/starsmell-evolved.graphml");

        ASTracker tracker = new ASTracker();

        List<CDSmell> smellsInV1 = ArchitecturalSmell.getArchitecturalSmellsIn(g1).stream().filter(smell -> smell instanceof CDSmell).map(s -> (CDSmell)s).collect(Collectors.toList());
        List<ArchitecturalSmell> smellsInV2 = ArchitecturalSmell.getArchitecturalSmellsIn(g2);


        tracker.trackCD2(g1, g2, smellsInV1, smellsInV2);

        for (Map.Entry<ArchitecturalSmell, List<ArchitecturalSmell>> e : tracker.getVersionMap().entrySet()) {
            e.getValue().forEach(smell ->
                    assertTrue(smell.getAffectedElements().containsAll(e.getKey().getAffectedElements())));
        }

    }
}