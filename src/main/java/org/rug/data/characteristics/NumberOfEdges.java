package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.HashSet;
import java.util.Set;

public class NumberOfEdges extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected NumberOfEdges() {
        super("numOfEdges");
    }

    @Override
    public String visit(CDSmell smell) {
        return countEdges(smell.getAffectedElements(), smell);
    }

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

    /**
     * Calculates the number of edges with any label among the given vertices
     * @param vertices the vertices to use for edge counting among them
     * @param smell the smell to get the graph from
     * @return the number of edges among the given vertices or 0 if no edges are present.
     */
    private String countEdges(Set<Vertex> vertices, ArchitecturalSmell smell){
        return smell.getTraversalSource().V(vertices)
                .bothE()
                .where(__.otherV().is(P.within(vertices)))
                .count().tryNext().orElse(0L).toString();
    }
}
