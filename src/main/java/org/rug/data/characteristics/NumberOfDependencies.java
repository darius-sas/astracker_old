package org.rug.data.characteristics;

import org.rug.data.smells.HLSmell;

public class NumberOfDependencies extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected NumberOfDependencies() {
        super("totalNumOfDependencies");
    }

    @Override
    public Double visit(HLSmell smell) {
        return (double) (smell.getInDep().size() + smell.getOutDep().size());
    }
}
