package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.smells.CDSmell;

public class NumberOfInheritanceEdges extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected NumberOfInheritanceEdges() {
        super("numOfInheritanceEdges");
    }

    /**
     * Calculates this characteristic and returns the value computed. The value can also be retrieved later by invoking
     * the method <code>getValue()</code>
     *
     * @param smell the CD smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(CDSmell smell) {
        return smell.getTraversalSource()
                .V(smell.getAffectedElements())
                .bothE(EdgeLabel.ISCHILDOF.toString(), EdgeLabel.ISIMPLEMENTATIONOF.toString())
                .count().next().toString();
    }
}
