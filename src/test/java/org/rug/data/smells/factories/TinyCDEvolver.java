package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.Set;

public class TinyCDEvolver extends CDEvolver {
    public TinyCDEvolver(Graph system) {
        super(system);
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() % 2 != 0)
            throw new IllegalArgumentException("Tiny must have two vertices.");

        Vertex[] vs = vertices.toArray(new Vertex[]{});
        for (int i = 0; i < vs.length - 1; i++) {
            Vertex a = vs[i];
            Vertex b = vs[i + 1];

            g.addE(EdgeLabel.DEPENDSON.toString()).from(a).to(b).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(b).to(a).next();

            Vertex smell = g.addV(VertexLabel.SMELL.toString())
                    .property("smellType", ArchitecturalSmell.Type.CD.toString())
                    .property("smellId", rng.nextInt())
                    .next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(a).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(b).next();

            Vertex shape = g.addV(VertexLabel.CYCLESHAPE.toString())
                    .property("shapeType", "tiny")
                    .next();
            g.addE(EdgeLabel.ISTINYSHAPED.toString()).from(shape).to(smell).next();
        }
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void addElements(Vertex smell, int... n) {
        return;
    }
}
