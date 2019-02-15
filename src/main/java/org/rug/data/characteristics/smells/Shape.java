package org.rug.data.characteristics.smells;

import org.rug.data.smells.CDSmell;

/**
 * This characteristic returns the shape of a cyclic dependency.
 */
public class Shape extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public Shape() {
        super("shape");
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the CD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(CDSmell smell) {
        return smell.getShape().toString();
    }
}
