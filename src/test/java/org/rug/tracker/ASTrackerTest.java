package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.SmellGraphFactory;

import static org.junit.jupiter.api.Assertions.*;

class ASTrackerTest {

    @Test
    void trackAS() {
        Graph g1 = SmellGraphFactory.extendWithDummyNodes(SmellGraphFactory.createStarSmell(10));
        Graph g2 = SmellGraphFactory.simpleExtendEvolution(g1, 5);

        int count = ASTracker.trackCD(g1, g2);
        assertTrue(count > 1);
    }

}