package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.smells.CDSmell;

/**
 * This characteristic computes the average weight among the edges of the elements affected by a smell.
 */
public class AverageEdgeWeight extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public AverageEdgeWeight() {
        super("avrgEdgeWeight");
    }

    @Override
    public String visit(CDSmell smell) {
        return String.valueOf(smell.getTraversalSource().V(smell.getAffectedElements())
                .bothE(EdgeLabel.DEPENDSON.toString(), EdgeLabel.PACKAGEISAFFERENTOF.toString())
                .where(__.otherV().is(P.within(smell.getAffectedElements())))
                .toStream()
                .mapToInt(edge -> Integer.parseInt(edge.value("weight").toString()))
                .average().orElse(0));
    }
}
