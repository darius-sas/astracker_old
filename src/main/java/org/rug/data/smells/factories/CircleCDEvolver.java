package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;
import org.rug.data.smells.CDShape;
import org.rug.data.smells.SmellType;

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
                .property("smellType", SmellType.CD.toString())
                .property("smellId", rng.nextInt()).next();

        Iterator<Vertex> itv = vertices.iterator();
        Vertex start = itv.next();
        Vertex from = null;
        do{
            from = (from == null) ? start : itv.next();
            Vertex to = itv.hasNext() ? itv.next() : start;
            g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(to).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();

        }while (itv.hasNext());

        Vertex shape = g.addV(VertexLabel.CYCLESHAPE.toString()).property("shapeType", CDShape.CIRCLE.toString()).next();
        g.addE(EdgeLabel.ISCIRCLESHAPED.toString()).from(shape).to(smell).next();

    }

    @Override
    public void addElements(Vertex smell, int... n) {
        Set<Vertex> newElements = getVerticesNotAffectedBySmell(n[0]);

        Vertex start = g.V(smell).out().next();
        Vertex end = (Vertex)g.V(start)
                .out(EdgeLabel.DEPENDSON.toString()).as("end")
                .inE(EdgeLabel.PARTOFCYCLE.toString())
                .outV().is(start).cap("end").select("end").next();

        g.E().from(start).to(end).drop().iterate();

        Iterator<Vertex> itv = newElements.iterator();
        Vertex from = null;
        Vertex to = null;
        do{
            from = (from == null) ? start : itv.next();
            to = itv.hasNext() ? itv.next() : end;
            g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(to).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();

        }while (itv.hasNext());

    }
}
