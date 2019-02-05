package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.util.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  The Jaccard coefficient measures similarity between finite sample sets,
 *  and is defined as the size of the intersection divided by the size of the union of the sample sets.
 *  This linker chooses the two smells to link by computing the Jaccard coefficient for every
 *  couple of smells and assigning the successor based on the highest similarity match between two couples.
 *  Note that the algorithm prioritises the couples with the highest similarity, hence some smells in the current
 *  version may not be assigned to the one with the highest simalirity since that smell may have already
 *  have been assigned. Moreover, smells with a similarity under the given threshold are not linked.
 */
public class JaccardSimilarityLinker implements ISimilarityLinker, SmellVisitor<Set<String>> {

    private final double fewElementsThreshold;
    private final double moreElementsThreshold;
    private final int fewElements;
    private List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> unlinkedMatchScores;
    private LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch;
    /**
     * Builds this linker with the given threshold.
     * @param fewElementsThreshold the threshold value to use for discarding couples with not enough similarity. This
     *                             threshold is applied when the number of affected elements is less than <code>fewElements</code>.
     * @param moreElementsThreshold the threshold to use for discarding when the affected elements are more than few elements.
     */
    public JaccardSimilarityLinker(double fewElementsThreshold, double moreElementsThreshold, int fewElements) {
        this.fewElementsThreshold = fewElementsThreshold;
        this.moreElementsThreshold = moreElementsThreshold;
        this.fewElements = fewElements;
        this.unlinkedMatchScores = new ArrayList<>();
    }

    /**
     * Builds a linker with few elements equal to 3, few elements threshold equal to 0.6, and more elements threshold equal to 0.75.
     * These thresholds were chose to protect smells with low elements to be excluded from calculation since variations
     * of a single element when the size is less than three has a great variation on the score computed.
     */
    public JaccardSimilarityLinker(){this(0.6, 0.75, 3);}

    /**
     * Calculates the best match for every pair of smell in the two given lists and returns an ordered list
     * of pair of smells that can be linked together according to the strategy of this similarity linker.
     * Values that do not satisfy the given threshold are also excluded.
     * @param currentVersionSmells the smells of this version
     * @param nextVersionSmells the smells of the next version
     * @return a descending sorted list of triples where the first value of the list is the current smell element,
     * the second is the next version element, and the third value of the triple is the similarity score.
     */
    @Override
    public Set<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch(List<ArchitecturalSmell> currentVersionSmells, List<ArchitecturalSmell> nextVersionSmells) {
        List<JaccardTriple> matchList = new ArrayList<>();
        for(ArchitecturalSmell s1 : currentVersionSmells) {
            for (ArchitecturalSmell s2 : nextVersionSmells) {
                if (s1.getType() == s2.getType()) {
                    double similarityScore = calculateJaccardSimilarity(s1, s2);
                    matchList.add(new JaccardTriple(s1, s2, similarityScore));
                }
            }
        }
        matchList.removeIf( t ->  {
            double variableThreshold = t.getA().getAffectedElements().size() <= fewElements ? fewElementsThreshold : moreElementsThreshold;
            return variableThreshold > t.getC();
        });
        unlinkedMatchScores.clear();
        unlinkedMatchScores.addAll(matchList);
        matchList.sort(Comparator.comparing(t -> (JaccardTriple)t).reversed());
        bestMatch = new JaccardTripleSet(matchList);

        return bestMatch;
    }

    @Override
    public Set<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch() {
        return bestMatch;
    }

    /**
     * Calculates the jaccard similarity between the given smells
     * @param smell1 the first smell
     * @param smell2 the second smell
     * @return the Jaccard similarity score of the two smells
     */
    private double calculateJaccardSimilarity(ArchitecturalSmell smell1, ArchitecturalSmell smell2){
        Set<String> A = smell1.accept(this);
        Set<String> B = smell2.accept(this);

        int intersectionSize = intersect(A,B).size();
        int denominator = (A.size() + B.size() - intersectionSize);

        return  denominator == 0 ? 0 : intersectionSize / (double)denominator;
    }

    /**
     * Intersection between a and b
     * @param a the first set
     * @param b the second set
     * @param <S> the type of the elements contained in the set
     * @return a new set containing the result of the intersection
     */
    private <S> Set<S> intersect(Set<S> a, Set<S> b){
        Set<S> c = new HashSet<>(a); c.retainAll(b); return c;
    }

    private String getName(Vertex vertex) {
        return vertex.value("name").toString();
    }

    @Override
    public List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> getUnlikedMatchScores() {
        return unlinkedMatchScores;
    }

    /**
     * Visit the given smell.
     *
     * @param smell the CD smell to visit.
     */
    @Override
    public Set<String> visit(CDSmell smell) {
        return smell.getAffectedElements().stream().map(this::getName).collect(Collectors.toSet());
    }

    /**
     * Visit the given smell.
     *
     * @param smell the HL smell to visit.
     */
    @Override
    public Set<String> visit(HLSmell smell) {
        Set<String> elements = smell.getAffectedElements().stream().map(this::getName).collect(Collectors.toSet());
        //elements.addAll(smell.getInDep().stream().map(this::getToolName).collect(Collectors.toSet()));
        //elements.add(getToolName(smell.getCentre()));
        return elements;
    }

    /**
     * Visit the given smell.
     *
     * @param smell the UD smell to visit.
     */
    @Override
    public Set<String> visit(UDSmell smell) {
        Set<String> elements = smell.getAffectedElements().stream().map(this::getName).collect(Collectors.toSet());
        //elements.add(getToolName(smell.getCentre()));
        return elements;
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
