package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.labels.VertexLabel;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Cyclic Dependency smell.
 */
public class CDSmell extends ArchitecturalSmell {

    public static final String VISITED_SMELL_NODE = "visitedSmellNode";
    protected Shape shape;
    protected Vertex shapeVertex;


    /**
     * Builds an architectural smell instance of a CD smell starting from the given vertex.
     * @param smell the vertex to use.
     */
    public CDSmell(Vertex smell){
        super(smell, Type.CD);
        setShape(smell);
    }

    /**
     * Returns the Vertex describing the shape of this smell. Some types of CD smell may not have a shape, null is returned in such cases.
     * @return the shape vertex of this smell.
     */
    public Vertex getShapeVertex() {
        return shapeVertex;
    }

    /**
     * Conveninence method that returns the shape of this smell as an enum.
     * @return the shape of the smell.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Tries to retrieve the shape type by walking to the shape node starting from the given smell node.
     * @param smell The smell node to start walking from
     */
    private void setShape(Vertex smell){
        //TODO a smell node can be part of two cycles, need to find a way to distinguish between the two
        this.shapeVertex = smell.graph().traversal().V(smell)
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .not(__.has(VISITED_SMELL_NODE, "true"))
                .tryNext().orElse(null);
        if (this.shapeVertex == null){
            this.shape = Shape.UNCLASSIFIED;
        }else {
            this.shape = Shape.fromString(shapeVertex.value("shapeType"));
        }
        if (Shape.getMultipleSmellNodesShapes().contains(this.shape)){
            this.smellNodes.forEach(vertex -> vertex.property(VISITED_SMELL_NODE, "true"));
            smell.property(VISITED_SMELL_NODE, "false");
        }
    }


    @Override
    protected void setAffectedElements(Vertex smell) {
        setAffectedElements(smell.graph().traversal().V(smell)
                .choose(__.in().hasLabel(VertexLabel.CYCLESHAPE.toString()),
                        __.in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                               .out().hasLabel(VertexLabel.SMELL.toString()))
                .out().hasLabel(P.within(VertexLabel.CLASS.toString(), VertexLabel.PACKAGE.toString())).toSet());
    }

    @Override
    protected void setSmellNodes(Vertex smell) {
        setSmellNodes(smell.graph().traversal().V(smell)
                .choose(__.in().hasLabel(VertexLabel.CYCLESHAPE.toString()),
                        __.in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                                .out().hasLabel(VertexLabel.SMELL.toString()),
                        __.V(smell)).toSet());
    }

    @Override
    public <R> R accept(SmellVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CDSmell)
                return super.equals(o) && ((CDSmell)o).shape == this.shape;
        else
            return false;
    }

    /**
     * Lists the shapes that are currently supported for detection.
     */
    public enum Shape {
        TINY("tiny"),
        CIRCLE("circle"),
        CLIQUE("clique"),
        STAR("star"),
        CHAIN("chain"),
        UNCLASSIFIED("unclassified");

        /**
         * Returns the shapes that are represented by multiple smell vertices.
         * @return A enum set.
         */
        public static Set<Shape> getMultipleSmellNodesShapes(){
            return EnumSet.of(STAR, CHAIN);
        }

        private String shape;

        Shape(String shape) {
            this.shape = shape;
        }

        @Override
        public String toString() {
            return shape;
        }

        /**
         * Retrieves the shape enum from a string.
         * @param name the string
         * @return the shape as an enum value. Null is returned if no shape with the given name is found.
         */
        public static Shape fromString(String name){
            return lookup.get(name);
        }

        private static final Map<String, Shape> lookup = new HashMap<>();

        static
        {
            for(Shape shape : Shape.values())
            {
                lookup.put(shape.shape, shape);
            }
        }
    }
}
