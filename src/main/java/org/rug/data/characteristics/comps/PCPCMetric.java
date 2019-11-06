package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the PCPC (Percentage of Commits Package has Changed) metric.
 */
public class PCPCMetric extends AbstractComponentCharacteristic {

    private long totalCommits = 2;
    private Map<String, Long> packageChanges;

    public PCPCMetric() {
        super("percCommitsPackChanged", VertexLabel.allComponents(), EnumSet.noneOf(EdgeLabel.class));
        packageChanges = new HashMap<>(1000);
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
        String name = vertex.value("name");
        if (vertex.value(CHOMetricPackage.NAME)){
            packageChanges.compute(name, (k, v) -> v == null ? 1L : v + 1);
        }
        vertex.property(this.name, (packageChanges.getOrDefault(name, 0L) * 100d) / totalCommits);
    }

    @Override
    protected void calculate(Edge edge) {

    }
}
