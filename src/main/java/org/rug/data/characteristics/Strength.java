package org.rug.data.characteristics;

import org.rug.data.smells.UDSmell;

/**
 * This characteristic returns the DUD property of the UD smell. DUD=Degree of Unstable Dependency
 * And is the ratio BadDependencies/TotalNumOfDependencies.
 */
public class Strength extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected Strength() {
        super("strength");
    }

    @Override
    public String visit(UDSmell smell) {
        return smell.getCentre().property("DUD").orElse(NO_VALUE).toString();
    }
}
