package org.rug.tracker;

import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Models a matching strategy to match each smell in this version with the most similar one in the next version.
 * Implementers must ensure that the matches are ordered in order of matching score.
 */
public interface ISimilarityLinker {

    /**
     * Calculates the best match for every pair of smell in the two given lists and returns an ordered list
     * of pair of smells that can be linked together according to the strategy of this similarity linker.
     * Values that do not satisfy the given threshold are also excluded.
     * @param currentVersionSmells the smells of this version
     * @param nextVersionSmells the smells of the next version
     * @return a descending sorted list of triples where the first value of the list is the current smell element,
     * the second is the next version element, and the third value of the triple is the similarity score.
     */
    LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch(List<ArchitecturalSmell> currentVersionSmells, List<ArchitecturalSmell> nextVersionSmells);

    /**
     * Returns the last best match set computed. See bestMatch(...) for more info.
     */
    LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch();

    /**
     * Returns the list of matches resulting from the computation of bestMatch(...) that is neither filtered nor sorted.
     * @return A list of triples
     */
    List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> getUnfilteredMatch();

}
