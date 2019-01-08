package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.VertexLabel;
import org.rug.data.smells.CDShape;
import org.rug.data.smells.SmellType;

import java.util.Iterator;
import java.util.Set;

public class ChainCDEvolver extends CDEvolver {
    public ChainCDEvolver(Graph system) {
        super(system);
    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() <= 2)
            throw new IllegalArgumentException("Length must be more than 2 in order to create chain CD.");

        Vertex smell = g.addV(VertexLabel.SMELL.toString())
                .property("smellType", SmellType.CD.toString())
                .property("smellId", rng.nextInt()).next();

        Iterator<Vertex> itv = vertices.iterator();
        Vertex start = itv.next();
        buildChain(smell, start, vertices);
        g.addE(EdgeLabel.STARTOFCYCLE.toString()).from(smell).to(start);

        Vertex shape = g.addV(VertexLabel.CYCLESHAPE.toString()).property("shapeType", CDShape.CHAIN.toString()).next();
        g.addE(EdgeLabel.ISPARTOFCHAIN.toString()).from(shape).to(smell).next();
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void addElements(Vertex smell, int... n) {
        Vertex start = g.V(smell).out(EdgeLabel.STARTOFCYCLE.toString()).next();
        Set<Vertex> newElements = getVerticesNotAffectedBySmell(n[0]);

        buildChain(smell, start, newElements);
    }

    private void buildChain(Vertex smell, Vertex start, Set<Vertex> vertices) {
        Iterator<Vertex> itv = vertices.iterator();
        Vertex from;
        if (itv.hasNext())
            from = itv.next();
        else
            return;
        Vertex to;
        while (itv.hasNext()){
            to = itv.next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(from).to(to).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(to).to(from).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();
            from = to;
        }
        g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(from).next();
    }
}
