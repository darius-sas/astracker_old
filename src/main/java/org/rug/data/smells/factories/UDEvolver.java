package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;
import org.rug.data.smells.CDShape;
import org.rug.data.smells.SmellType;

import java.util.ArrayList;
import java.util.Set;

public class UDEvolver extends ASEvolver {
    protected UDEvolver(Graph system) {
        super(system);
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {

        Vertex centre = new ArrayList<>(vertices).get(rng.nextInt(vertices.size()));
        Vertex smell = g.addV(VertexLabel.SMELL.toString())
                .property("smellType", SmellType.HL.toString())
                .property("smellId", rng.nextInt()).next();

        g.addE(EdgeLabel.UDAFFECTED.toString()).from(smell).to(centre).next();

        vertices.stream().filter(vertex -> !vertex.equals(centre)).forEach(vertex -> {
            g.addE(EdgeLabel.DEPENDSON.toString()).from(centre).to(vertex).next();
            g.addE(EdgeLabel.UDBADDEP.toString()).from(smell).to(vertex).next();
        });
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void addElements(Vertex smell, int... n) {

    }

    /**
     * Remove the given amount of elements from the given smell. If the number of elements exceeds the minimum
     * number of elements necessary for the smell to exists, then the smell is removed.
     *
     * @param smell the smell to reduce
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void removeElements(Vertex smell, int... n) {

    }

    /**
     * Valid only for smells that have a shape attribute.
     * Changes the shape of a smell to the given shape.
     *
     * @param smell   the smell to shapeshift.
     */
    @Override
    public void shapeShift(Vertex smell) {
        return;
    }
}
