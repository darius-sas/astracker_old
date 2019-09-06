package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.labels.EdgeLabel;

import java.util.Set;

public class GCSmell extends SingleElementSmell {
    /**
     * Initializes this smell instance starting from the smell node
     *
     * @param smell the smell that characterizes this instance.
     */
    public GCSmell(Vertex smell) {
        super(smell, Type.GC);
    }

    @Override
    protected void setAffectedElements(Vertex smell) {

    }

    @Override
    public <T> T accept(SmellVisitor<T> visitor) {
        return null;
    }

    /**
     * Retrieves the elements that directly belong to the element affected by God Component.
     * @return a set of vertices (classes and packages) that belong to the affected element.
     */
    public Set<Vertex> getElementsInAffected(){
        return getTraversalSource().V(getCentre()).in(EdgeLabel.BELONGSTO.toString()).toSet();
    }
}
