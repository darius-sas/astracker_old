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
        addAll(c);
    }

    public JaccardTripleSet() {
        this.currentIds = new HashSet<>();
        this.nextIds = new HashSet<>();
    }

    /**
     * Adds a triple <A, B, C> if and only if there are no other elements <A', B', C'> in the set that have
     * either B = B' or A = A'.
     * @param triple
     * @return
     */
    @Override
    public boolean add(Triple<ArchitecturalSmell, ArchitecturalSmell, Double> triple) {
        if (currentIds.contains(triple.getA()) || nextIds.contains(triple.getB()))
            return false;
        currentIds.add(triple.getA());
        nextIds.add(triple.getB());
        return super.add(triple);
    }

    /**
     * Iteratively calls {@link #add(Triple)} on the given collection.
     * @param c the collection to use
     * @return true if the collection changed as a result of this call.
     */
    @Override
    public boolean addAll(Collection<? extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> c) {
        boolean isChanged = false;
        for(Triple<ArchitecturalSmell, ArchitecturalSmell, Double> t : c){
            isChanged = this.add(t);
        }
        return isChanged;
    }

    /**
     * Convenience class to represent a Jaccard triple and adequately compare between to triples based
     * on the smells.
     * Two JaccardTriples are the same if any of the two smells are the same (respecting positions).
     */
    static class JaccardTriple extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double> implements Comparable<JaccardTriple>{

        public JaccardTriple(ArchitecturalSmell smell, ArchitecturalSmell smell2, Double aDouble) {
            super(smell, smell2, aDouble);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof JaccardTriple))
                return false;

            JaccardTriple other = (JaccardTriple)o;

            if (this == other)
                return true;

            return this.a.equals(other.a) && this.b.equals(other.b);
        }

        private int hashCode;
        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0){
                result = a.hashCode();
                result = 31 * result + b.hashCode();
                hashCode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            return String.format("A: %s\nB: %s\n C: %f", a.getId(), b.getId(), c);
        }

        /**
         * A Jaccard triple is compared with another triple based uniquely on the similarity score.
         * This may cause issues when two or more smell have the same similarity score.
         * @param o the other triple to use for comparison with this
         * @return see above.
         */
        @Override
        public int compareTo(JaccardTriple o) {
            return this.c.compareTo(o.c);
        }
    }
}
