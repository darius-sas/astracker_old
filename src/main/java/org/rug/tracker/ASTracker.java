package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;

import java.util.List;
import java.util.Map;

public class ASTracker {

    public static void trackAS(Graph graphV1, Graph graphV2){
        // Forward pass
        GraphTraversalSource g1 = graphV1.traversal();
        GraphTraversalSource g2 = graphV2.traversal();

        Map<String, List<Vertex>> smellGroups = (Map<String, List<Vertex>>)(Map<?,?>)g1.V().hasLabel(VertexLabel.SMELL.toString()).group().by("smellType").next();

        smellGroups.forEach((type, vertices) -> {
            switch (type) {
                case "cyclicDependency":
                    Vertex star = g1.V(vertices).in(EdgeLabel.PARTOFSTAR.toString()).next();

                    break;
                default:
                    break;
            }
        });

        smellGroups.clear();

        // Backward pass
    }
}
