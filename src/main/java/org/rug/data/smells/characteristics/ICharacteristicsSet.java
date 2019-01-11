package org.rug.data.smells.characteristics;

import java.util.Set;

/**
 * Represents a set of characteristics that are calculated by a certain type of smell.
 */
public interface ICharacteristicsSet<R> {
    Set<ISmellCharacteristic<R>> getCharacteristicSet();
}
