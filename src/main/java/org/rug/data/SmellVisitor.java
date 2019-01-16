package org.rug.data;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models a class that visits an architectural smell
 */
public interface SmellVisitor {
    /**
     * Visit the given smell.
     * @param smell the CD smell to visit.
     */
    double visit(CDSmell smell);

    /**
     * Visit the given smell.
     * @param smell the HL smell to visit.
     */
    double visit(HLSmell smell);

    /**
     * Visit the given smell.
     * @param smell the UD smell to visit.
     */
    double visit(UDSmell smell);
}
