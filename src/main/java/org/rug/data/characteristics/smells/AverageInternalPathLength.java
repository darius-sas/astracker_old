package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.computer.traversal.step.map.ShortestPath;
import org.apache.tinkerpop.gremlin.process.traversal.P;
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
        var inDep = g.V(smell.getInDep())
                .in(EdgeLabel.ISEFFERENTOF.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                         .is(affectedPackage));
        var outDep = g.V(smell.getOutDep())
                .in(EdgeLabel.ISAFFERENTOF.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage));

        var paths = g.withComputer().V(inDep).
                shortestPath()
                .with(ShortestPath.target, __.is(P.within(outDep)))
                .with(ShortestPath.includeEdges, true)
                .with(ShortestPath.edges, __.outE(EdgeLabel.DEPENDSON.toString()))
                .toList();

        var mean = 0;

        if (paths.size() > 0)
            mean = g.inject(paths.toArray())
                .map(__.unfold().values("Weight").sum())
                .mean().next().intValue();
        return String.valueOf(mean);
    }
}
