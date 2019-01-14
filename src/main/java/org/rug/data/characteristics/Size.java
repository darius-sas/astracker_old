package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

public class Size extends AbstractSmellCharacteristic {
    /**
     * Sets up this instance.
     **/
    protected Size() {
        super("size");
    }


    /**
     * Calculates this characteristic and returns the value computed. The value can be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the CD smell to calculate the characteristic on.
     */
    @Override
    public double calculate(CDSmell smell) {
        return 0;
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the HL smell to calculate the characteristic on.
     */
    @Override
    public double calculate(HLSmell smell) {
        return 0;
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can  be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the UD smell to calculate the characteristic on.
     */
    @Override
    public double calculate(UDSmell smell) {
        return 0;
    }
}
