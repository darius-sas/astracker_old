package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.Project;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.rug.simpletests.TestData.*;

public class ArchitecturalSmellTest {

    @Test
    void testASDataStructure(){

        for (var version : antlr.versions()){
            List<ArchitecturalSmell> smellsInTheSystem = antlr.getArchitecturalSmellsIn(version);
            Graph graph = version.getGraph();
            Set<Vertex> smellVertices = graph.traversal().V()
                    .hasLabel(VertexLabel.SMELL.toString())
                    .toSet();

            Supplier<String> errMessage = () -> String.format("Error for version %s.", version);

            Supplier<Stream<Vertex>> smellVertexStream = () -> smellVertices.stream().filter(vertex -> !vertex.value("smellType").equals("multipleAS") && !vertex.property(CDSmell.VISITED_SMELL_NODE).orElse("false").equals("true"));

            // check all the smell were parsed using the id
            assertEquals(smellVertexStream.get()
                            .map(vertex -> Long.parseLong(vertex.id().toString()))
                            .collect(Collectors.toSet()),
                    smellsInTheSystem.stream()
                            .map(ArchitecturalSmell::getId)
                            .collect(Collectors.toSet()),
                    errMessage);

            // Check smell type matches
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
            // Check CD shape matches
            assertEquals(smellVertexStream.get()
                            .filter(vertex -> vertex.value("smellType").equals(ArchitecturalSmell.Type.CD.toString())
                                && vertex.edges(Direction.IN).hasNext())
                            .sorted(Comparator.comparing(vertex -> Long.valueOf(vertex.id().toString())))
                            .map(vertex -> vertex.graph().traversal().V(vertex)
                                    .in().hasLabel(VertexLabel.CYCLESHAPE.toString()).values("shapeType").next().toString())
                            .collect(Collectors.toList()),
                    smellsInTheSystem.stream()
                            .filter(as -> as.getType().equals(ArchitecturalSmell.Type.CD) && !((CDSmell)as).getShape().equals(CDSmell.Shape.UNCLASSIFIED))
                            .sorted(Comparator.comparing(ArchitecturalSmell::getId))
                            .map(as -> ((CDSmell) as).getShape().toString())
                            .collect(Collectors.toList()),
                    errMessage);
            // This does not work for star smells, need to fix that in the smell parsing
        }

    }

    @Test
    void testEquals() {

        var smellsV1 = antlr.getArchitecturalSmellsIn("2.7.2");
        var smellsV2 = antlr.getArchitecturalSmellsIn("2.7.3");


        var smell1 = smellsV1.stream().filter(s -> s.getId() == 4157).findFirst();
        var smell2 = smellsV1.stream().filter(s -> s.getId() == 4274).findFirst();

        assertNotEquals(smell1.get(), smell2.get());

        var smell3 = smellsV2.stream().filter(s -> s.getId() == 4557).findFirst();

        assertNotEquals(smell1.get(), smell3.get());
        assertNotEquals(smell2.get(), smell3.get());
    }
}