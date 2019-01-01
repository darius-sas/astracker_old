package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.SmellGraphFactory;
import org.rug.data.VSetPair;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ASTrackerTest {

    @Test
    void trackAS() throws IOException {
        int startingLeaves = 10;
        int extLeaves = 5;
        Graph g1 = SmellGraphFactory.extendWithDummyNodes(SmellGraphFactory.createStarSmell(startingLeaves), 3, 3, 0.01, SmellGraphFactory.DUMMYSYSSEED);
        Graph g2 = SmellGraphFactory.simpleExtendEvolution(g1, extLeaves);

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

}