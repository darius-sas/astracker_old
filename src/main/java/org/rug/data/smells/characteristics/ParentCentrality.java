package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.smells.SmellType;

public class ParentCentrality extends AbstractSmellCharacteristic<Double> {

    protected ParentCentrality() {
        super(SmellType.CD, "parentCentrality");
    }

    @Override
    public Double calculate(Graph sysGraph) {
        return null;
    }

}
