package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.HLSmell;

public class AverageInternalPathLength extends AbstractSmellCharacteristic {
    /**
     * Sets up this smell characteristic.
     *
     */
    public AverageInternalPathLength() {
        super("avrgInternalPathLength");
    }


    @Override
    public String visit(HLSmell smell) {
        var g = smell.getAffectedGraph().traversal();
        var affectedPackage = smell.getAffectedElements().iterator().next();
        if (!affectedPackage.label().equals(VertexLabel.PACKAGE.toString()))
            return "-1";
        var inDep = smell.getInDep();
        var outDep = smell.getOutDep();

        var paths = g.V(inDep).repeat(__.out(EdgeLabel.DEPENDSON.toString(), EdgeLabel.ISCHILDOF.toString(), EdgeLabel.ISIMPLEMENTATIONOF.toString())
                            .where(__.in(EdgeLabel.BELONGSTO.toString()).is(affectedPackage)))
                .until(__.hasId(outDep)).path()
                .by(__.coalesce(__.values("Weight"), __.constant(0.0)))
                .map(__.unfold().sum()).mean().next();
         
        return paths.toString();
    }
}
