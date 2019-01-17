package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.*;

/**
 * Creates a syntethic dependecy graph representing a system, including AS.
 */
public class SyntethicSystemFactory {

    /**
     * The seed used to generate dummy nodes
     */
    public static long DUMMYSYSSEED = 123456;

    private Graph graph;
    private Random rng;

    public SyntethicSystemFactory(Graph graph){
        this.graph = graph;
        this.rng = new Random(DUMMYSYSSEED);
        this.graph.traversal().V().properties(CDSmell.VISITED_SMELL_NODE).drop().iterate();
    }


    /**
     * Randomly selects pairs of nodes that do not take part in any smell and adds tiny smells among them.
     * @param numerosity the number of tiny to add. If there are not enough free nodes, the maximum available is used.
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addTiny(int numerosity){
        if (numerosity < 1 )
            throw new IllegalArgumentException("At least one tiny should be created");

        ASEvolver evolver = new TinyCDEvolver(graph);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(numerosity * 2));

        return this;
    }

    /**
     * Adds a star smell by adding the given amount of nodes that will be used as leaves of the star.
     * @param elements the number of elements of the star and nodes to add.
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addStar(int elements){

        ASEvolver evolver = new StarCDEvolver(graph, elements);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(elements));

        return this;
    }

    /**
     * Adds a circle of the given length to the current graph. If elements are not enough, vertices are added.
     * @param length the length of the circle
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addCircle(int length){
        if (length < 2)
            throw new IllegalArgumentException("Lenght must be more than 2 in order to create circle CD.");

        ASEvolver evolver = new CircleCDEvolver(graph);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(length));
        return this;

    }

    /**
     * Creates a clique with the given amount of nodes.
     * @param nodes the number of nodes
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addClique(int nodes){
        if (nodes < 2)
            throw new IllegalArgumentException("Nodes must be more than 2 in order to create clique CD.");

        ASEvolver evolver = new CliqueCDEvolver(graph);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(nodes));

        return this;
    }

    /**
     * Creates a chain-shaped smell in the system.
     * @param length The length of the chain
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addChain(int length){

        ASEvolver evolver = new ChainCDEvolver(graph);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(length));

        return this;
    }

    /**
     * Selects a node and makes it a Hublike Dependency within the current graph.
     * @param in the number of in edges
     * @param out the numnber of out edges
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addHubLike(int in, int out){
        ASEvolver evolver = new HLEvolver(graph, in, ArchitecturalSmell.Level.PACKAGE);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(in+out+1));
        return this;
    }

    /**
     * Selects a node within the current graph and makes it an unstable dependency
     * @param badDep the number of bad dependencies
     * @return the smell factory to further extend the graph (Builder pattern)
     */
    public SyntethicSystemFactory addUnstable(int badDep){
        ASEvolver evolver = new HLEvolver(graph);
        evolver.addSmell(evolver.getVerticesNotAffectedBySmell(badDep + 1));
        return this;
    }

    public SyntethicSystemFactory evolveSmells(){
        return null;
    }

    /**
     * Gets the current instance representing the synthetic system.
     * @return the graph containing the changes made
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Creates a system graph with dummy nodes.
     * @param nodes the number of dummy nodes to add
     * @param maxOutDegree the maximun number of edges to add among the edges
     * @param edgeGenerationProbability the probability that for each package node in the given graph to generate an
     *                                  outgoing edge to another package node in the system.
     * @param seed the seed to use for randomly generating the edges
     * @return a graph representing a system
     */
    public static SyntethicSystemFactory createRandomSystemGraph(int nodes, int maxOutDegree, double edgeGenerationProbability, long seed){
        Graph graph = TinkerGraph.open();
        return new SyntethicSystemFactory(extendWithDummyNodes(graph, nodes, maxOutDegree, edgeGenerationProbability, seed));
    }

    /**
     * Creates a system graph with dummy nodes.
     * @param nodes the number of dummy nodes to add
     * @return a graph representing a system
     */
    public static SyntethicSystemFactory createRandomSystemGraph(int nodes){
        return new SyntethicSystemFactory(extendWithDummyNodes(TinkerGraph.open(), nodes));
    }

