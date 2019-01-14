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
    public double calculate(HLSmell smell) {
        return smell.getInDep().size() + smell.getOutDep().size();
    }
}
