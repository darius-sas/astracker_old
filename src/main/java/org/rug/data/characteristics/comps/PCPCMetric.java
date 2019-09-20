package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.EnumSet;

/**
 * Calculates the PCPC (Percentage of Commits Package has Changed) metric.
 * This is the only characteristic that assumes that PCCCMetric has already been
 * computed for the current graph/version (for performance reasons).
 * The current implementation is taking the maximum value of PCCC of the vertexes belonging
 * to the current package and uses it as a the value of PCPC.
 */
public class PCPCMetric extends AbstractComponentCharacteristic {

    private String pcccName;

    PCPCMetric(String name, String pcccName) {
        super(name, VertexLabel.allComponents(), EnumSet.noneOf(EdgeLabel.class));
        this.pcccName = pcccName;
    }

    PCPCMetric(String pcccName){
        this(pcccName, pcccName);
    }

    @Override
    protected void calculate(Vertex vertex) {
        var pcpcValue = vertex.graph().traversal()
                .V(vertex)
                .in(EdgeLabel.BELONGSTO.toString())
                .values(pcccName)
                .max().tryNext().orElse(0d);
        vertex.property(this.name, pcpcValue);
    }

    @Override
    protected void calculate(Edge edge) {

    }
}
