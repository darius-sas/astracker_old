package org.rug.persistence;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.characteristics.IComponentCharacteristic;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IVersion;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Writes component metrics on CSV files.
 */
public class ComponentMetricGenerator extends CSVDataGenerator<IVersion> {
    private List<String> header;
    private Set<String> characteristicsNames;

    public ComponentMetricGenerator(String outputFile) {
        super(outputFile);
        this.header = new ArrayList<>();
        header.addAll(List.of("name", "type", "version", "versionIndex"));
        characteristicsNames = new ComponentCharacteristicSet().getCharacteristicSet().stream().map(IComponentCharacteristic::getName).collect(Collectors.toCollection(LinkedHashSet::new));
        characteristicsNames.removeAll(header);
        header.addAll(characteristicsNames);
    }

    @Override
    public String[] getHeader() {
        return header.toArray(new String[0]);
    }

    @Override
    public void accept(IVersion version) {
        var g = version.getGraph().traversal();
        var versionString = version.getVersionString();
        var versionPosition = String.valueOf(version.getVersionIndex());

        var vertices = g.V().hasLabel(P.within(VertexLabel.getTypesStrings())).toSet();
        for (var vertex : vertices){
            var record = new ArrayList<String>();
            record.add(vertex.value("name"));
            record.add(vertex.label());
            record.add(versionString);
            record.add(versionPosition);
            for (String characteristicsName : characteristicsNames) {
                record.add(vertex.property(characteristicsName).orElse(0).toString());
            }
            records.add(record);
        }
    }
}
