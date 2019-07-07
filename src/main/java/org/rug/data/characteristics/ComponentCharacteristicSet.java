package org.rug.data.characteristics;

import org.rug.data.characteristics.comps.ClassSourceCodeRetriever;
import org.rug.data.characteristics.comps.NumberOfClassesInPackage;
import org.rug.data.characteristics.comps.NumberOfLinesOfCode;

import java.util.HashSet;
import java.util.Set;

public class ComponentCharacteristicSet {

    private Set<IComponentCharacteristic> characteristics;

    /**
     * Initializes the set of component characteristics to save in the dependency graph.
     */
    public ComponentCharacteristicSet(){
        characteristics = new HashSet<>();
        characteristics.add(new NumberOfClassesInPackage());
        characteristics.add(new NumberOfLinesOfCode());
    }

    public Set<IComponentCharacteristic> getCharacteristicSet(){
        return characteristics;
    }
}
