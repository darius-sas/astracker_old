package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;

import java.util.*;

@SuppressWarnings("unchecked")
public class ASTracker {

    public static int trackCD(Graph graphV1, Graph graphV2){
        // Forward pass
        GraphTraversalSource g1 = graphV1.traversal();
        GraphTraversalSource g2 = graphV2.traversal();

        Map<String, List<Vertex>> smellGroups = (Map<String, List<Vertex>>)(Map<?,?>)g1.V()
                .hasLabel(VertexLabel.CYCLESHAPE.toString())
                .group()
                .by("shapeType").next();

        int count = 0;
        Map<String, Map<Vertex, AbstractMap.SimpleEntry<Set<Vertex>, Set<Vertex>>>> smellMappingsPerType = new HashMap<>();

        for (Map.Entry<String, List<Vertex>> entry : smellGroups.entrySet()){
            String shape = entry.getKey();
            List<Vertex> vertices = entry.getValue();
            switch (shape){
                case "star":
                    smellMappingsPerType.put(shape, getMapping(g1, g2, vertices, EdgeLabel.PARTOFSTAR));
                    break;
                default:
                    break;
            }

        }
        // TODO return results as pairs of smell ids
        return count;
        // Backward pass
    }

    /**
     * Computes what vertices of g1 are present in g1 using the "name" property at package or class level.
     * @param g1 the first graph to extract the smells from
     * @param g2 the second graph to extract the smells from. It is assumed that this is the evolution of g1
     * @param smellVertices the vertices that describe the smell
     * @param cdType the edge label that describe the smell
     * @return A map where the keys is the smell vertex in smellVertices and the value is a Pair of sets,
     *         with the first element containing vertices from g1 and the second element the elements from g2
     *         part of the same smell.
     */
    private static Map<Vertex, AbstractMap.SimpleEntry<Set<Vertex>, Set<Vertex>>> getMapping(GraphTraversalSource g1,
                                                                                             GraphTraversalSource g2,
                                                                                             List<Vertex> smellVertices,
                                                                                             EdgeLabel cdType) {

        Map<Vertex, AbstractMap.SimpleEntry<Set<Vertex>, Set<Vertex>>> versionsMapping = new HashMap<>();
        for (Vertex smell : smellVertices){
            Set<String> vNames = (Set<String>)(Set<?>)
                    g1.V(smell)
                    .out(cdType.toString())
                    .out(EdgeLabel.PARTOFCYCLE.toString())
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .values("name").toSet();
            Set<Vertex> v1 = g1.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(vNames))
                    .toSet();
            Set<Vertex> v2 = g2.V()
                    .has("name", P.within(vNames))
                    .in(EdgeLabel.PARTOFCYCLE.toString()).toSet();

            versionsMapping.put(smell, new AbstractMap.SimpleEntry<>(v1, v2));

            /* TODO:
                - check whether the nodes are part of the same smell
            */
        }
        return versionsMapping;
    }
}
