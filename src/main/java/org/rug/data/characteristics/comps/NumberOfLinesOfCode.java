package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * Counts the lines of code of a given vertex (package or class) and saves the results within
 * the vertex node using {@link #name} as key.
 */
public class NumberOfLinesOfCode extends AbstractComponentCharacteristic {

    private SourceCodeRetriever sourceRetriever;

    /**
     * Instantiates the calculator of LOC.
     */
    public NumberOfLinesOfCode(){
        super("linesOfCode",
                VertexLabel.allComponents(),
                EnumSet.noneOf(EdgeLabel.class));
    }

    @Override
    public void calculate(IVersion version) {
        this.sourceRetriever = version.getSourceCodeRetriever();
        super.calculate(version);
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
    public void calculate(Vertex vertex) {
        if (vertex.property(this.name).isPresent() || sourceRetriever == null)
            return;

        long loc;
        if (vertex.label().equals(VertexLabel.CLASS.toString()) ||
                vertex.label().equals(VertexLabel.CFILE.toString()) ||
                vertex.label().equals(VertexLabel.HFILE.toString())){
            loc = countLOC(vertex);
        }else if ((vertex.label().equals(VertexLabel.PACKAGE.toString()) ||
                   vertex.label().equals(VertexLabel.COMPONENT.toString())) &&
                   vertex.edges(Direction.IN, EdgeLabel.BELONGSTO.toString()).hasNext()) {
            vertex.graph().traversal().V(vertex)
                    .in(EdgeLabel.BELONGSTO.toString())
                    .hasNot(this.name)
                    .forEachRemaining(this::calculate);
            loc = vertex.graph().traversal().V(vertex)
                    .in(EdgeLabel.BELONGSTO.toString())
                    .values(this.name)
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

    public void setSourceRetriever(SourceCodeRetriever sourceRetriever) {
        this.sourceRetriever = sourceRetriever;
    }

    private Pattern linePattern = Pattern.compile("[^\\s*].*[\\n\\r]+");
    private long countLOC(Vertex element){
        var sourceCode = sourceRetriever.getSource(element);

        var matcher = linePattern.matcher(sourceCode);
        var linesOfCode = 0;
        while(matcher.find())
            linesOfCode++;
        return linesOfCode;
    }

}
