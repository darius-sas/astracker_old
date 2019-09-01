package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class UDSmellCPP extends UDSmell {

	public UDSmellCPP(Vertex smell) {
		super(smell, Type.UD);
	}

	@Override
	protected void setLevel(Vertex smell){
		setLevel(Level.fromString(smell.value("vertexType") == "component" ? "package":"class"));
	}
}
