package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;
import org.rug.data.smells.CDShape;
import org.rug.data.smells.SmellType;

import java.util.Set;

/**
 * Clique-shaped smell evolver
 */
public class CliqueCDEvolver extends CDEvolver {

    public CliqueCDEvolver(Graph system) {
        super(system);
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() < 2)
            throw new IllegalArgumentException("Vertices must be more than 2 in order to create clique CD.");

        Vertex smell = g.addV(VertexLabel.SMELL.toString())
                .property("smellType", SmellType.CD.toString())
                .property("smellId", rng.nextInt()).next();

        for (Vertex from : vertices) {
            for (Vertex to : vertices) {
                g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(to).next();
            }
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();
        }
        Vertex shape = g.addV(VertexLabel.CYCLESHAPE.toString())
                .property("shapeType", CDShape.CLIQUE.toString()).next();
        g.addE(EdgeLabel.ISCLIQUESHAPED.toString()).from(shape).to(smell).next();
    }
}
