package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArcanDependencyGraphParserTest {

    @Test
    void parseGraphML() {
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr");

        List<String> versions = Arrays.asList("2.7.1", "2.7.2", "2.7.5", "2.7.6", "2.7.7", "3.0",
                "3.0.1", "3.1", "3.1.1", "3.1.2", "3.1.3", "3.2", "3.3", "3.4", "3.5");

        assertTrue(versionedSystem.keySet().containsAll(versions));

    }
}