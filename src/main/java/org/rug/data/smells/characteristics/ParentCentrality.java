package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.smells.ArchitecturalSmell;

public class ParentCentrality extends AbstractSmellCharacteristic<Double> {

    protected ParentCentrality() {
        super(ArchitecturalSmell.Type.CD, "parentCentrality");
    }

    @Override
    public Double calculate(Graph sysGraph) {
        return null;
    }

}
