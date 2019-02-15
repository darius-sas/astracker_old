package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface IComponentCharacteristic {

    /**
     * Returns the name of this characteristic.
     * @return the name.
    */
    String getName();

    /**
     * The graph to calculate this characteristic and store it as a vertex, edge, or graph property.
     * @param graph the graph to use
     */
    void calculate(Graph graph);

    /**
     * Returns the label used for the main operations on the given graph.
     * @return an array of labels.
     */
    String[] getOperatingEdgeLabels();

    /**
     * Returns the label used for the main operations on the given graph.
     * @return an array of labels.
     */
    String[] getOperatingVertexLabels();
}
