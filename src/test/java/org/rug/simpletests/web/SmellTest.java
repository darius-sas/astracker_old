package org.rug.simpletests.web;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.rug.tracker.ASmellTracker;
import org.rug.web.Smell;

import static org.junit.jupiter.api.Assertions.*;

public class SmellTest {

    private final Graph graph = TinkerGraph.open();

    public SmellTest() {
        var graphFile = "./test-data/output/trackASOutput/antlr/condensed-graph-consecOnly.graphml";
        graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
    }

    @Test
    void testConstructor(){
        var smellID = 86;
        var smellVertex = graph.traversal().V().has(ASmellTracker.UNIQUE_SMELL_ID, smellID).tryNext().orElse(null);
        assertNotNull(smellVertex);
        var smell = new Smell(smellVertex);
        assertEquals(smellID, smell.getId());
        assertFalse(smell.getAffectedComponents().isEmpty());
        assertFalse(smell.getAffectedVersions().isEmpty());
        assertFalse(smell.getCharacteristics().isEmpty());
    }
}
