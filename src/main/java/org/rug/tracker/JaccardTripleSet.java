package org.rug.tracker;

import org.rug.data.util.Triple;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

class JaccardTripleSet extends LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> {

    private Set<Long> currentIds;
    private Set<Long> nextIds;

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
        if (currentIds.contains(triple.getA().getId()) || nextIds.contains(triple.getB().getId()))
            return false;
        currentIds.add(triple.getA().getId());
        nextIds.add(triple.getB().getId());
        return super.add(triple);
    }
}
