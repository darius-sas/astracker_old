package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models an abstract characteristic and groups commons methods and fields
 */
public abstract class AbstractSmellCharacteristic implements ISmellCharacteristic{
    private String name;
    protected final static String NO_VALUE = "0";

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
     * @param smell the CD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(CDSmell smell) {
        return NO_VALUE;
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the HL smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(HLSmell smell) {
        return NO_VALUE;
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the UD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(UDSmell smell) {
        return NO_VALUE;
    }
}
