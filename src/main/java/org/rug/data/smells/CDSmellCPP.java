package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;

public class CDSmellCPP extends CDSmell {

	public CDSmellCPP(Vertex smell) {
		super(smell, Type.CD);
	}

	@Override
	protected void setAffectedElements(Vertex smell) {
		setAffectedElements(smell.graph().traversal().V(smell)
                .choose(__.in().hasLabel(VertexLabel.CYCLESHAPE.toString()),
                        __.in().hasLabel(VertexLabel.CYCLESHAPE.toString())
                               .out().hasLabel(VertexLabel.SMELL.toString()))
                .out().hasLabel(P.within(VertexLabel.CFILE.toString(), VertexLabel.COMPONENT.toString())).toSet());
	}
	
	@Override
	protected void setLevel(Vertex smell){
        setLevel(Level.fromString(smell.value("vertexType") == "component" ? "package":"class"));
    }
	
	

}
