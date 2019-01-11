package org.rug.data.smells.characteristics;

import org.rug.data.smells.ArchitecturalSmell;

/**
 * Models a SmellCharacteristics that returns a value of type R
 * @param <R> the type returned by the
 */
public interface ISmellCharacteristic <R> {
    R calculate(ArchitecturalSmell smell);
    R getValue();
    String getName();
    ArchitecturalSmell.Type getTargetSmellType();
}