    /**
     * Create a star-shaped smell with the specified amount of 'leaves'.
     * @param leaves the number of leaves, min is 3
     * @return the graph representing the smell
     */
    public static Graph createStarSmell(int leaves){
        if (leaves < 3)
            throw new IllegalArgumentException("Star smells require at least 3 leaves.");

        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();

        Random rng = new Random();

        Vertex centre = g.addV(VertexLabel.PACKAGE.toString()).property("name", "org.dummy.centre").next();
        Vertex star = g.addV(VertexLabel.CYCLESHAPE.toString()).property("shapeType", CDSmell.Shape.STAR.toString()).property("smellId", rng.nextInt()).next();

        addLeaves(g, centre, star, leaves, "org.dummy.leaf");

        g.addE(EdgeLabel.ISCENTREOFSTAR.toString()).from(star).to(centre).next();

        return graph;
    }

    /**
     * Adds the given amount of leaves to the given star centre.
     * @param g The traversal of the graph.
     * @param centre The centre vertex.
     * @param star The star vertex.
     * @param leaves the number of leaves to add.
     * @param namePrefix The prefix to use for the name label of the generated vertices.
     * @return The vertices added as leaves.
     */
    private static Set<Vertex> addLeaves(GraphTraversalSource g, Vertex centre, Vertex star, int leaves, String namePrefix) {
        Set<Vertex> leafVertices = new HashSet<>();
        for (int i = 0; i < leaves; i++){
            Vertex leaf = g.addV(VertexLabel.PACKAGE.toString()).property("name", String.format("%s.%s", namePrefix, i)).next();
            leafVertices.add(leaf);
            g.addE(EdgeLabel.DEPENDSON.toString()).from(centre).to(leaf).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(leaf).to(centre).next();
            Vertex smell = g.addV(VertexLabel.SMELL.toString()).property("smellType", ArchitecturalSmell.Type.CD.toString()).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(leaf).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(centre).next();
            g.addE(EdgeLabel.PARTOFSTAR.toString()).from(star).to(smell).next();
        }
        return leafVertices;
    }

    /**
     * Add n extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the existing system.
     * @param graph the smell to extend with a dummy system
     * @param n the number of dummy nodes to add
     * @param maxOutDegree the maximun number of edges to add among the edges
     * @param edgeGenerationProbability the probability that for each package node in the given graph to generate an
     *                                  outgoing edge to another package node in the system.
     * @param seed the seed to use for randomly generating the edges
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(Graph graph, int n, int maxOutDegree, double edgeGenerationProbability, long seed){

        GraphTraversalSource g = graph.traversal();

        List<Vertex> newVertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++){
            newVertices.add(g.addV(VertexLabel.PACKAGE.toString())
                    .property("name", String.format("%s.%s", "org.dummy.dummySysNode", i)).next());
        }

        addDummyDependsOnEdges(graph, newVertices, g.V().toList(), maxOutDegree, edgeGenerationProbability, seed);

        return graph;
    }


    /**
     * Adds dummy edges between the given vertices.
     * @param graph the smell to extend with a dummy system
     * @param maxOutDegree the maximun number of edges to add among the edges
     * @param edgeGenerationProbability the probability that for each package node in the given graph to generate an
     *                                  outgoing edge to another package node in the system.
     * @param seed the seed to use for randomly generating the edges
     */
    private static void addDummyDependsOnEdges(Graph graph, Collection<Vertex> fromVertices, Collection<Vertex> toVertices, int maxOutDegree, double edgeGenerationProbability, long seed) {
        Random rng = new Random(seed);
        Random degreeRng = new Random(seed - 1L);

        GraphTraversalSource g = graph.traversal();

        g.V(fromVertices).forEachRemaining(from -> {
            int degree = degreeRng.nextInt(maxOutDegree + 1);
            for (int i = 0; i <= degree; i++) {
                GraphTraversal<Vertex, Vertex> packages = g.V(toVertices).hasLabel(VertexLabel.PACKAGE.toString());
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
    }

    /**
     * Add some extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the existing system.
     * @param graph the smell to extend with a dummy system
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(Graph graph){
        return extendWithDummyNodes(graph, graph.traversal().V().count().next().intValue() * 2);
    }

    /**
     * Add n extra dummy nodes to the current smell to simulate a system doubling the nodes currently available.
     * No edges are added between the elements of the existing system.
     * @param graph the smell to extend with a dummy system
     * @param n the number of dummy nodes to add
     * @return a graph representing a system
     */
    public static Graph extendWithDummyNodes(Graph graph, int n){
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

        Edge centreOfEdge = g.E().hasLabel(EdgeLabel.ISCENTREOFSTAR.toString()).next();
        Vertex centre = centreOfEdge.inVertex();
        Vertex star = centreOfEdge.outVertex();

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
