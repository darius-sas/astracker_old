package org.rug.data.smells;

import java.util.HashMap;
import java.util.Map;

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

    public static CDShape getValueOf(String name){
        return lookup.get(name);
    }

    private static final Map<String, CDShape> lookup = new HashMap<>();

    static
    {
        for(CDShape shape : CDShape.values())
        {
            lookup.put(shape.shape, shape);
        }
    }
}
