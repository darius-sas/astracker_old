package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;

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
}
