package org.rug.simpletests.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.web.System;
import org.rug.web.SystemController;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SystemTest {


    private final Graph graph = TinkerGraph.open();
    private final System system;

    public SystemTest() {
        var graphFile = "./test-data/output/trackASOutput/antlr/condensed-graph-consecOnly.graphml";
        graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
        system = new System(graph);
    }

    @Test
    void testConstruction() {
        assertFalse(system.getSmells().isEmpty());
        assertFalse(system.getComponents().isEmpty());
        assertFalse(system.getVersions().isEmpty());
    }

    @Test
    void testRequests(){
        assertFalse(system.getSmells(-3, 4).isEmpty());
        assertTrue(system.getSmells(4, 1).isEmpty());

        assertFalse(system.getComponents(-3, 4).isEmpty());
        assertTrue(system.getComponents(4, 1).isEmpty());

        assertTrue(system.getRecentStartingIndex() < system.getVersions().lastKey());

        var versions = new TreeMap<>(system.getVersions());
        system.getVersions().clear();
        assertEquals(0, system.getRecentStartingIndex());

        system.getVersions().put(versions.lastKey(), versions.lastEntry().getValue());
        assertTrue(system.getRecentStartingIndex() <= versions.lastKey());

        system.getVersions().putAll(versions);
    }

    @Test
    void testController(){
        var controller = new SystemController();

        assertEquals(system.getVersions(), controller.versions("antlr"));

        var components = controller.components("antlr", true, 0, 1);
        components.forEach(c -> {
            assertTrue(c.getSpanningVersions().contains(system.getVersions().lastKey()));
        });
        assertNotEquals(system.getComponents(), components);

        var smells = controller.smells("antlr", true, 0, 1);
        smells.forEach(s -> {
            assertTrue(s.getSpanningVersions().contains(system.getVersions().lastKey()));
        });
        assertNotEquals(system.getSmells(), smells);
    }
}
