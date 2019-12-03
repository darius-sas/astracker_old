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
    public SingleElementSmell(Vertex smell, Type type) {
        super(smell, type);
    }

    /**
     * Get the vertex affected by this smell.
     * @return the vertex affected by this smell.
     */
    public Vertex getCentre() {
        return centre;
    }

    /**
     * Return the name property of the centre of this smell.
     * @return a string representing the name.
     */
    public String getCentreName(){
        return this.getCentre().value("name");
    }

    /**
     * The component center of this smell.
     * @param centre the vertex causing this smell.
     */
    public void setCentre(Vertex centre){
        this.centre = centre;
    }

    /**
     * Sets the smells affected by this smell starting from the smell vertex describing it.
     * @param smell the starting smell node. This will be mostly the only element in this set.
     */
    @Override
    protected void setSmellNodes(Vertex smell) {
        this.smellNodes = new HashSet<>();
        this.smellNodes.add(smell);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SingleElementSmell)
            return super.equals(o) && ((SingleElementSmell) o).centre == centre;
        else
            return false;
    }
}
