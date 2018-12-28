package org.rug.data;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.Iterator;

/**
 * A class that models a smell as a graph using TinkerGraph as underlying implementation.
 */
public class SmellGraph implements Graph {

    private Graph tinkerGraph;

    public SmellGraph(){
        tinkerGraph = TinkerGraph.open();
    }

    public SmellGraph(String type){
        this();
        setSmellType(type);
    }

    public SmellGraph(Configuration configuration){
        tinkerGraph = TinkerGraph.open(configuration);
    }

    public Vertex addVertex(Object... keyValues) {
        return tinkerGraph.addVertex(keyValues);
    }

    public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        return tinkerGraph.compute(graphComputerClass);
    }

    public GraphComputer compute() throws IllegalArgumentException {
        return tinkerGraph.compute();
    }

    public Iterator<Vertex> vertices(Object... vertexIds) {
        return tinkerGraph.vertices();
    }

    public Iterator<Edge> edges(Object... edgeIds) {
        return tinkerGraph.edges();
    }

    public Transaction tx() {
        return tinkerGraph.tx();
    }

    public void close() throws Exception {
        tinkerGraph.close();
    }

    public Variables variables() {
        return tinkerGraph.variables();
    }

    public Configuration configuration() {
        return tinkerGraph.configuration();
    }

    /**
     * Set a type for this smell graph
     * @param type type
     */
    public void setSmellType(String type){
        tinkerGraph.variables().set("type", type);
    }

    /**
     * Retrieve the type of this smell as a string
     * @return the smell type
     */
    public String getSmellType(){
        return tinkerGraph.variables().get("type").orElse("noType").toString();
    }
}
