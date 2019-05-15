package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.HLSmell;

/**
 * Calculates the ratio of classes that are dependend or dependend upon
 * by classes from afferent and efferent packages over the total
 * number of classes in the affected package.
 */
public class AffectedClassesRatio extends AbstractSmellCharacteristic {
    /**
     * Sets up this smell characteristic.
     */
    public AffectedClassesRatio() {
        super("affectedClassesRatio");
    }

    /**
     * Calculates this characteristic and returns the value computed.
     *
     * @param smell the HL smell to visit the characteristic on.
     * @return the value computed.
     */
    @Override
    public String visit(HLSmell smell) {
        var g = smell.getAffectedGraph().traversal();
        var affectedPackage = smell.getAffectedElements().iterator().next();
        if (!affectedPackage.label().equals(VertexLabel.PACKAGE.toString()))
            return "-1";
        var inDep = g.V(smell.getInDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISAFFERENTOF.toString())
                        .is(affectedPackage))
                .out(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();
        var outDep = g.V(smell.getOutDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISEFFERENTOF.toString())
                        .is(affectedPackage))
                .in(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();
        var affectedComponents = g.V(affectedPackage).in(EdgeLabel.BELONGSTO.toString()).count().next();
        var ratio = (inDep.size() + outDep.size()) / affectedComponents.doubleValue();
        return String.format("%.2f", ratio);
    }
}
