package org.rug.data.characteristics.smells;

import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.HLSmell;

/**
 * The percentage of classes within the package HL smell that are depended upon by afferent packages.
 */
public class AfferentAffectedRatio extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public AfferentAffectedRatio() {
        super("afferentAffectedRatio");
    }

    @Override
    public String visit(HLSmell smell) {
        var affectedPackage = smell.getCentre();
        if (!VertexLabel.getComponentStrings().contains(affectedPackage.label())) {
            return "-1";
        }
        var inDep = smell.getClassesDependedUponByAfferentPackages();
        var affectedComponents = smell.getTraversalSource().V(affectedPackage).in(EdgeLabel.BELONGSTO.toString()).count().next();
        var ratio = inDep.size() / affectedComponents.doubleValue();
        return String.format("%.2f", ratio);
    }
}
