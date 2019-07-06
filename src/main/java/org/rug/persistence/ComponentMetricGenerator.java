package org.rug.persistence;

import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.ArrayList;

/**
 * Writes component metrics on CSV files.
 */
public class ComponentMetricGenerator extends CSVDataGenerator<IVersion> {
    public ComponentMetricGenerator(String outputFile) {
        super(outputFile);
    }

    @Override
    public String[] getHeader() {
        return new String[]{"name", "type", "version", "versionPosition", "linesOfCode", "componentType"};
    }

    @Override
    public void accept(IVersion version) {
        var g = version.getGraph().traversal();
        var versionString = version.getVersionString();
        var versionPosition = String.valueOf(version.getVersionPosition());
        g.V().hasLabel(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString())
                .has("linesOfCode")
                .forEachRemaining(vertex -> {
                    var record = new ArrayList<String>();
                    record.add(vertex.value("name"));
                    record.add(vertex.label());
                    record.add(versionString);
                    record.add(versionPosition);
                    record.add(vertex.value("linesOfCode").toString());
                    var componentType = vertex.label().equals("class") ? "ClassType" : "PackageType";
                    record.add(vertex.value(componentType).toString());
                    records.add(record);
                });
    }
}
