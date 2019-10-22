package org.rug.data.characteristics.smells;

import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.HLSmell;

/**
 * The percentage of classes of a package affected by HL smell which depend upon packages the affected
 * package depends upon.
 */
public class EfferentAffectedRatio extends AbstractSmellCharacteristic {

    /**
     * Sets up the name of this smell characteristic.
     */
    public EfferentAffectedRatio() {
        super("efferentAffectedRatio");
    }

    @Override
    public String visit(HLSmell smell) {
        var affectedPackage = smell.getCentre();
        if (!VertexLabel.getComponentStrings().contains(affectedPackage.label())) {
            return "-1";
        }
        var outDep = smell.getClassesDependingOnEfferentPackages();
        var affectedComponents = smell.getTraversalSource().V(affectedPackage).in(EdgeLabel.BELONGSTO.toString()).count().next();
        var ratio = outDep.size() / affectedComponents.doubleValue();
        return String.format("%.2f", ratio);
    }
}
