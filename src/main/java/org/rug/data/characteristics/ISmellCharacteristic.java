package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models a SmellCharacteristics that returns a value of type R
 */
public interface ISmellCharacteristic {
    /**
     * Calculates this characteristic and returns the value computed.
     * @param smell the CD smell to calculate the characteristic on.
     */
    double calculate(CDSmell smell);

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     * @param smell the HL smell to calculate the characteristic on.
     */
    double calculate(HLSmell smell);

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     * @param smell the UD smell to calculate the characteristic on.
     */
    double calculate(UDSmell smell);


    /**
     * Returns the name of this characteristic.
     * @return the name.
     */
    String getName();

}
