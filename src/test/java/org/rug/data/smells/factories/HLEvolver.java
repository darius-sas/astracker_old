package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Evolves HL smells.
 */
public class HLEvolver extends ASEvolver {

    private int in;
    private ArchitecturalSmell.Level level;

    protected HLEvolver(Graph system) {
        this(system, 3, ArchitecturalSmell.Level.PACKAGE);
    }

    /**
     * Creates an instance where the amount of in-dependencies of the central component of the created smells
     * will be equal to the given amount. The out will be calculated as a difference.
     * @param system the system to use
     * @param in the number of in-dependencies
     */
    protected HLEvolver(Graph system, int in, ArchitecturalSmell.Level level){
        super(system);
        this.in = in;
        this.level = level;
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() - in - 1 <= 0)
            throw new IllegalArgumentException("The given number of vertices is not sufficient to create a smell with the given amount of in dependencies");

        Vertex centre = new ArrayList<>(vertices).get(rng.nextInt(vertices.size()));
        Vertex smell = g.addV(VertexLabel.SMELL.toString())
                .property("smellType", ArchitecturalSmell.Type.HL.toString())
                .property("smellId", rng.nextInt())
                .property("vertexType", ArchitecturalSmell.Level.CLASS.toString())
                .next();

        Set<Vertex> inDependecies = new HashSet<>();
        Set<Vertex> outDependencies = new HashSet<>();

        Set<Vertex> inout = vertices.stream().filter(vertex -> !vertex.equals(centre)).collect(Collectors.toSet());
        int index = 0;
        for(Vertex vertex : inout){
            if(index++ % 2 == 0) inDependecies.add(vertex); else outDependencies.add(vertex);
        }

        g.addE(EdgeLabel.getHLAffectedOf(level).toString()).from(smell).to(centre).next();
        inDependecies.forEach(vertex -> {
            g.addE(EdgeLabel.DEPENDSON.toString()).from(vertex).to(centre).next();
            g.addE(EdgeLabel.HLIN.toString()).from(smell).to(vertex).next();
        });

        outDependencies.forEach(vertex -> {
            g.addE(EdgeLabel.DEPENDSON.toString()).from(centre).to(vertex).next();
            g.addE(EdgeLabel.HLOUT.toString()).from(smell).to(vertex).next();
        });
    }


    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void addElements(ArchitecturalSmell smell, int... n) {

    }

    /**
     * Remove the given amount of elements from the given smell. If the number of elements exceeds the minimum
     * number of elements necessary for the smell to exists, then the smell is removed.
     *
     * @param smell the smell to reduce
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void removeElements(ArchitecturalSmell smell, int... n) {

    }

    /**
     * Valid only for smells that have a shape attribute.
     * Changes the shape of a smell to the given shape.
     *
     * @param smell   the smell to shapeshift.
     */
    @Override
    public void shapeShift(CDSmell smell) {
        return;
    }
}
