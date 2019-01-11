package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.Iterator;
import java.util.Set;

/**
 * Evolves Circle CD smells
 */
public class CircleCDEvolver extends CDEvolver{

    public CircleCDEvolver(Graph system) {
        super(system);
    }


    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() < 3)
            throw new IllegalArgumentException("Length must be more than 2 in order to create circle CD.");


        Vertex smell = g.addV(VertexLabel.SMELL.toString())
                .property("smellType", ArchitecturalSmell.Type.CD.toString())
                .property("smellId", rng.nextInt()).next();

        Iterator<Vertex> itv = vertices.iterator();
        Vertex from;
        if (itv.hasNext())
            from = itv.next();
        else
            return;
        Vertex start = from;
        Vertex to;
        while (itv.hasNext()){
            to = itv.next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(to).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();
            from = to;
        }
        g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(start).next();
        g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();

        Vertex shape = g.addV(VertexLabel.CYCLESHAPE.toString()).property("shapeType", CDSmell.Shape.CIRCLE.toString()).next();
        g.addE(EdgeLabel.ISCIRCLESHAPED.toString()).from(shape).to(smell).next();

    }

    @Override
    public void addElements(Vertex smell, int... n) {
        Set<Vertex> newElements = getVerticesNotAffectedBySmell(n[0]);
        Set<Vertex> currentElements = getAffectedElements(smell);
        currentElements.addAll(newElements);
        addSmell(currentElements);
    }
}
