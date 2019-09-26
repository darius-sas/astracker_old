package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.smells.*;

import java.util.HashSet;
import java.util.Set;

/**
 * This characteristic returns the number of edges among the affected components.
 * The Weight property is taken into account.
 */
public class NumberOfEdges extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public NumberOfEdges() {
        super("numOfEdges");
    }

    @Override
    public String visit(CDSmell smell) {
        return countEdges(smell.getAffectedElements(), smell);
    }

    @Override
    public String visit(UDSmell smell) {
        Set<Vertex> vertices = new HashSet<>();
        vertices.add(smell.getCentre());
        vertices.addAll(smell.getBadDep());
        return countEdges(vertices, smell);
    }

    @Override
    public String visit(HLSmell smell) {
        Set<Vertex> vertices = new HashSet<>();
        vertices.add(smell.getCentre());
        vertices.addAll(smell.getInDep());
        vertices.addAll(smell.getOutDep());
        return countEdges(vertices, smell);
    }

    @Override
    public String visit(GCSmell smell) {
        Set<Vertex> vertices = new HashSet<>(smell.getElementsInAffected());
        return countEdges(vertices, smell);
    }

    /**
     * Calculates the number of edges that have Weight property among the given vertices, sums such values among them
     * and then sums the count of rest of the edges.
     * @param vertices the vertices to use for edge counting among them
     * @param smell the smell to get the graph from
     * @return the sums of the weights and the number of other edges, or 0 if no edges are found.
     */
    private String countEdges(Set<Vertex> vertices, ArchitecturalSmell smell){
        return String.valueOf(
                smell.getTraversalSource()
                        .V(vertices).bothE()
                .hasNot("Weight")
                .where(__.otherV().is(P.within(vertices)))
                .count().tryNext().orElse(0L)
                +
                        smell.getTraversalSource()
                                .V(vertices).bothE()
                .has("Weight")
                .where(__.otherV().is(P.within(vertices))).as("edges")
                .select("edges").by("Weight")
                .sum().tryNext().orElse(0L).longValue());
    }
}
