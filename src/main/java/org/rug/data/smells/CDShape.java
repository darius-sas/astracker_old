package org.rug.data.smells;

public enum CDShape {
    TINY("tiny"),
    CIRCLE("circle"),
    CLIQUE("clique"),
    STAR("star"),
    CHAIN("chain");

    private String shape;

    CDShape(String shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return shape;
    }
}
