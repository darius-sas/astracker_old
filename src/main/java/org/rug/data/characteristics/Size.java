package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;

public class Size extends AbstractSmellCharacteristic<ArchitecturalSmell> {
    /**
     * Sets up the name and target smell type.
     *
     * @param targetType the target smell type
     * @param name       the name to use for this characteristic. Must be unique across the others characteristics.
     */
    protected Size(ArchitecturalSmell.Type targetType, String name) {
        super(targetType, name);
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the smell to calculate the characteristic on.
     * @return the value computed.
     */
    @Override
    public double calculate(ArchitecturalSmell smell) {
        return smell.getAffectedElements().size();
    }
}
