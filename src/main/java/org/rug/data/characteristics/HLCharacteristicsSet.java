package org.rug.data.characteristics;

import java.util.Set;

/**
 * This class is responsible for creating the characteristic set for HL smells.
 */
public class HLCharacteristicsSet extends CharacteristicsSet {


    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     *
     * @param characteristics
     */
    @Override
    protected void addSmellSpecificCharacteristics(Set<ISmellCharacteristic> characteristics) {
        characteristics.add(new NumberOfDependencies());
    }
}
