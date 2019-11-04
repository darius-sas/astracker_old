package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.EnumSet;

/**
 * Computes the 'Total amount of changes' metric on packages.
 */
class TACHMetricPackage extends AbstractComponentCharacteristic {

    public final static String NAME = "totalAmountOfChanges";

    public TACHMetricPackage() {
        super(NAME, VertexLabel.allComponents(), EnumSet.noneOf(EdgeLabel.class));
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param vertex the vertex to calculate this characteristic on. The result must be stored within the vertex using
     *               {@link #name} as property key.
     */
    @Override
    protected void calculate(Vertex vertex) {
        var tach = vertex.graph().traversal().V(vertex)
                .in(EdgeLabel.BELONGSTO.toString())
                .hasLabel(P.within(VertexLabel.getFilesStrings()))
                .values(this.name)
                .sum().tryNext().orElse(0);
        vertex.property(this.name, tach);
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param edge the edge to calculate this characteristic on. The result must be stored within the edge using
     *             {@link #name} as property key.
     */
    @Override
    protected void calculate(Edge edge) {

    }
}
