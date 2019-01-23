package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;

public class Shape extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected Shape() {
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
