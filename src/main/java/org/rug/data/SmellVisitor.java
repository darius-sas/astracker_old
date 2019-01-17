package org.rug.data;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

/**
 * Models a class that visits an architectural smell
 * @param <R> the return type of the visitor
 */
public interface SmellVisitor<R> {
    /**
     * Visit the given smell.
     * @param smell the CD smell to visit.
     */
    R visit(CDSmell smell);

    /**
     * Visit the given smell.
     * @param smell the HL smell to visit.
     */
    R visit(HLSmell smell);

    /**
     * Visit the given smell.
     * @param smell the UD smell to visit.
     */
    R visit(UDSmell smell);
}
