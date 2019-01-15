package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

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
    public void removeSmell(ArchitecturalSmell smell) {
        g.V(smell.getSmellNodes())
                .inV().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .outV().hasLabel(VertexLabel.SMELL.toString())
                .drop().iterate();

        g.V(smell.getSmellNodes()).drop().iterate();
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public abstract void addElements(ArchitecturalSmell smell, int... n);

    /**
     * Remove the given amount of elements from the given smell. If the number of elements exceeds the minimum
     * number of elements necessary for the smell to exists, then the smell is removed.
     *
     * @param smell the smell to reduce
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void removeElements(ArchitecturalSmell smell, int... n) {

    }

    /**
     * Valid only for smells that have a shape attribute.
     * Changes the shape of a smell to the given shape.
     *
     * @param smell   the smell to shapeshift.
     */
    @Override
    public void shapeShift(CDSmell smell) {
        // Skip smells that are not part of the graph (previously removed)
        if (!g.V(smell.getSmellNodes()).hasNext())
            return;

        Set<Vertex> affectedElements = smell.getAffectedElements();

        // Remove all the "smell" vertices starting from affected elements
        g.V(affectedElements)
                .in(EdgeLabel.PARTOFCYCLE.toString()).aggregate("x")
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString()).aggregate("x")
                .select("x").unfold().drop().iterate();

        addSmell(affectedElements);
    }
}
