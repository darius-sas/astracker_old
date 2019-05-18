package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
            List<ArchitecturalSmell> smellsInTheSystem = ArcanDependencyGraphParser.getArchitecturalSmellsIn(entry.getValue());
            Set<Vertex> smellVertices = entry.getValue().traversal().V()
                    .hasLabel(VertexLabel.SMELL.toString())
                    .toSet();

            Supplier<String> errMessage = () -> String.format("Error for version %s.", entry.getKey());

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
    void equals() {
        var systems = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr/");

        var smellsV1 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.5"));

        var smell1 = smellsV1.stream().filter(s -> s.getId() == 315).findFirst();
        var smell2 = smellsV1.stream().filter(s -> s.getId() == 305).findFirst();

        assertNotEquals(smell1.get(), smell2.get());

        var smellsV2 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.6"));
        var smell3 = smellsV2.stream().filter(s -> s.getId() == 315).findFirst();

        assertNotEquals(smell1.get(), smell3.get());
        assertNotEquals(smell2.get(), smell3.get());
    }
}