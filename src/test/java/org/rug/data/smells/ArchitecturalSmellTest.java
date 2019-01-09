package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.VertexLabel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArchitecturalSmellTest {

    @Test
    void testASDataStructure(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr/");

        for (Map.Entry<String, Graph> entry : versionedSystem.entrySet()){
            List<ArchitecturalSmell> smellsInTheSystem = ArchitecturalSmell.getArchitecturalSmellsIn(entry.getValue());
            Set<Vertex> smellVertices = entry.getValue().traversal().V()
                    .hasLabel(VertexLabel.SMELL.toString())
                    .toSet();
            assertEquals(smellVertices.size(), smellsInTheSystem.size()); // This does not work for star smells, need to fix that in the smell parsing
        }

    }
}