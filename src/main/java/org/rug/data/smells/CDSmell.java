package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;

import java.util.HashMap;
import java.util.Map;

public class CDSmell extends ArchitecturalSmell {

    protected Shape shape;
    protected Vertex shapeVertex;


    protected CDSmell(Vertex smell){
        super(smell, Type.CD);
        setShape(smell);
    }

    public Vertex getShapeVertex() {
        return shapeVertex;
    }

    public Shape getShape() {
        return shape;
    }

    /**
     * Tries to retrieve the shape type by walking to the shape node starting from the given smell node.
     * @param smell The smell node to start walking from
     */
    private void setShape(Vertex smell){
        this.shapeVertex = smell.graph().traversal().V(smell)
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .not(__.has("visitedStar", "true"))
                .tryNext().orElse(null);
        if (this.shapeVertex == null){
            this.shape = Shape.UNKNOWN;
        }else {
            this.shape = Shape.fromString(shapeVertex.value("shapeType"));
        }
        if (this.shape == Shape.STAR){
            getSmellNodes().forEach(vertex -> vertex.property("visitedStar", "true"));
        }
    }

    @Override
    public void setAffectedElements(Vertex smell) {
        setAffectedElements(smell.graph().traversal().V(smell)
                .choose(__.in().hasLabel(VertexLabel.CYCLESHAPE.toString()),
                        __.in().hasLabel(VertexLabel.CYCLESHAPE.toString()))
                .out().hasLabel(P.within(VertexLabel.CLASS.toString(), VertexLabel.PACKAGE.toString())).toSet());
    }

    @Override
    public void setSmellNodes(Vertex smell) {
        setSmellNodes(smell.graph().traversal().V(smell)
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .out().hasLabel(VertexLabel.SMELL.toString()).toSet());
    }

    public enum Shape {
        TINY("tiny"),
        CIRCLE("circle"),
        CLIQUE("clique"),
        STAR("star"),
        CHAIN("chain"),
        UNKNOWN("unknown");

        private String shape;

        Shape(String shape) {
            this.shape = shape;
        }

        @Override
        public String toString() {
            return shape;
        }

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
