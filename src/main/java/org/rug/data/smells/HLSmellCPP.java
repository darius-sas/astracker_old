package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class HLSmellCPP extends HLSmell {

	public HLSmellCPP(Vertex smell) {
		super(smell, Type.HL);
	}

	@Override
	protected void setLevel(Vertex smell){
		setLevel(Level.fromString(smell.value("vertexType") == "component" ? "package":"class"));
	}

}
