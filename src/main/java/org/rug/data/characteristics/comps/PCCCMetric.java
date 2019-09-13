package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * Characteristics that calculates the metric PCCC (Percentage of Commits a Class has Changed)
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    private final static Logger logger = LoggerFactory.getLogger(PCCCMetric.class);

    private SourceCodeRetriever retriever;

    public PCCCMetric() {
        super("pccc",
                VertexLabel.allComponents(),
                EnumSet.noneOf(EdgeLabel.class));
    }

    @Override
    public void calculate(IVersion version) {
        retriever = version.getSourceCodeRetriever();
        super.calculate(version);
    }

    @Override
    protected void calculate(Vertex vertex) {
        var pathFile = retriever.getPathOf(vertex);
    }

    @Override
    protected void calculate(Edge edge) {

    }

}
