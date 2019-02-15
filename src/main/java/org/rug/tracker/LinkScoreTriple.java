package org.rug.tracker;

import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.util.Triple;

/**
 * Convenience class to represent a tracking triple and adequately compare between two triples based
 * on the smells.
 * Two triples are the same if any of the two smells are the same (respecting positions).
 */
public class LinkScoreTriple extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double> implements Comparable<LinkScoreTriple>{

    public LinkScoreTriple(ArchitecturalSmell smell, ArchitecturalSmell smell2, Double aDouble) {
        super(smell, smell2, aDouble);
    }

    /**
     * Two triples are equals if and only if any of the two smells are the same (respecting positions).
     * @param o the other triple to use for comparison
     * @return true if the triples are equals (as explained above).
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof LinkScoreTriple))
            return false;

        LinkScoreTriple other = (LinkScoreTriple)o;

        if (this == other)
            return true;

        return this.a.equals(other.a) && this.b.equals(other.b);
    }

    private int hashCode;

    /**
     * Always return zero because all triples must be able to force Sets to use {@link #equals(Object)}
     * to check their presence in the set.
     * @return Returns always 0.
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0){
            result = 31 * a.hashCode();
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
     * A linking triple is compared with another triple based uniquely on the similarity score.
     * This may cause issues when two or more smell have the same similarity score.
     * Hence, the storing collection must ensure a proper selection strategy in such cases.
     * @param o the other triple to use for comparison with this
     * @return see above.
     */
    @Override
    public int compareTo(LinkScoreTriple o) {
        return this.c.compareTo(o.c);
    }
}
