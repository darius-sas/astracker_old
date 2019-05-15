package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;

import java.util.Arrays;
import java.util.EnumSet;

public class NumberOfLinesOfCodeClass extends AbstractComponentCharacteristic {

    ClassSourceCodeRetriever sourceRetriever;

    public NumberOfLinesOfCodeClass(ClassSourceCodeRetriever sourceRetriever){
        super("linesOfCode", EnumSet.of(VertexLabel.CLASS), EnumSet.noneOf(EdgeLabel.class));
        this.sourceRetriever = sourceRetriever;
    }

    /**
     * This method is applied to every vertex that has the label given during instantiation.
     *
     * @param vertex the vertex to calculate this characteristic on. The result must be stored within the vertex using
     *               {@link #name} as property key.
     */
    @Override
    protected void calculate(Vertex vertex) {
        if (vertex.label().equals(VertexLabel.PACKAGE.toString())){
            // recursively sum all the LOCs of the subpackages and classes
            // stop if LOC is already counted
        }
        var g = vertex.graph().traversal();
        var parentPackage = g.V(vertex).out(EdgeLabel.BELONGSTO.toString()).next();
        var classFullName = String.join(".", parentPackage.value("name"), vertex.value("name"));

        var sourceCode = sourceRetriever.getClassSource(classFullName);
        var linesOfCode = sourceCode.split("[\n|\r]");
        var nbLocCount = Arrays.stream(linesOfCode).filter(line -> line.length() > 0).count();
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


}
