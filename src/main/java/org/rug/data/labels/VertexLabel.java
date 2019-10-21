package org.rug.data.labels;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final EnumSet<VertexLabel> components = EnumSet.of(COMPONENT, PACKAGE);
    private static final EnumSet<VertexLabel> types = EnumSet.of(CLASS, PACKAGE, CFILE, HFILE, COMPONENT);
    private static final EnumSet<VertexLabel> files = EnumSet.of(CLASS, CFILE, HFILE);

    private static final Set<String> componentStrings = components.stream().map(VertexLabel::toString).collect(Collectors.toSet());
    private static final Set<String> typesStrings = types.stream().map(VertexLabel::toString).collect(Collectors.toSet());
    private static final Set<String> filesStrings = files.stream().map(VertexLabel::toString).collect(Collectors.toSet());

    /**
     * Creates a enumset of all grouping elements.
     * @return a enumset of grouping elements (e.g. packages, components, etc.).
     */
    public static EnumSet<VertexLabel> allComponents(){
        return components;
    }

    /**
     * Creates a enumset of all type labels.
     * @return a new enumset of the type labels (classes, packages, cfiles, etc.).
     */
    public static EnumSet<VertexLabel> allTypes(){
        return types;
    }

    /**
     * A enumset representing all file types.
     * @return enum set of file-types.
     */
    public static EnumSet<VertexLabel> allFiles(){
        return files;
    }

    public static Set<String> getComponentStrings() {
        return componentStrings;
    }

    public static Set<String> getTypesStrings() {
        return typesStrings;
    }

    public static Set<String> getFilesStrings() {
        return filesStrings;
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
