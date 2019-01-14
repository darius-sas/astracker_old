package org.rug.data.characteristics;

import org.rug.data.smells.CDSmell;

public class AffectedDesign extends AbstractSmellCharacteristic{

    public AffectedDesign() {
        super("affectedDesignLevel");
    }

    @Override
    public double calculate(CDSmell smell) {
        return super.calculate(smell);
    }

    /**
     * Whether the cycle is present only at architectural level (between packages)
     * or also at design level.
     */
    public enum Level{
        ARCHITECTURAL(0),
        DESIGN(1);

        private double level;

        Level(double level){
            this.level = level;
        }
    }
}
