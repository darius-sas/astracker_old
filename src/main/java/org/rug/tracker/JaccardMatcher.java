package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *  The Jaccard coefficient measures similarity between finite sample sets,
 *  and is defined as the size of the intersection divided by the size of the union of the sample sets.
 *  This matcher chooses between two smells by comparing the set of their composing elements.
 */
public class JaccardMatcher implements ISuccessorMatcher, SmellVisitor<Set<String>> {

    private final double THRESHOLD;

    public JaccardMatcher(double THRESHOLD) {
        this.THRESHOLD = THRESHOLD;
    }

    public JaccardMatcher(){this(0.8);}

    /**
     * Calculates the best match
     * @param currentVersionSmells the smells of this version
     * @param nextVersionSmells the smells of the next version
     * @return a descending sorted list of triples where the first value of the list is the current smell element,
     * the second is the next version element, and the third value of the triple is the similarity score.
     */
    @Override
    public List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch(List<ArchitecturalSmell> currentVersionSmells, List<ArchitecturalSmell> nextVersionSmells) {
        List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> matchList = new ArrayList<>();
        for(ArchitecturalSmell s1 : currentVersionSmells) {
            for (ArchitecturalSmell s2 : nextVersionSmells) {
                if (s1.getType() == s2.getType()) {
                    double similarityScore = calculateJaccardSimilarity(s1, s2);
                    if (similarityScore >= getThreshold())
                        matchList.add(new Triple<>(s1, s2, similarityScore));
                }
            }
        }

        matchList.sort(Comparator.comparingDouble(t -> ((Triple<?,?, Double>)t).getC()).reversed());
        return matchList;
    }

    /**
     * The suggested threshold for this matcher
     *
     * @return the threshold for this matcher
     */
    @Override
    public double getThreshold() {
        return THRESHOLD;
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

        double intersectionSize = intersect(A,B).size();

        return  intersectionSize / (A.size() + B.size() - intersectionSize);
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

    private Function<Vertex, String> getName() {
        return vertex -> vertex.value("name").toString();
    }

    /**
     * Visit the given smell.
     *
     * @param smell the CD smell to visit.
     */
    @Override
    public Set<String> visit(CDSmell smell) {
        return smell.getAffectedElements().stream().map(getName()).collect(Collectors.toSet());
    }

    /**
     * Visit the given smell.
     *
     * @param smell the HL smell to visit.
     */
    @Override
    public Set<String> visit(HLSmell smell) {
        Set<String> elements = smell.getOutDep().stream().map(getName()).collect(Collectors.toSet());
        elements.addAll(smell.getInDep().stream().map(getName()).collect(Collectors.toSet()));
        elements.add(getName().apply(smell.getCentre()));
        return elements;
    }

    /**
     * Visit the given smell.
     *
     * @param smell the UD smell to visit.
     */
    @Override
    public Set<String> visit(UDSmell smell) {
        Set<String> elements = smell.getBadDep().stream().map(getName()).collect(Collectors.toSet());
        elements.add(getName().apply(smell.getCentre()));
        return elements;
    }
}
