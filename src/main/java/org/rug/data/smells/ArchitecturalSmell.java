package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;

import java.util.Set;

/**
 * Abstraction of an AS. A smell is composed by the nodes that represent the smell (label
 * <code>VertexLabel.SMELL</code>), and the nodes that are affected by the smell (label
 * <code>VertexLabel.PACKAGE || VertexLabel.CLASS</code>.
 */
public abstract class ArchitecturalSmell {
    private long id;
    private Set<Vertex> smellNodes;
    private Set<Vertex> affectedElements;

    private SmellType smellType;


    /**
     * Initializes this smell instance starting from the smell node
     * @param smell the smell that characterizes this instance.
     */
    protected ArchitecturalSmell(Vertex smell){
        assert smell.label().equals(VertexLabel.SMELL.toString());
        this.id = smell.value("id");
        this.smellType = SmellType.valueOf(smell.value("smellType"));
        setAffectedElements(smell);
        setSmellNodes(smell);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Vertex> getSmellNodes() {
        return smellNodes;
    }

    public void setSmellNodes(Set<Vertex> smellNodes) {
        this.smellNodes = smellNodes;
    }

    public Set<Vertex> getAffectedElements() {
        return affectedElements;
    }

    public void setAffectedElements(Set<Vertex> affectedElements) {
        this.affectedElements = affectedElements;
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     * @param smell the starting node
     */
    public abstract void setAffectedElements(Vertex smell);

    /**
     * Sets the smell nodes that characterize this instance
     * @param smell the starting smell node. This will be mostly the only element in this set.
     */
    public abstract void setSmellNodes(Vertex smell);
}
