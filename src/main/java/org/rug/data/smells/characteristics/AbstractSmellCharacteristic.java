package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.List;

/**
 * Models an abstract characteristic and groups commons methods and fields
 * @param <R> the type returned by the characteristic
 */
public abstract class AbstractSmellCharacteristic<R> implements ISmellCharacteristic<R>{
    private ArchitecturalSmell.Type targetSmellType;
    private String name;
    private R value;


    protected AbstractSmellCharacteristic(ArchitecturalSmell.Type targetType, String name){
        this.targetSmellType = targetType;
        this.name = name;
    }

    public ArchitecturalSmell.Type getTargetSmellType() {
        return targetSmellType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public R getValue() {
        return value;
    }
}
