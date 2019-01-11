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
     * Sets up the name and target smell type.
     * @param targetType the target smell type
     * @param name the name to use for this characteristic. Must be unique across the others characteristics.
     */
    protected AbstractSmellCharacteristic(ArchitecturalSmell.Type targetType, String name){
        this.targetSmellType = targetType;
        this.name = name;
    }

    /**
     * Get the type of the smell that this characteristics is calculated on
     * @return the type of the smell
     */
    public ArchitecturalSmell.Type getTargetSmellType() {
        return targetSmellType;
    }

    /**
     * Returns the name of this characteristic
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value computed by this characteristic. This is supposed to be the same value computed and returned
     * by the <code>calculate()</code> method.
     * @return the result
     */
    public double getValue() {
        return value;
    }
}
