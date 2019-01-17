package org.rug.data.characteristics;

import org.rug.data.SmellVisitor;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models a SmellCharacteristics that returns a value of type R
 */
public interface ISmellCharacteristic extends SmellVisitor<Double> {

    /**
     * Returns the name of this characteristic.
     * @return the name.
     */
    String getName();

}
