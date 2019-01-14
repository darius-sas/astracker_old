package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models an abstract characteristic and groups commons methods and fields
 */
public abstract class AbstractSmellCharacteristic implements ISmellCharacteristic{
    private String name;

    /**
     * Sets up the name of this smell characteristic.
     * @param name the name to use for this characteristic. Must be unique across the others characteristics.
     */
    protected AbstractSmellCharacteristic(String name){
        this.name = name;
    }


    /**
     * Returns the name of this characteristic
     * @return the name.
     */
    public String getName() {
        return name;
    }


    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the CD smell to calculate the characteristic on.
     * @return the value computed.
     */
    @Override
    public double calculate(CDSmell smell) {
        throw new UnsupportedOperationException("This operation is not possible on this type of smell.");
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the HL smell to calculate the characteristic on.
     * @return the value computed.
     */
    @Override
    public double calculate(HLSmell smell) {
        throw new UnsupportedOperationException("This operation is not possible on this type of smell.");
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the UD smell to calculate the characteristic on.
     * @return the value computed.
     */
    @Override
    public double calculate(UDSmell smell) {
        throw new UnsupportedOperationException("This operation is not possible on this type of smell.");
    }
}
