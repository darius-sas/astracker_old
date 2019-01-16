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
     * @param smell the CD smell to visit the characteristic on.
     */
    @Override
    public double visit(CDSmell smell) {
        return smell.getAffectedElements().size();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to visit the characteristic on.
     */
    @Override
    public double visit(HLSmell smell) {
        return smell.getOutDep().size() + smell.getInDep().size();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the UD smell to visit the characteristic on.
     */
    @Override
    public double visit(UDSmell smell) {
        return smell.getBadDep().size();
    }
}
