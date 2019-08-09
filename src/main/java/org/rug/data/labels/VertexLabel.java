package org.rug.data.labels;

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

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
