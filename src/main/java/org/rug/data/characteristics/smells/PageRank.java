package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * This characteristic computes the PageRank of the components of a given smell. The algorithm
 * navigates the {@link EdgeLabel}.DEPENDSON and {@link EdgeLabel}.PACKAGEISAFFERENTOF labels.
 */
public class PageRank extends AbstractSmellCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PageRank.class);

    private final Function<DoubleStream, Double> rankSelector;


    /**
     * Sets up the name of this smell characteristic.
     * The page rank of the smell will be the maximum value among its affected components.
     */
    public PageRank() {
        super("pageRankMax");
        this.rankSelector = (x) -> x.max().orElse(0);
    }

    /**
     * Initialiazes a characteristic calculator with the given name and aggregation function
     * @param name the name of the characteristic
     * @param rankSelector the aggregation function to use
     */
    public PageRank(String name, Function<DoubleStream, Double> rankSelector){
        super(name);
        this.rankSelector = rankSelector;
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the CD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(CDSmell smell) {
        return visitInternal(smell);
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(HLSmell smell) {
        return visitInternal(smell);
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the UD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(UDSmell smell) {
        return visitInternal(smell);
    }

    @Override
    public String visit(GCSmell smell) {
        return visitInternal(smell);
    }

    /**
     * Computes the pagerank on the affected elements of the smell and returns the maximum value.
     * This behaviour was suggested by Roveda et al. 2018
     * @param smell the smell to calculate the page rank of.
     * @return a string containing the double value computed.
     */
    private String visitInternal(ArchitecturalSmell smell){

        var pageRank = 0d;
        Graph g = getPageRankGraph(smell);
        Set<String> affectedElements = smell.getAffectedElements()
                .stream().map(vertex -> vertex.value("name").toString())
                .collect(Collectors.toSet());
        pageRank = rankSelector.apply(g.traversal().V().has("name", P.within(affectedElements))
                .values("centrality").toSet()
                .stream().mapToDouble(value -> (Double)value));

        return String.valueOf(pageRank);
    }

    /**
     * Clones the given graph and returns the clone.
     * @param src the graph to clone
     * @return the clone
     */
    private static Graph clone(Graph src, List<String> vLabels, List<String> eLabels){
        Graph des = TinkerGraph.open();
        Map<Object, Object> mapId = new HashMap<>();

        src.traversal().V().hasLabel(P.within(vLabels)).forEachRemaining(srcVert -> {
            Vertex toVertex = des.addVertex(srcVert.label());
            mapId.put(srcVert.id(), toVertex.id());
            srcVert.properties().forEachRemaining(srcVProp ->{
                toVertex.property(srcVProp.key(), srcVProp.value());
            });
        });

        src.traversal().V().hasLabel(P.within(vLabels)).bothE(eLabels.toArray(new String[0])).forEachRemaining(srcEdge -> {
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


    private static Map<Graph, Map<AffectedDesign.Level, Graph>> cachedPageRankGraphs = new HashMap<>();

    private static Graph getPageRankGraph(ArchitecturalSmell smell){
        Graph smellGraph = smell.getAffectedGraph();
        if (!cachedPageRankGraphs.containsKey(smellGraph)){
            var innerMap = new HashMap<AffectedDesign.Level, Graph>();

            Graph explodedGraph = explodeGraph(smellGraph);

            var programClasses = PageRankVertexProgram
                    .build().property("centrality")
                    .edges(__.outE(EdgeLabel.DEPENDSON.toString()).asAdmin()).create(explodedGraph);
            var programPackage = PageRankVertexProgram
                    .build().property("centrality")
                    .edges(__.outE(EdgeLabel.AFFERENT.toString()).asAdmin()).create(explodedGraph);

            try {
                Future<ComputerResult> futureClasses = explodedGraph
                        .compute().workers(4) // max workers is 4
                        .program(programClasses)
                        .submit();
                Graph g = futureClasses.get().graph();
                innerMap.put(AffectedDesign.Level.DESIGN, g);

                Future<ComputerResult> futurePackage = explodedGraph
                        .compute().workers(4)
                        .program(programPackage)
                        .submit();
                g = futurePackage.get().graph();
                innerMap.put(AffectedDesign.Level.ARCHITECTURAL, g);
                cachedPageRankGraphs.put(smellGraph, innerMap);

            } catch (InterruptedException e) {
                logger.error("InterruptedException while retrieving computer result: {}", e.getMessage());
                e.printStackTrace();
            } catch (ExecutionException e) {
                logger.error("ExecutionException while retrieving computer result: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        return cachedPageRankGraphs.get(smellGraph).get(smell.getLevel().isDesignLevel() ? AffectedDesign.Level.DESIGN : AffectedDesign.Level.ARCHITECTURAL);
    }

    /**
     * Expands the dependsOn edges using the Weight property. For every dependsOn/packageIsAfferentOf edge e with Weight = n, n new edges
     * will be added to the graph between the nodes connected by e. The original graph is not modified.
     * @param src the starting graph to expand. This graph is not modified
     * @return a new graph containing the exploded edges.
     */
    private static Graph explodeGraph(Graph src){
        Graph dst = clone(src, List.of(VertexLabel.CFILE.toString(), VertexLabel.COMPONENT.toString(), VertexLabel.HFILE.toString()),
                List.of(EdgeLabel.DEPENDSON.toString(), EdgeLabel.AFFERENT.toString()));

        dst.traversal().E().has("Weight").toSet().forEach(edge -> {
            int weight = edge.value("Weight");
            for (int i = 0; i < weight; i++) {
                dst.traversal().addE(edge.label())
                        .from(edge.outVertex()).to(edge.inVertex()).next();
            }
            dst.edges(edge).next().remove();
        });

        return dst;
    }
}
