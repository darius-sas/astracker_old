package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

/**
 * Calculates the Parent Centrality metric as defined by Al-Mutawa et al.
 */
public class ParentCentrality extends AbstractSmellCharacteristic {

    public ParentCentrality() {
        super("parentCentrality");
    }


    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the smell to calculate the characteristic on.
     */
    @Override
    public double calculate(CDSmell smell) {
        return 0;
    }


}
