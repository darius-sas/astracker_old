package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.EnumSet;

public class NumberOfClassesInPackage extends AbstractComponentCharacteristic {

    public NumberOfClassesInPackage() {
        super("numOfClassesInPackage", VertexLabel.allComponents(), EnumSet.noneOf(EdgeLabel.class));
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param vertex the vertex to calculate this characteristic on. The result must be stored within the vertex using
     *               {@link #name} as property key.
     */
    @Override
    protected void calculate(Vertex vertex) {
        long numOfClasses = vertex.graph().traversal().V(vertex).in(EdgeLabel.BELONGSTO.toString()).count().next();
        vertex.property(name, numOfClasses);
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param edge the edge to calculate this characteristic on. The result must be stored within the edge using
     *             {@link #name} as property key.
     */
    @Override
    protected void calculate(Edge edge) {}
}
