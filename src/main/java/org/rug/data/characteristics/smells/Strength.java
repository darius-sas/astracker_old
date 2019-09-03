package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.smells.UDSmell;

/**
 * This characteristic returns the DUD property of the UD smell. DUD=Degree of Unstable Dependency
 * And is the ratio BadDependencies/TotalNumOfDependencies.
 */
public class Strength extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public Strength() {
        super("strength");
    }

    @Override
    public String visit(UDSmell smell) {
        Vertex smellNode = smell.getSmellNodes().iterator().next();
        double dud = Double.valueOf(smellNode.value("DUD").toString()) / 100d;
        return String.valueOf(dud);
    }
}
