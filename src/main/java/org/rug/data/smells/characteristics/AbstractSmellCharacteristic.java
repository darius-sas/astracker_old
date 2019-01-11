package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.List;

/**
 * Models an abstract characteristic and groups commons methods and fields
 */
public abstract class AbstractSmellCharacteristic implements ISmellCharacteristic{
    private ArchitecturalSmell.Type targetSmellType;
    private String name;
    private double value;

    /**
     *
     * @param targetType
     * @param name
     */
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

    public double getValue() {
        return value;
    }
}
