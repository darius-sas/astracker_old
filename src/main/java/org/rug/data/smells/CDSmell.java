package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;

import java.util.Set;

public abstract class CDSmell extends ArchitecturalSmell {

    protected CDShape shape;
    protected Vertex shapeVertex;


    protected CDSmell(Vertex smell){
        super(smell);
        assert smell.label().equals(VertexLabel.SMELL.toString());
        this.shapeVertex = smell.graph().traversal().V(smell).in().hasLabel(VertexLabel.CYCLESHAPE.toString()).next();
        setShape(this.shapeVertex);
    }

    public Vertex getShapeVertex() {
        return shapeVertex;
    }

    public CDShape getShape() {
        return shape;
    }

    private void setShape(Vertex shapeVertex){
        this.shape = CDShape.valueOf(shapeVertex.value("shapeType"));
    }

    @Override
    public void setAffectedElements(Vertex smell) {
        setAffectedElements(smell.graph().traversal().V(smell)
                    .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                    .out().hasLabel(VertexLabel.SMELL.toString())
                    .out().hasLabel(P.within(VertexLabel.CLASS.toString(), VertexLabel.PACKAGE.toString())).toSet());
    }

    @Override
    public void setSmellNodes(Vertex smell) {
        setSmellNodes(smell.graph().traversal().V(smell)
                .in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                .out().hasLabel(VertexLabel.SMELL.toString()).toSet());
    }
}
