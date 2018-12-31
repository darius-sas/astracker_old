package org.rug.data;

public enum VertexLabel {
    PACKAGE("package"),
    SMELL("smell"),
    CYCLESHAPE("cycleShape"),
    CLASS("class");


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
