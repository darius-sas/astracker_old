package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.characteristics.HLCharacteristicsSet;
import org.rug.data.characteristics.ISmellCharacteristic;
import org.rug.data.characteristics.UDCharacteristicsSet;
import org.rug.data.labels.EdgeLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Unstable dependency smell.
 */
public class UDSmell extends SingleElementSmell {

    private Set<Vertex> badDep;

    /**
     * Builds an architectural smell instance of a UD smell starting from the given vertex.
     * @param smell the vertex to use.
     */
    public UDSmell(Vertex smell) {
        super(smell, Type.UD);
        this.badDep = smell.graph().traversal().V(smell).out(EdgeLabel.UDBADDEP.toString()).toSet();
    }

    @Override
    public void setAffectedElements(Vertex smell) {
        this.affectedElements = new HashSet<>();
        this.affectedElements.add(smell.graph().traversal().V(smell).out(EdgeLabel.UDAFFECTED.toString()).next());
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getBadDep() {
        return Collections.unmodifiableSet(badDep);
    }

    /**
     * UD is only defined at package Level, so we set it like that by default
     * @param smell
     */
    @Override
    protected void setLevel(Vertex smell) {
        setLevel(Level.PACKAGE);
    }

    @Override
    protected void calculateCharacteristicsInternal() {
        UDCharacteristicsSet cSet = (UDCharacteristicsSet)getCharacteristicsSet();
        Set<ISmellCharacteristic<UDSmell>> characteristicsSets = cSet.getCharacteristicSet();
        for (ISmellCharacteristic<UDSmell> characteristic : characteristicsSets){
            characteristicsMap.put(characteristic.getName(), characteristic.calculate(this));
        }
    }
}
