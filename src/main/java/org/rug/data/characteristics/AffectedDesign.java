package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This characteristic computes whether a smell at the architectural level is also present at design level.
 * For example, is a cycle only among packages, or it is also among the classes belonging to those packages?
 */
public class AffectedDesign extends AbstractSmellCharacteristic{

    public AffectedDesign() {
        super("affectedDesignLevel");
    }

    @Override
    public String visit(CDSmell smell) {
        if (smell.getLevel() == ArchitecturalSmell.Level.CLASS){
            return Level.DESIGN.toString();
        } else {
            GraphTraversalSource g = smell.getTraversalSource();
            // Get list of smells affecting the system and filter out smells that are not relevant.
            // Then check if all the affected classes of the relevant ones are contained
            // in all the packages of the given smell.
            // If so, the smell is present at both levels.
            List<ArchitecturalSmell> smellList = ArcanDependencyGraphParser.getArchitecturalSmellsIn(smell.getAffectedGraph());
            Set<ArchitecturalSmell> classLevelSmells = smellList.stream()
                    .filter(s -> s.getType() == ArchitecturalSmell.Type.CD &&
                            s.getLevel() == ArchitecturalSmell.Level.CLASS)
                    .filter(s ->
                        g.V(s.getAffectedElements())
                                .out(EdgeLabel.BELONGSTO.toString())
                                .hasLabel(VertexLabel.PACKAGE.toString())
                                .toSet().equals(smell.getAffectedElements()))
                    .collect(Collectors.toSet());

            return classLevelSmells.size() > 0 ? Level.DESIGN_AND_ARCH.toString() : Level.ARCHITECTURAL.toString();
        }
    }

    /**
     * Whether the smell is present only at architectural level (between packages)
     * or also at design level.
     */
    public enum Level{
        ARCHITECTURAL("architecturalOnly"),
        DESIGN("designOnly"),
        DESIGN_AND_ARCH("designAndArch");

        private String level;

        Level(String level){
            this.level = level;
        }

        @Override
        public String toString() {
            return level;
        }
    }
}
