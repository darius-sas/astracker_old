package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.EnumSet;

/**
 * Calculates the "Change has occurred metric" for packages for both classes and packages.
 * For packages, the value is true every time any direct contained class has a changed.
 */
public class CHOMetricPackage extends AbstractComponentCharacteristic {

    public final static String NAME = "changeHasOccurredMetric";

    public CHOMetricPackage(String name) {
        super(name, VertexLabel.allComponents(), EnumSet.noneOf(EdgeLabel.class));
    }

    public CHOMetricPackage(){
        this(NAME);
    }

    /**
     * Calculate the characteristic on the graph of this version and save it as a node, vertex or graph property.
     *
     * @param version the version from which to retrieve the components
     */
    @Override
    public void calculate(IVersion version) {
        if (version.getVersionIndex() == 1)
            return;
        super.calculate(version);
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param vertex the vertex to calculate this characteristic on. The result must be stored within the vertex using
     *               {@link #name} as property key.
     */
    @Override
    protected void calculate(Vertex vertex) {
        boolean hasChanged = vertex.graph().traversal().V(vertex).in(EdgeLabel.BELONGSTO.toString())
                .hasLabel(P.within(VertexLabel.getFilesStrings()))
                .<Boolean>values(this.name).toStream().filter(b -> b).findAny().orElse(false);
        vertex.property(name, hasChanged);
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
