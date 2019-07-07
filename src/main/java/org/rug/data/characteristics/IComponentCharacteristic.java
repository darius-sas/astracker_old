package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.project.IVersion;

import java.util.List;

/**
 * Represents a characteristic/attribute/property of a component.
 */
public interface IComponentCharacteristic {

    /**
     * Returns the name of this characteristic.
     * @return the name.
    */
    String getName();

    /**
     * The version that contains the graph to calculate this characteristic and store it as a vertex, edge, or graph property.
     * @param version the version from which to retrieve the components
     */
    void calculate(IVersion version);

    /**
     * Returns the label used for the main operations on the given graph.
     * @return an array of labels.
     */
    List<String> getOperatingEdgeLabels();

    /**
     * Returns the label used for the main operations on the given graph.
     * @return an array of labels.
     */
    List<String> getOperatingVertexLabels();
}
