package org.rug.tracker;

import org.rug.data.util.Triple;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A JaccardTripleSet is a set where every smell appears only once in the triples stored.
 * More precisely for every triple <A, B, C> and <A', B', C'>, A!=A' and B!=B'.
 */
class JaccardTripleSet extends LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> {

    private Set<ArchitecturalSmell> currentIds;
    private Set<ArchitecturalSmell> nextIds;

    public JaccardTripleSet(Collection<? extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> c) {
        this();
        for(Triple<ArchitecturalSmell, ArchitecturalSmell, Double> t : c){
            this.add(t);
        }
    }

    public JaccardTripleSet() {
        this.currentIds = new HashSet<>();
        this.nextIds = new HashSet<>();
    }

    @Override
    public boolean add(Triple<ArchitecturalSmell, ArchitecturalSmell, Double> triple) {
        if (currentIds.contains(triple.getA()) || nextIds.contains(triple.getB()))
            return false;
        currentIds.add(triple.getA());
        nextIds.add(triple.getB());
        return super.add(triple);
    }

}
