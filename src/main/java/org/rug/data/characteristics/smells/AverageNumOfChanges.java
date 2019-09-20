package org.rug.data.characteristics.smells;

import org.rug.data.characteristics.comps.PCCCMetric;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.GCSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * This characteristic computes the average number of times the components affected by a given smell
 * were changed (according to the underlying VCS) in relation to the previous versions.
 * This number is expressed in percentage with values ranging from 0 to 100.
 */
public class AverageNumOfChanges extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public AverageNumOfChanges() {
        super("avrgNumOfChanges");
    }

    @Override
    public String visit(CDSmell smell) {
        return visitInternal(smell);
    }

    @Override
    public String visit(HLSmell smell) {
        return visitInternal(smell);
    }

    @Override
    public String visit(UDSmell smell) {
        return visitInternal(smell);
    }

    @Override
    public String visit(GCSmell smell) {
        return visitInternal(smell);
    }

    /**
     * Computes this metric by retrieving the average number of changes in percentage of the
     * affected elements.
     * @param smell the smell to calculate this smell on.
     * @return a string representing the mean values of the PCCC metric of the affected components.
     */
    private String visitInternal(ArchitecturalSmell smell){
        var vertices = smell.getTraversalSource().V(smell.getAffectedElements());
        return String.valueOf(vertices.values(PCCCMetric.PCCCMetricName).mean().tryNext().orElse(0));
    }
}
