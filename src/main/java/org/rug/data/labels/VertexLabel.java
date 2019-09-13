package org.rug.data.labels;

import java.util.EnumSet;

public enum VertexLabel {
    PACKAGE("package"),
    SMELL("smell"),
    CYCLESHAPE("cycleShape"),
    CLASS("class"),    
    COMPONENT("component"),
	CFILE("CFile"),
	HFILE("HFile");


    private final String value;

    VertexLabel(String s) {
        this.value = s;
    }

    public static EnumSet<VertexLabel> allComponents(){
        return EnumSet.of(VertexLabel.CLASS, VertexLabel.PACKAGE, VertexLabel.CFILE,
                VertexLabel.HFILE, VertexLabel.COMPONENT);
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
