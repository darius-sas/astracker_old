package org.rug.data.characteristics;

import org.rug.data.characteristics.comps.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class ComponentCharacteristicSet {

    private Set<IComponentCharacteristic> characteristics;

    /**
     * Initializes the set of component characteristics to save in the dependency graph.
     */
    public ComponentCharacteristicSet(){
        characteristics = new LinkedHashSet<>();
        characteristics.add(new NumberOfClassesInPackage());
        characteristics.add(new NumberOfLinesOfCode());
        characteristics.add(new ChangeMetrics(ChangeMetrics.NAME));
        characteristics.add(new PCCCMetric(ChangeMetrics.NAME));
        characteristics.add(new CHOMetricPackage());
        characteristics.add(new PCPCMetric());
        characteristics.add(new TACHMetricPackage());
    }

    public Set<IComponentCharacteristic> getCharacteristicSet(){
        return characteristics;
    }
}
