package org.rug.tracker;

import org.rug.data.smells.ArchitecturalSmell;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A JaccardTripleSet is a set where every smell appears only once in the triples stored.
 * More precisely for every triple <A, B, C> and <A', B', C'>, A!=A' and B!=B'.
 */
public class JaccardTripleSet extends LinkedHashSet<LinkScoreTriple> {

    static final long serialVersionUID = 6703014914L;

    private Set<ArchitecturalSmell> current;
    private Set<ArchitecturalSmell> next;

    public JaccardTripleSet(Collection<? extends LinkScoreTriple> c) {
        this();
        addAll(c);
    }

    public JaccardTripleSet() {
        this.current = new HashSet<>();
        this.next = new HashSet<>();
    }

    /**
     * Adds a triple <A, B, C> if and only if there are no other elements <A', B', C'> in the set that have
     * either B = B' or A = A'.
     * @param triple
     * @return
     */
    @Override
    public boolean add(LinkScoreTriple triple) {
        if (current.contains(triple.getA()) || next.contains(triple.getB()))
            return false;
        current.add(triple.getA());
        next.add(triple.getB());
        return super.add(triple);
    }

    /**
     * Iteratively calls {@link #add(LinkScoreTriple)} on the given collection.
     * @param c the collection to use
     * @return true if the collection changed as a result of this call.
     */
    @Override
    public boolean addAll(Collection<? extends LinkScoreTriple> c) {
        boolean isChanged = false;
        for(LinkScoreTriple t : c){
            isChanged = this.add(t);
        }
        return isChanged;
    }

}
