package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

public class Size extends AbstractSmellCharacteristic {
    /**
     * Sets up this instance.
     **/
    protected Size() {
        super("size");
    }


    /**
     * Calculates this characteristic and returns the value computed.
     * @param smell the CD smell to calculate the characteristic on.
     */
    @Override
    public double calculate(CDSmell smell) {
        return smell.getAffectedElements().size();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to calculate the characteristic on.
     */
    @Override
    public double calculate(HLSmell smell) {
        return smell.getOutDep().size() + smell.getInDep().size();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the UD smell to calculate the characteristic on.
     */
    @Override
    public double calculate(UDSmell smell) {
        return smell.getBadDep().size();
    }
}
