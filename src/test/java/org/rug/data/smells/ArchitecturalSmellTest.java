package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArchitecturalSmellTest {

    private final static Logger logger = LoggerFactory.getLogger(ArchitecturalSmellTest.class);

    @Test
    void testASDataStructure(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr/");

        for (Map.Entry<String, Graph> entry : versionedSystem.entrySet()){
            List<ArchitecturalSmell> smellsInTheSystem = ArchitecturalSmell.getArchitecturalSmellsIn(entry.getValue());
            Set<Vertex> smellVertices = entry.getValue().traversal().V()
                    .hasLabel(VertexLabel.SMELL.toString())
                    .toSet();

            Supplier<String> errMessage = () -> String.format("Error for version %s.", entry.getKey());

            Supplier<Stream<Vertex>> smellVertexStream = () -> smellVertices.stream().filter(vertex -> !vertex.value("smellType").equals("multipleAS"));

            assertEquals(smellsInTheSystem.stream()
                            .map(ArchitecturalSmell::getId)
                            .collect(Collectors.toSet()),
                    smellVertexStream.get()
                             .map(vertex -> Long.parseLong(vertex.id().toString()))
                             .collect(Collectors.toSet()),
                    errMessage);

            assertEquals(smellVertexStream.get()
                            .sorted(Comparator.comparing(vertex -> Long.valueOf(vertex.id().toString())))
                            .map(vertex -> vertex.value("smellType"))
                            .collect(Collectors.toList()),
                    smellsInTheSystem.stream()
                            .sorted(Comparator.comparing(ArchitecturalSmell::getId))
                            .map(ArchitecturalSmell::getType)
                            .map(Objects::toString)
                            .collect(Collectors.toList()),
                    errMessage);

            assertEquals(smellVertexStream.get()
                            .filter(vertex -> vertex.value("smellType").equals(ArchitecturalSmell.Type.CD.toString()))
                            .sorted(Comparator.comparing(vertex -> Long.valueOf(vertex.id().toString())))
                            .map(vertex -> vertex.graph().traversal().V(vertex).out().hasLabel(VertexLabel.CYCLESHAPE.toString()).ge("smellShape"))
                            .collect(Collectors.toList()),
                    smellsInTheSystem.stream()

                            .sorted(Comparator.comparing(ArchitecturalSmell::getId))
                            .map(ArchitecturalSmell::getType)
                            .map(Objects::toString)
                            .collect(Collectors.toList()),
                    errMessage)
            // This does not work for star smells, need to fix that in the smell parsing
        }

    }
}