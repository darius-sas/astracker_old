package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;
import org.rug.data.smells.CDShape;

import java.util.Set;

public abstract class CDEvolver extends ASEvolver {
    public CDEvolver(Graph system) {
        super(system);
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public abstract void addSmell(Set<Vertex> vertices);

    /**
     * Removes the smell from the system
     *
     * @param smell the smell to remove that has a VertexLabel.Smell label.
     */
    @Override
    public void removeSmell(Vertex smell) {
        if (!smell.label().equals(VertexLabel.SMELL))
            return;

        g.V(smell)
                .inV().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .outV().hasLabel(VertexLabel.SMELL.toString())
                .drop().iterate();

        g.V(smell).drop().iterate();
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public abstract void addElements(Vertex smell, int... n);

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
     * @param toShape the output shape
     */
    @Override
    public void shapeShift(Vertex smell, CDShape toShape) {

    }
}
