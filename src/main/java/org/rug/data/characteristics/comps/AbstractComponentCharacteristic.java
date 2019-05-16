package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.characteristics.IComponentCharacteristic;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractComponentCharacteristic implements IComponentCharacteristic {

    protected String name;
    protected List<String> eLabels;
    protected List<String> vLabels;

    public AbstractComponentCharacteristic(String name, EnumSet<VertexLabel> vLabels, EnumSet<EdgeLabel> eLabels) {
        this.name = name;
        this.vLabels = vLabels.stream().map(VertexLabel::toString).collect(Collectors.toList());
        this.eLabels = eLabels.stream().map(EdgeLabel::toString).collect(Collectors.toList());
    }

    /**
     * Returns the name of this characteristic.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * The graph to calculate this characteristic and store it as a vertex, edge, or graph property.
     *
     * @param graph the graph to use
     */
    @Override
    public void calculate(Graph graph) {
        graph.traversal().V().hasLabel(P.within(vLabels)).forEachRemaining(this::calculate);
        graph.traversal().E().hasLabel(P.within(eLabels)).forEachRemaining(this::calculate);
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     * @param vertex the vertex to calculate this characteristic on. The result must be stored within the vertex using
     *               {@link #name} as property key.
     */
    protected abstract void calculate(Vertex vertex);

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     * @param edge the edge to calculate this characteristic on. The result must be stored within the edge using
     *               {@link #name} as property key.
     */
    protected abstract void calculate(Edge edge);

    /**
     * Returns the label used for the main operations on the given graph.
     *
     * @return an array of labels.
     */
    @Override
    public List<String> getOperatingEdgeLabels() {
        return eLabels;
    }

    /**
     * Returns the label used for the main operations on the given graph.
     *
     * @return an array of labels.
     */
    @Override
    public List<String> getOperatingVertexLabels() {
        return vLabels;
    }
}
