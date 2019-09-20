package org.rug.data.characteristics;

import org.rug.data.characteristics.smells.*;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of stateless characteristics that are calculated by a certain type of smell.
 * Every instance of a characteristic set will be instantiated only once.
 */
public abstract class SmellCharacteristicsSet {

    private final Set<ISmellCharacteristic> characteristics;

    public SmellCharacteristicsSet(){
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
        characteristics.add(new AverageNumOfChanges());
        characteristics.add(new OverlapRatio());
        characteristics.add(new OverlapRatio(ArchitecturalSmell.Type.CD));
        characteristics.add(new OverlapRatio(ArchitecturalSmell.Type.HL));
        characteristics.add(new OverlapRatio(ArchitecturalSmell.Type.UD));
        characteristics.add(new PageRank());
        characteristics.add(new PageRank("pageRankAvrg", x -> x.average().orElse(0)));
        characteristics.add(new NumberOfEdges());
        characteristics.add(new AffectedComponentsType());
        characteristics.add(new AverageNumOfChanges());
    }
}
