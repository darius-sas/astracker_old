package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

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
     */
    @Override
    public void shapeShift(Vertex smell) {
        // Skip smells that are not part of the graph (previously removed)
        if (!g.V(smell).hasNext())
            return;

        Set<Vertex> affectedElements = getAffectedElements(smell);

        // Remove all the "smell" vertices starting from affected elements
        g.V(affectedElements)
                .in(EdgeLabel.PARTOFCYCLE.toString()).aggregate("x")
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString()).aggregate("x")
                .select("x").unfold().drop().iterate();

        addSmell(affectedElements);
    }

    /**
     * Returns the elements affected by this CD smell instance
     * @param smell
     * @return
     */
    protected Set<Vertex> getAffectedElements(Vertex smell) {
        return g.V(smell)
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .out().hasLabel(VertexLabel.SMELL.toString())
                .out().hasLabel(P.within(VertexLabel.CLASS.toString(), VertexLabel.PACKAGE.toString())).toSet();
    }
}
