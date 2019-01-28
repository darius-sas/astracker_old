package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class PageRank extends AbstractSmellCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PageRank.class);


    /**
     * Sets up the name of this smell characteristic.
     */
    protected PageRank() {
        super("pageRank");
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

    /**
     * Computes the pagerank on the affected elements of the smell and returns the maximum value.
     * This behaviour was suggested by Roveda et al. 2018
     * @param smell the smell to calculate the page rank of.
     * @return a string containing the double value computed.
     */
    private String visitInternal(ArchitecturalSmell smell){

        var pageRank = 0d;
        Graph g = getPageRankGraph(smell);
        Set<String> affectedElements = smell.getAffectedElements().stream().map(vertex -> vertex.value("name").toString()).collect(Collectors.toSet());
        pageRank = g.traversal().V().has("name", P.within(affectedElements))
                .values("centrality").max().next().doubleValue();

        return String.valueOf(pageRank);
    }

    /**
     * Clones the given graph and returns the clone.
     * @param src the graph to clone
     * @return the clone
     */
    private Graph clone(Graph src, List<String> vLabels, List<String> eLabels){
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


    private static Map<Graph, Map<ArchitecturalSmell.Level, Graph>> cachedPageRankGraphs = new HashMap<>();

    private static Graph getPageRankGraph(ArchitecturalSmell smell){
        if (!cachedPageRankGraphs.containsKey(smell.getAffectedGraph())) {
            var innerMap = new HashMap<ArchitecturalSmell.Level, Graph>();

            var programClasses = PageRankVertexProgram
                    .build().property("centrality")
                    .edges(__.outE(EdgeLabel.DEPENDSON.toString()).asAdmin()).create(smell.getAffectedGraph());
            var programPackage = PageRankVertexProgram
                    .build().property("centrality")
                    .edges(__.outE(EdgeLabel.PACKAGEISAFFERENTOF.toString()).asAdmin()).create(smell.getAffectedGraph());

            try {
                Future<ComputerResult> futureClasses = smell.getAffectedGraph()
                        .compute().workers(4)
                        .program(programClasses)
                        .submit(); // max workers is 4
                Graph g = futureClasses.get().graph();
                innerMap.put(ArchitecturalSmell.Level.CLASS, g);

                Future<ComputerResult> futurePackage = smell.getAffectedGraph()
                        .compute().workers(4)
                        .program(programPackage)
                        .submit();
                g = futurePackage.get().graph();
                innerMap.put(ArchitecturalSmell.Level.PACKAGE, g);
                cachedPageRankGraphs.put(smell.getAffectedGraph(), innerMap);

            } catch (InterruptedException e) {
                logger.error("InterruptedException while retrieving computer result: {}", e.getMessage());
                e.printStackTrace();
            } catch (ExecutionException e) {
                logger.error("ExecutionException while retrieving computer result: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        return cachedPageRankGraphs.get(smell.getAffectedGraph()).get(smell.getLevel());
    }
}
