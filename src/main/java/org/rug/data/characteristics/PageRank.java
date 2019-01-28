package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

public class PageRank extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected PageRank() {
        super("pageRank");
    }

    private void visitInternal(ArchitecturalSmell smell){
        var v = PageRankVertexProgram.build().alpha(0.85).edges(__.)
        GraphTraversalSource g = smell.getTraversalSource().withComputer();
        g.V().hasLabel(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()).
    }

}
