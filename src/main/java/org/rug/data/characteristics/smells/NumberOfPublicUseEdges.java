package org.rug.data.characteristics.smells;

// Need to modify arcan in order to retrieve this information

/**
 * (Not implemented yet) This characteristic computes the number of public usages of the classes affected by this smell.
 */
public class NumberOfPublicUseEdges extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    public NumberOfPublicUseEdges() {
        super("numOfPublicUseEdges");
    }
}
