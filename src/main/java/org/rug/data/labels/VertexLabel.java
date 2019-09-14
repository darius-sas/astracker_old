package org.rug.data.labels;

import org.rug.data.smells.ArchitecturalSmell;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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

    public static EnumSet<VertexLabel> allFiles(){
        return EnumSet.of(VertexLabel.CLASS, VertexLabel.CFILE, VertexLabel.HFILE);
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns the instance of VertexLabel described by the given string.
     * @param name the label as a string.
     * @return a vertex label enum value.
     */
    public static VertexLabel fromString(String name){
        return lookup.get(name);
    }

    private static final Map<String, VertexLabel> lookup = new HashMap<>();

    static
    {
        for(VertexLabel label : VertexLabel.values())
        {
            lookup.put(label.value, label);
        }
    }
}
