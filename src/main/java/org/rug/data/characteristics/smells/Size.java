package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * This characteristic returns the number of elements affected by a smell.
 */
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
    public String visit(CDSmell smell) {
        return String.valueOf(smell.getAffectedElements().size());
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to visit the characteristic on.
     */
    @Override
    public String visit(HLSmell smell) {
        return String.valueOf(smell.getOutDep().size() + smell.getInDep().size());
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the UD smell to visit the characteristic on.
     */
    @Override
    public String visit(UDSmell smell) {
        return String.valueOf(smell.getBadDep().size());
    }
}
