package org.rug.data.characteristics;

import org.rug.data.smells.UDSmell;

public class Strength extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected Strength() {
        super("strength");
    }

    @Override
    public Double visit(UDSmell smell) {
        return 0d;
        //return smell.getCentre().value("DUD"); //TODO property is not found for some smells... strange
    }
}
