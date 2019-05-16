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
     * @param sourceCodeRetriever an object that retrieves the source code of a given class. If null, no
     *                            characteristic that requires the source code is computed.
     */
    public ComponentCharacteristicSet(ClassSourceCodeRetriever sourceCodeRetriever){
        characteristics = new HashSet<>();
        characteristics.add(new NumberOfClassesInPackage());
        if (sourceCodeRetriever != null) {
            characteristics.add(new NumberOfLinesOfCode(sourceCodeRetriever));
        }
    }

    public Set<IComponentCharacteristic> getCharacteristicSet(){
        return characteristics;
    }
}
