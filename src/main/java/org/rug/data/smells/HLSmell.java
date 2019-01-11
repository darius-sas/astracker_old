package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Hublike dependency smell.
 */
public class HLSmell extends SingleElementSmell {

    private Set<Vertex> inDep;
    private Set<Vertex> outDep;

    /**
     * Builds an architectural smell instance of a HD smell starting from the given vertex.
     * @param smell the vertex to use.
     */
    public HLSmell(Vertex smell) {
        super(smell, Type.HL);
        this.inDep = smell.graph().traversal().V(smell).out(EdgeLabel.HLIN.toString()).toSet();
        this.outDep = smell.graph().traversal().V(smell).out(EdgeLabel.HLOUT.toString()).toSet();
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     *
     * @param smell the starting node
     */
    @Override
    public void setAffectedElements(Vertex smell) {
        // Select the appropriate label based on the smell label
        EdgeLabel label = EdgeLabel.HLAFFECTEDCLASS.toString().toLowerCase().contains(getLevel().toString().toLowerCase()) ? EdgeLabel.HLAFFECTEDCLASS : EdgeLabel.HLAFFECTEDPACK;
        setAffectedElements(new HashSet<>());
        getAffectedElements().add(smell.graph().traversal().V(smell).out(label.toString()).next());
    }

    /**
     * Gets the set of incoming dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getInDep() {
        return Collections.unmodifiableSet(inDep);
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getOutDep() {
        return Collections.unmodifiableSet(outDep);
    }
}
