package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;

import java.util.HashSet;
import java.util.Set;

public class GenericCharacteristicSet implements ICharacteristicsSet<ArchitecturalSmell> {
    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     *
     * @return A new instance of a set of smell characteristics.
     */
    @Override
    public Set<ISmellCharacteristic<ArchitecturalSmell>> getCharacteristicSet() {
        Set<ISmellCharacteristic<ArchitecturalSmell>> genericCharact = new HashSet<>();
        genericCharact.add(new Size(null, "size"));
        return genericCharact;
    }
}
