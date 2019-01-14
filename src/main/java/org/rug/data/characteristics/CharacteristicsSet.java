package org.rug.data.characteristics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of stateless characteristics that are calculated by a certain type of smell.
 * Every instance of a characteristic set will be instantiated only once.
 */
public abstract class CharacteristicsSet {

    private final Set<ISmellCharacteristic> characteristics;

    public CharacteristicsSet(){
        Set<ISmellCharacteristic> characteristics = new HashSet<>();
        addSmellGenericCharacteristics(characteristics);
        addSmellSpecificCharacteristics(characteristics);
        this.characteristics = Collections.unmodifiableSet(characteristics);
    }


    /**
     * Returns the sets of characteristics.
     * @return an unmodifiable set of characteristics.
     */
    public final Set<ISmellCharacteristic> getCharacteristicSet(){
        return characteristics;
    }

    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     * @param characteristics the set to add the characteristics to
     */
    protected abstract void addSmellSpecificCharacteristics(Set<ISmellCharacteristic> characteristics);

    /**
     * Instantiates the smell-generic characteristics and adds them to the given set
     * @param characteristics the set to add the characteristics to.
     */
    private void addSmellGenericCharacteristics(Set<ISmellCharacteristic> characteristics){
        characteristics.add(new Size());
    }
}
