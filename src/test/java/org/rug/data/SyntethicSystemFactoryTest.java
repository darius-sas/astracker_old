package org.rug.data;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.factories.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        expected = expected + 15;
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
            Set<Vertex> smellOfShape = (Set<Vertex>)(Set<?>)g.V()
                    .in(EdgeLabel.PARTOFCYCLE.toString()).hasLabel(VertexLabel.SMELL.toString()).as("smell")
                    .in().has("shapeType", shape)
                    .select("smell").toSet();
            for (Vertex smell : smellOfShape){
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
            }
            for (Vertex smell : smellOfShape){
                int affectedElements = g.V(smell).in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                        .out().hasLabel(VertexLabel.SMELL.toString())
                        .out().hasLabel(P.within(VertexLabel.CLASS.toString(), VertexLabel.PACKAGE.toString())).count().next().intValue();
                assertEquals(elements + elementsToAddtoEachSmell, affectedElements);
            }
        }

        Set<Vertex> notChainSmells = (Set<Vertex>)(Set<?>)g.V()
                .in(EdgeLabel.PARTOFCYCLE.toString()).hasLabel(VertexLabel.SMELL.toString()).as("smell")
                .in().has("shapeType", P.neq(CDSmell.Shape.CHAIN.toString()))
                .select("smell").toSet();

        for (Vertex smell : notChainSmells){
            cdEvolver.shapeShift(smell);
        }

        notChainSmells = (Set<Vertex>)(Set<?>)g.V()
                .in(EdgeLabel.PARTOFCYCLE.toString()).hasLabel(VertexLabel.SMELL.toString()).as("smell")
                .in().has("shapeType", P.neq(CDSmell.Shape.CHAIN.toString()))
                .select("smell").toSet();

        assertEquals(0, notChainSmells.size());

    }
}