package org.rug.data.characteristics;

import org.rug.data.characteristics.smells.AffectedClassesRatio;
import org.rug.data.characteristics.smells.AfferentAffectedRatio;
import org.rug.data.characteristics.smells.AverageInternalPathLength;
import org.rug.data.characteristics.smells.EfferentAffectedRatio;

import java.util.Set;

/**
 * This class is responsible for creating the characteristic set for HL smells.
 */
public class HLCharacteristicsSet extends SmellCharacteristicsSet {


    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     *
     * @param characteristics
     */
    @Override
    protected void addSmellSpecificCharacteristics(Set<ISmellCharacteristic> characteristics) {
        characteristics.add(new AverageInternalPathLength());
        characteristics.add(new AffectedClassesRatio());
        characteristics.add(new EfferentAffectedRatio());
        characteristics.add(new AfferentAffectedRatio());
    }
}
