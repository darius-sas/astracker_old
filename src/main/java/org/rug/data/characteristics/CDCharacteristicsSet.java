package org.rug.data.characteristics;

import org.rug.data.characteristics.smells.*;

import java.util.Set;

/**
 * This class is responsible for creating the characteristic set for UD smells.
 */
public class CDCharacteristicsSet extends SmellCharacteristicsSet {

    /**
     * Instantiates a new set of characteristics that can be used to save their calculations.
     */
    @Override
    protected void addSmellSpecificCharacteristics(Set<ISmellCharacteristic> characteristics) {
        characteristics.add(new NumberOfInheritanceEdges());
        characteristics.add(new NumberOfPrivateUseEdges());
        characteristics.add(new NumberOfPublicUseEdges());
        characteristics.add(new AverageEdgeWeight());
        characteristics.add(new AffectedDesign());
        characteristics.add(new ParentCentrality());
        characteristics.add(new Shape());
    }
}
