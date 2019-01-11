package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UDSmell extends SingleElementSmell {

    private Set<Vertex> badDep;

    /**
     * Initializes this smell instance starting from the smell node
     *
     * @param smell the smell that characterizes this instance.
     */
    public UDSmell(Vertex smell) {
        super(smell, Type.UD);
        this.badDep = smell.graph().traversal().V(smell).out(EdgeLabel.UDBADDEP.toString()).toSet();
    }

    @Override
    public void setAffectedElements(Vertex smell) {
        setAffectedElements(new HashSet<>());
        getAffectedElements().add(smell.graph().traversal().V(smell).out(EdgeLabel.UDAFFECTED.toString()).next());
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getBadDep() {
        return Collections.unmodifiableSet(badDep);
    }

    /**
     * UD is only defined at package Level, so we set it like that by default
     * @param smell
     */
    @Override
    protected void setLevel(Vertex smell) {
        setLevel(Level.PACKAGE);
    }
}
