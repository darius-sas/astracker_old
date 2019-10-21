package org.rug.data.characteristics.smells;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.GCSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

public class AffectedComponentsType extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     *
     */
    public AffectedComponentsType() {
        super("affectedComponentType");
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the CD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(CDSmell smell) {
        return smell.getLevel().toString();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(HLSmell smell) {
        return smell.getLevel().toString();
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the UD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(UDSmell smell) {
        return smell.getLevel().toString();
    }

    /**
     * Calculates the characteristic and returns the value computed.
     * @param smell the GC smell to visit
     * @return the value computed.
     */
    public String visit(GCSmell smell){
        return smell.getLevel().toString();
    }
}
