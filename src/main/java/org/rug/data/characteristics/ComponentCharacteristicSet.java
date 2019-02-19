package org.rug.data.characteristics;

import org.rug.data.characteristics.comps.NumberOfClassesInPackage;

import java.util.HashSet;
import java.util.Set;

public class ComponentCharacteristicSet {

    private Set<IComponentCharacteristic> characteristics;

    public ComponentCharacteristicSet(){
        characteristics = new HashSet<>();
        characteristics.add(new NumberOfClassesInPackage());
    }

    public Set<IComponentCharacteristic> getCharacteristicSet(){
        return characteristics;
    }
}