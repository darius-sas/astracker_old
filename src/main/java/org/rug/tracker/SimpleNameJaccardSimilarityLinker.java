package org.rug.tracker;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This linker uses the simple names of the classes and or packages rather their full name.
 * This makes sense because it is rather difficult that a class with the same name to be affected
 * by the same type of smell and have the other elements involved in the smell named the same.
 * In case of homonyms, the threshold of the jaccard score of the set of affected elements
 * should be sufficient to eliminate the uncertainty in selecting the appropriate successor.
 *
 * NOTE: In order to strengthen the linking, this implementation uses all the elements taking part
 * in a HL or UD smell, in contrast to the default implementation.
 */
public class SimpleNameJaccardSimilarityLinker extends JaccardSimilarityLinker {

    /**
     * Builds this linker with the given threshold.
     *
     * @param fewElementsThreshold  the threshold value to use for discarding couples with not enough similarity. This
     *                              threshold is applied when the number of affected elements is less than <code>fewElements</code>.
     * @param moreElementsThreshold the threshold to use for discarding when the affected elements are more than few elements.
     * @param fewElements
     */
    public SimpleNameJaccardSimilarityLinker(double fewElementsThreshold, double moreElementsThreshold, int fewElements) {
        super(fewElementsThreshold, moreElementsThreshold, fewElements);
    }

    /**
     * Builds a linker with few elements equal to 3, few elements threshold equal to 0.6, and more elements threshold equal to 0.75.
     * These thresholds were chose to protect smells with low elements to be excluded from calculation since variations
     * of a single element when the size is less than three has a great variation on the score computed.
     */
    public SimpleNameJaccardSimilarityLinker() {
    }

    /**
     * Visit the given smell and returns the element of a CD by class or package name.
     *
     * @param smell the CD smell to visit.
     */
    @Override
    public Set<String> visit(CDSmell smell) {
        return smell.getAffectedElementsNames().stream()
                .map(this::getSimpleName)
                .collect(Collectors.toSet());
    }

    /**
     * Visit the given smell.
     *
     * @param smell the HL smell to visit.
     */
    @Override
    public Set<String> visit(HLSmell smell) {
        Set<String> names = smell.getAffectedElementsNames();
        names.addAll(smell.getInDepNames());
        names.addAll(smell.getOutDepNames());
        names = names.stream()
                .map(this::getSimpleName)
                .collect(Collectors.toSet());
        return names;
    }

    /**
     * Visit the given smell.
     *
     * @param smell the UD smell to visit.
     */
    @Override
    public Set<String> visit(UDSmell smell) {
        Set<String> names = smell.getAffectedElementsNames();
        names.addAll(smell.getBadDepNames());
        names = names.stream().map(this::getSimpleName).collect(Collectors.toSet());
        return names;
    }

    /**
     * Returns the simple name of a class or a package.
     * @param fullName the full name in the format root.package.class.
     * @return the simple name or the same string if no '.' character is present.
     */
    private String getSimpleName(String fullName){
        var lastDot = fullName.lastIndexOf('.');
        return fullName.substring(lastDot < 0 ? 0 : lastDot + 1);
    }
}
