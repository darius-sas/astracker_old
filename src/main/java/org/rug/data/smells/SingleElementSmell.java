package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashSet;

/**
 * Models smells that are composed by a single central element.
 */
public abstract class SingleElementSmell extends ArchitecturalSmell {

    private Vertex centre;

    /**
     * Initializes this smell instance starting from the smell node
     *
     * @param smell the smell that characterizes this instance.
     */
    public SingleElementSmell(Vertex smell, SmellType type) {
        super(smell, type);
        this.centre = getAffectedElements().iterator().next();
    }

    public Vertex getCentre() {
        return centre;
    }

    @Override
    public void setSmellNodes(Vertex smell) {
        setSmellNodes(new HashSet<>());
        getSmellNodes().add(smell);
    }
}
