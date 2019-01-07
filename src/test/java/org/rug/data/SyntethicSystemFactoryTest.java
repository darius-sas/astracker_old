package org.rug.data;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.jupiter.api.Test;
import org.rug.data.smells.factories.SyntethicSystemFactory;

import java.io.IOException;

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

        factory.addStar(5);
        assertEquals(++expected, g.V().hasLabel(VertexLabel.SMELL.toString()).count().next().intValue());

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

}