package org.rug.data.characteristics;

import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

/**
 * Calculates the Parent Centrality metric as defined by Al-Mutawa et al.
 */
public class ParentCentrality extends AbstractSmellCharacteristic<CDSmell> {

    public ParentCentrality() {
        super(ArchitecturalSmell.Type.CD, "parentCentrality");
    }


    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the smell to calculate the characteristic on.
     * @return the value computed.
     */
    @Override
    public double calculate(CDSmell smell) {
        return 0;
    }
}
