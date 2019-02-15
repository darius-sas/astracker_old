package org.rug.data.characteristics.smells;

import org.rug.data.characteristics.CharacteristicsSet;
import org.rug.data.characteristics.ISmellCharacteristic;

import java.util.Set;

/**
 * This class is responsible for creating the characteristic set for UD smells.
 */
public class UDCharacteristicsSet extends CharacteristicsSet {

    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     *
     * @param characteristics
     */
    @Override
    protected void addSmellSpecificCharacteristics(Set<ISmellCharacteristic> characteristics) {
        characteristics.add(new Strength());
        characteristics.add(new InstabilityGap());
    }
}
