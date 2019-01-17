package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.smells.CDSmell;

public class AverageEdgeWeight extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected AverageEdgeWeight() {
        super("avrgEdgeWeight");
    }

    @Override
    public Double visit(CDSmell smell) {
        /*
         Get all the edges between the affected nodes that have EdgeLabel.DEPENDSON label, then get their Weight and
         average the value across all the retrieved edges. Return 0 if the average is not present.
         */
        return smell.getTraversalSource().V(smell.getAffectedElements())
                .bothE(EdgeLabel.DEPENDSON.toString())
                .where(__.otherV().is(P.within(smell.getAffectedElements())))
                .toStream()
                .mapToInt(edge -> Integer.parseInt(edge.property("Weight").toString()))
                .average().orElse(0);
    }
}
