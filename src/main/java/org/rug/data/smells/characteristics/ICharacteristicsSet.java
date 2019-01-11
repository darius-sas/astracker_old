package org.rug.data.smells.characteristics;

import java.util.Set;

/**
 * Represents a set of characteristics that are calculated by a certain type of smell.
 */
public interface ICharacteristicsSet<R> {
    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     * @return A new instance of a set of smell characteristics.
     */
    Set<ISmellCharacteristic<R>> getCharacteristicSet();
}
