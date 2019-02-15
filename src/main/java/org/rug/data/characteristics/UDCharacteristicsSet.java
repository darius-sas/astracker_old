package org.rug.data.characteristics;

import org.rug.data.characteristics.smells.InstabilityGap;
import org.rug.data.characteristics.smells.Strength;

import java.util.Set;

/**
 * This class is responsible for creating the characteristic set for UD smells.
 */
public class UDCharacteristicsSet extends SmellCharacteristicsSet {

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
