package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.smells.ArchitecturalSmell;

/**
 * Calculates the Parent Centrality metric as defined by Al-Mutawa et al.
 */
public class ParentCentrality extends AbstractSmellCharacteristic {

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
    public double calculate(ArchitecturalSmell smell) {
        return 0;
    }
}
