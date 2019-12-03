package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.labels.EdgeLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        this(smell, Type.HL);
    }
    
    public HLSmell(Vertex smell, Type type) {
        super(smell, type);
        this.inDep = smell.graph().traversal().V(smell).out(EdgeLabel.HLIN.toString()).toSet();
        this.outDep = smell.graph().traversal().V(smell).out(EdgeLabel.HLOUT.toString()).toSet();
        EdgeLabel label = getLevel().isDesignLevel() ? EdgeLabel.HLAFFECTEDCLASS : EdgeLabel.HLAFFECTEDPACK;
        this.setCentre(smell.graph().traversal().V(smell).out(label.toString()).next());
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     *
     * @param smell the starting node
     */
    @Override
    public void setAffectedElements(Vertex smell) {
        EdgeLabel label = getLevel().isDesignLevel() ? EdgeLabel.HLAFFECTEDCLASS : EdgeLabel.HLAFFECTEDPACK;
        this.affectedElements = new HashSet<>();
        this.affectedElements.add(smell.graph().traversal().V(smell).out(label.toString()).next());
        // Ingoing and outgoing dependencies are also considered affected elements
        this.affectedElements.addAll(smell.graph().traversal().V(smell).out(EdgeLabel.HLIN.toString()).toSet());
        this.affectedElements.addAll(smell.graph().traversal().V(smell).out(EdgeLabel.HLOUT.toString()).toSet());
    }

    /**
     * Gets the set of incoming dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getInDep() {
        return Collections.unmodifiableSet(inDep);
    }

    /**
     * Gets the set of incoming dependencies to the element affected by this smell as names.
     * @return a set of strings.
     */
    public Set<String> getInDepNames(){return getInDep().stream().map(v -> v.property("name").toString()).collect(Collectors.toSet());}

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getOutDep() {
        return Collections.unmodifiableSet(outDep);
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell as names.
     * @return a set of strings.
     */
    public Set<String> getOutDepNames(){return getOutDep().stream().map(v -> v.property("name").toString()).collect(Collectors.toSet());}

    /**
     * Computes the classes that have an incoming edge to one of the elements within the affected package.
     * @return a set of vertices
     */
    public Set<Vertex> getClassesDependedUponByAfferentPackages(){
        if (!this.getLevel().isArchitecturalLevel()) {
            return Collections.emptySet();
        }
        var affectedPackage = this.getCentre();
        return getTraversalSource().V(this.getInDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISAFFERENTOF.toString())
                        .is(affectedPackage))
                .out(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();
    }

    /**
     * Computes the classes that have an outgoing edge to one of the elements outside the affected package.
     * @return a set of vertices
     */
    public Set<Vertex> getClassesDependingOnEfferentPackages(){
        if (!this.getLevel().isArchitecturalLevel()) {
            return Collections.emptySet();
        }
        var affectedPackage = this.getCentre();
        return getTraversalSource().V(this.getOutDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISEFFERENTOF.toString())
                        .is(affectedPackage))
                .in(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();
    }

    @Override
    public <R> R accept(SmellVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HLSmell) {
            return super.equals(o) && ((HLSmell) o).inDep.equals(inDep) && ((HLSmell) o).outDep.equals(outDep);
        }else {
            return false;
        }
    }
}
