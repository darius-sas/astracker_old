package org.rug.data;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.*;

/**
 * Creates graphs of smells
 */
public class SmellGraphFactory {

    /**
     * Create a star-shaped smell with the specified amount of 'leaves'.
     * @param leaves the number of leaves, min is 3
     * @return the graph representing the smell
     */
    public static SmellGraph createStarSmell(int leaves){
        if (leaves < 3)
            throw new IllegalArgumentException("Star smells require at least 3 leaves.");

        SmellGraph graph = new SmellGraph("star");
        GraphTraversalSource g = graph.traversal();

        Random rng = new Random();

        Vertex centre = g.addV(VertexLabel.PACKAGE.toString()).property("name", "org.dummy.centre").next();
        Vertex star = g.addV(VertexLabel.CYCLESHAPE.toString()).property("shapeType", "star").property("smellId", rng.nextInt()).next();

        addLeaves(g, centre, star, leaves, "org.dummy.leaf");

        g.addE(EdgeLabel.ISCENTREOF.toString()).from(star).to(centre).next();

        return graph;
    }

    /**
     * Adds the given amount of leaves to the given star centre.
     * @param g The traversal of the graph.
     * @param centre The centre vertex.
     * @param star The star vertex.
     * @param leaves the number of leaves to add.
     * @param namePrefix The prefix to use for the name label of the generated vertices.
     */
    private static void addLeaves(GraphTraversalSource g, Vertex centre, Vertex star, int leaves, String namePrefix) {
        for (int i = 0; i < leaves; i++){
            Vertex leaf = g.addV(VertexLabel.PACKAGE.toString()).property("name", String.format("%s.%s", namePrefix, i)).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(centre).to(leaf).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(leaf).to(centre).next();
            Vertex smell = g.addV(VertexLabel.SMELL.toString()).property("smellType", "cyclicDependency").next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(leaf).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(centre).next();
            g.addE(EdgeLabel.PARTOFSTAR.toString()).from(star).to(smell).next();
        }
    }

    /**
     * The seed used to generate dummy nodes
     */
    public static long DUMMYSYSSEED = 123456;

    /**
     * Add n extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the given smell.
     * @param graph the smell to extend with a dummy system
     * @param n the number of dummy nodes to add
     * @param maxOutDegree the maximun number of edges to add among the edges
     * @param edgeGenerationProbability the probability that for each package node in the given graph to generate an
     *                                  outgoing edge to another package node in the system.
     * @param seed the seed to use for randomly generating the edges
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(SmellGraph graph, int n, int maxOutDegree, double edgeGenerationProbability, long seed){

        GraphTraversalSource g = graph.traversal();
        Random rng = new Random(seed);
        Random degreeRng = new Random(seed - 1L);

        List<Vertex> newVertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++){
            newVertices.add(g.addV(VertexLabel.PACKAGE.toString())
                    .property("name", String.format("%s.%s", "org.dummy.dummySysLeaf", i)).next());
        }

        g.V(newVertices).forEachRemaining(from -> {
            int degree = degreeRng.nextInt(maxOutDegree + 1);
            for (int i = 0; i <= degree; i++) {
                GraphTraversal<Vertex, Vertex> packages = g.V().hasLabel(VertexLabel.PACKAGE.toString());
                packages.forEachRemaining(to -> {
                    if(rng.nextDouble() < edgeGenerationProbability){
                        g.addE(EdgeLabel.DEPENDSON.toString())
                                .from(from)
                                .to(to)
                                .next();
                    }
                });
            }
        });

        return graph;
    }

    /**
     * Add some extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the given smell.
     * @param graph the smell to extend with a dummy system
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(SmellGraph graph){
        return extendWithDummyNodes(graph, graph.traversal().V().count().next().intValue() * 2);
    }

    /**
     * Add n extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the given smell.
     * @param graph the smell to extend with a dummy system
     * @param n the number of dummy nodes to add
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(SmellGraph graph, int n){
        return extendWithDummyNodes(graph, n, 3, 0.01, DUMMYSYSSEED);
    }

    /**
     * Creates a new copy of the given graph containing the smell and extends it with a given number of dummy leaves.
     * @param graph the starting graph
     * @param n the number of leaves to add
     * @return a new graph with the given smell extended.
     */
    public static Graph simpleExtendEvolution(Graph graph, int n){

        //Graph cgraph = (Graph)graph.traversal().E().subgraph("copy").cap("copy").next();
        Graph cgraph = clone(graph);
        GraphTraversalSource g = cgraph.traversal();

        Edge centreOfEdge = g.E().hasLabel(EdgeLabel.ISCENTREOF.toString()).next();
        Vertex centre = centreOfEdge.outVertex();
        Vertex star = centreOfEdge.inVertex();

        addLeaves(g, centre, star, n, "org.dummy.extendedEvol");

        return cgraph;
    }

    /**
     * Clones the given graph and returns the clone.
     * @param src the graph to clone
     * @return the clone
     */
    public static Graph clone(Graph src){
        Graph des = TinkerGraph.open();
        Map<Object, Object> mapId = new HashMap<>();

        src.vertices().forEachRemaining(srcVert -> {
            Vertex toVertex = des.addVertex(srcVert.label());
            mapId.put(srcVert.id(), toVertex.id());
            srcVert.properties().forEachRemaining(srcVProp ->{
                toVertex.property(srcVProp.key(), srcVProp.value());
            });
        });

        src.edges().forEachRemaining(srcEdge -> {
            Object outVId = mapId.get(srcEdge.outVertex().id());
            Object inVId = mapId.get(srcEdge.inVertex().id());
            Edge toEdge = des.traversal().V().addE(srcEdge.label())
                    .from(des.vertices(outVId).next())
                    .to(des.vertices(inVId).next())
                    .next();
            srcEdge.properties().forEachRemaining(srcEProp -> {
                toEdge.property(srcEProp.key(), srcEProp.value());
            });
        });

        return des;
    }
}
