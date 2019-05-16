package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Counts the lines of code of a given vertex (package or class) and saves the results within
 * the vertex node using {@link #name} as key.
 */
public class NumberOfLinesOfCode extends AbstractComponentCharacteristic {

    private ClassSourceCodeRetriever sourceRetriever;

    /**
     * Instantiates the calculator of LOC.
     * @param sourceRetriever the object that retrieves the source code of a class. If null, no computation is performed.
     */
    public NumberOfLinesOfCode(ClassSourceCodeRetriever sourceRetriever){
        super("linesOfCode", EnumSet.of(VertexLabel.CLASS, VertexLabel.PACKAGE), EnumSet.noneOf(EdgeLabel.class));
        this.sourceRetriever = sourceRetriever;
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     * If the method is invoked on a package vertex, than it triggers the calculation of the lines of code on all the
     * classes belonging to the package.
     *
     * @param vertex the vertex to calculate this characteristic on. The result is stored using {@code this.name}
     *              as property key.
     */
    @Override
    protected void calculate(Vertex vertex) {
        if (vertex.property(this.name).isPresent() || sourceRetriever == null)
            return;

        long loc;
        if (vertex.label().equals(VertexLabel.CLASS.toString())) {
            loc = countLOC(vertex);
        }else if (vertex.label().equals(VertexLabel.PACKAGE.toString())){
            vertex.graph().traversal().V(vertex)
                    .in(EdgeLabel.BELONGSTO.toString())
                    .hasNot(this.name)
                    .forEachRemaining(this::calculate);
            loc = vertex.graph().traversal().V(vertex)
                    .in(EdgeLabel.BELONGSTO.toString())
                    .has(this.name)
                    .by(this.name)
                    .sum().next().longValue();
        }else {
            return;
        }
        vertex.property(this.name, loc);

    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param edge the edge to calculate this characteristic on. The result must be stored within the edge using
     *             {@link #name} as property key.
     */
    @Override
    protected void calculate(Edge edge) {

    }

    private long countLOC(Vertex clazz){
        var g = clazz.graph().traversal();
        var parentPackage = g.V(clazz).out(EdgeLabel.BELONGSTO.toString()).next();
        var classFullName = String.join(".", parentPackage.value("name"), clazz.value("name"));

        var sourceCode = sourceRetriever.getClassSource(classFullName);
        var linesOfCode = sourceCode.split("[\n|\r]");
        return Arrays.stream(linesOfCode).filter(line -> line.length() > 0).count();

    }

}
