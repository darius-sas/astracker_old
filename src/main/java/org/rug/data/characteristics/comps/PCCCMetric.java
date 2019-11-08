package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.EnumSet;

/**
 * Calculates the Percentage of Commits a Class has Changed.
 */
public class PCCCMetric extends AbstractComponentCharacteristic {

    public final static String NAME = "percCommitsClassChanged";

    private String frchMetricName;
    private long totalCommits = 2;

    public PCCCMetric(String frchMetricName) {
        super(NAME, VertexLabel.allFiles(), EnumSet.noneOf(EdgeLabel.class));
        this.frchMetricName = frchMetricName;
    }

    @Override
    public void calculate(IVersion version) {
        if (version.getVersionPosition() == 1)
            return;
        super.calculate(version);
        totalCommits++;
    }

    @Override
    protected void calculate(Vertex vertex) {
        if (vertex.property(frchMetricName).isPresent()) {
            long fcrh = vertex.value(frchMetricName);
            vertex.property(this.name, (fcrh * 100d) / totalCommits);
        }
    }

    @Override
    protected void calculate(Edge edge) {

    }
}
