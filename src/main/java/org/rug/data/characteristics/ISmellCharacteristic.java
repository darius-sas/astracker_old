package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

/**
 * Models a SmellCharacteristics that returns a value of type R
 */
public interface ISmellCharacteristic<S extends ArchitecturalSmell> {
    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     * @param smell the smell to calculate the characteristic on.
     * @return the value computed.
     */
    double calculate(S smell);

    /**
     * Returns the value computed by this characteristic. This is supposed to be the same value computed and returned
     * by the <code>calculate()</code> method.
     * @return the result
     */
    double getValue();

    /**
     * Returns the name of this characteristic.
     * @return the name.
     */
    String getName();

    /**
     * Get the type of the smell that this characteristics is calculated on
     * @return the type of the smell
     */
    ArchitecturalSmell.Type getTargetSmellType();
}
