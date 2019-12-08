package org.rug.simpletests.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.rug.web.SmellList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmellListTest {


    private final Graph graph = TinkerGraph.open();

    public SmellListTest() {
        var graphFile = "./test-data/output/trackASOutput/antlr/condensed-graph-consecOnly.graphml";
        graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
    }

    @Test
    void testConstruction() throws JsonProcessingException {
        var smellList = new SmellList(graph);
        assertFalse(smellList.getSmellList().isEmpty());
        assertFalse(smellList.getVersions().isEmpty());
        ObjectMapper mapper = new ObjectMapper();
        var jsonList = mapper.writeValueAsString(smellList);
        assertFalse(jsonList.isEmpty());
    }

}
