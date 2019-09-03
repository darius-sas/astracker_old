package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class UDSmellCPP extends UDSmell {

	public UDSmellCPP(Vertex smell) {
		super(smell, Type.UD);
	}

	/**
	 * UD is only defined at component Level, so we set it like that by default.
	 * @param smell the smell this instance is instantiated from.
	 */
	@Override
	protected void setLevel(Vertex smell) {
		setLevel(Level.COMPONENT);
	}
}
