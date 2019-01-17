package org.rug.tracker;

import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.List;

/**
 * Models a matching strategy to match each smell in this version with the most similar one in the next version.
 * Implementers must ensure that the matches are ordered in order of matching score.
 */
public interface ISuccessorMatcher {

    /**
     * Calculates the best match
     * @param currentVersionSmells the smells of this version
     * @param nextVersionSmells the smells of the next version
     * @return a descending sorted list of triples where the first value of the list is the current smell element,
     * the second is the next version element, and the third value of the triple is the similarity score.
     */
    List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch(List<ArchitecturalSmell> currentVersionSmells, List<ArchitecturalSmell> nextVersionSmells);

    /**
     * The suggested threshold for this matcher
     * @return the threshold for this matcher
     */
    double getThreshold();
}
