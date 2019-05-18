package org.rug.data;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.factories.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SyntethicSystemFactoryTest {

    @Test
    void testStarFactory() throws IOException{
        int leaves = 100;
        Graph graph = SyntethicSystemFactory.createStarSmell(leaves);
        int packageCount = getVertexCountLabel(graph.traversal(), VertexLabel.PACKAGE);

        int edgeCount = graph.traversal().E().count().next().intValue();

        assertEquals(leaves + 1, packageCount);
        assertTrue(edgeCount > 0);
        graph.io(IoCore.graphml()).writeGraph("src/test/graphimages/starsample.graphml");

        Graph graph2 = SyntethicSystemFactory.extendWithDummyNodes(graph);
        int packageCount2 = getVertexCountLabel(graph2.traversal(), VertexLabel.PACKAGE);
        assertTrue(packageCount2 > packageCount);
        int edgeCount2 = graph2.traversal().E().count().next().intValue();
        assertTrue(edgeCount2 > edgeCount);

        int smellTypeCount = getVertexCountLabel(graph.traversal(), VertexLabel.SMELL);
        int smellTypeCount2 = getVertexCountLabel(graph2.traversal(), VertexLabel.SMELL);

        assertTrue(smellTypeCount == smellTypeCount2);

        graph2.io(IoCore.graphml()).writeGraph("src/test/graphimages/extendedstarsample.graphml");
    }

    private int getVertexCountLabel(GraphTraversalSource traversal, VertexLabel vertexLabel) {
        return traversal.V().hasLabel(vertexLabel.toString()).count().next().intValue();
    }

    @Test
    void simpleExtendEvolution() throws IOException{
        int leaves = 10;
        Graph graph = SyntethicSystemFactory.extendWithDummyNodes(SyntethicSystemFactory.createStarSmell(leaves));
        int extendedLeaves = 5;
        Graph extendedGraph = SyntethicSystemFactory.simpleExtendEvolution(graph, extendedLeaves);

        assertFalse(extendedGraph == graph);

        assertEquals(getVertexCountLabel(graph.traversal(), VertexLabel.PACKAGE) + extendedLeaves,
                getVertexCountLabel(extendedGraph.traversal(), VertexLabel.PACKAGE));

        graph.io(IoCore.graphml()).writeGraph("src/test/graphimages/extendedstarsample.graphml");
        extendedGraph.io(IoCore.graphml()).writeGraph("src/test/graphimages/evolvedstar.graphml");

    }

    @Test
    void smellGraphBuilderTest() throws IOException{
        SyntethicSystemFactory factory = SyntethicSystemFactory.createRandomSystemGraph(100);
        GraphTraversalSource g = factory.getGraph().traversal();

        int expected = 0;
        assertEquals(expected, factory.getGraph().traversal().V().hasLabel(VertexLabel.SMELL.toString()).count().next());

        factory.addTiny(1);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addChain(3);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addStar(15);
        expected = expected + 15 - 1; // the central element does not have a smell node attached
        assertEquals(expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addClique(5);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addCircle(7);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addHubLike(5, 10);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        factory.addUnstable(5);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

        g.getGraph().io(IoCore.graphml()).writeGraph("src/test/graphimages/smells-graph.graphml");
    }

    @SuppressWarnings("unchecked")
    @Test
    void smellEvolverTest() throws IOException{
        SyntethicSystemFactory factory = SyntethicSystemFactory.createRandomSystemGraph(50);
        GraphTraversalSource g = factory.getGraph().traversal();

        int smellsToAddperType = 2;
        int elements = 4;

        for (int i = 0; i < smellsToAddperType; i++) {
            factory.addChain(elements)
                    .addCircle(elements)
                    .addClique(elements)
                    .addStar(elements);
        }

        List<ArchitecturalSmell> asInSystem = ArcanDependencyGraphParser.getArchitecturalSmellsIn(factory.getGraph());

        assertTrue(asInSystem.size() == smellsToAddperType * 4, "Actual size is " + asInSystem.size());

        ChainCDEvolver cdEvolver = new ChainCDEvolver(factory.getGraph());
        CircleCDEvolver circEvolver = new CircleCDEvolver(factory.getGraph());
        CliqueCDEvolver cliqueCDEvolver = new CliqueCDEvolver(factory.getGraph());
        StarCDEvolver starEvolver = new StarCDEvolver(factory.getGraph());

        List<ASEvolver> evolvers = new ArrayList<>();
        evolvers.add(cdEvolver);
        evolvers.add(circEvolver);
        evolvers.add(cliqueCDEvolver);
        evolvers.add(starEvolver);


        int elementsToAddtoEachSmell = 3;
        for (CDSmell.Shape shape : CDSmell.Shape.values()){
            Set<ArchitecturalSmell> smellOfShape = asInSystem.stream().filter(smell -> smell instanceof CDSmell && ((CDSmell) smell).getShape().equals(shape)).collect(Collectors.toSet());
            for (ArchitecturalSmell smell : smellOfShape){
                assertEquals(elements, g.V(smell.getSmellNodes()).out().hasLabel("package").toSet().size());
                switch (shape.toString()) {
                    case "chain":
                        cdEvolver.addElements(smell, elementsToAddtoEachSmell);
                        break;
                    case "star":
                        starEvolver.addElements(smell, elementsToAddtoEachSmell);
                        break;
                    case "circle":
                        circEvolver.addElements(smell, elementsToAddtoEachSmell);
                        break;
                    case "clique":
                        circEvolver.addElements(smell, elementsToAddtoEachSmell);
                }
                assertEquals(elements + elementsToAddtoEachSmell, g.V(smell.getSmellNodes()).out().in().hasLabel("smell").out().hasLabel("package").toSet().size(),
                        String.format("Error on smell shape: %s \t Smell Vertices %s", shape, smell.getSmellNodes()));
            }
        }

        asInSystem = ArcanDependencyGraphParser.getArchitecturalSmellsIn(factory.getGraph());

        for (ArchitecturalSmell smell : asInSystem){
            if (smell instanceof CDSmell && ((CDSmell) smell).getShape().equals(CDSmell.Shape.STAR))
                continue;
            int affectedElements = smell.getAffectedElements().size();
            assertEquals(elements + elementsToAddtoEachSmell, affectedElements,
                    String.format("Smell shape: %s \t elements: %s", ((CDSmell)smell).getShape(), smell.getAffectedElements()));
        }

        Set<ArchitecturalSmell> notChainSmells = asInSystem.stream().filter(smell -> smell instanceof CDSmell && !((CDSmell) smell).getShape().equals(CDSmell.Shape.CHAIN)).map(s -> (CDSmell)s).collect(Collectors.toSet());

        for (ArchitecturalSmell smell : notChainSmells){
            cdEvolver.shapeShift((CDSmell)smell);
        }

        List<ArchitecturalSmell> newSmellsInSystem = ArcanDependencyGraphParser.getArchitecturalSmellsIn(factory.getGraph());

        assertEquals(0, newSmellsInSystem.stream().filter(smell -> !(((CDSmell)smell).getShape() == CDSmell.Shape.CHAIN)).count());
        assertEquals(smellsToAddperType * 4, newSmellsInSystem.stream().filter(smell -> (((CDSmell)smell).getShape() == CDSmell.Shape.CHAIN)).count());


    }
}