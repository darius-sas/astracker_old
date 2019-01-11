package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.smells.ArchitecturalSmell;

/**
 * Calculates the Parent Centrality metric as defined by Al-Mutawa et al.
 */
public class ParentCentrality extends AbstractSmellCharacteristic<Double> {

    protected ParentCentrality() {
        super(ArchitecturalSmell.Type.CD, "parentCentrality");
    }

    @Override
    public Double calculate(ArchitecturalSmell smell) {
        return null;
    }

}
