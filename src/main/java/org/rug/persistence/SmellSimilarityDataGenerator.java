package org.rug.persistence;

import org.rug.data.smells.CDSmell;
import org.rug.tracker.ASmellTracker;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates the data for the similarity scores computed by a tracker.
 */
public class SmellSimilarityDataGenerator extends DataGenerator<ASmellTracker> {

    /**
     * Intantiates this data generator.
     * @param outputFile the target outputDir file for this generator.
     */
    public SmellSimilarityDataGenerator(String outputFile) {
        super(outputFile);
    }

    /**
     * Returns the header of the underlying data.
     *
     * @return a array containing the headers.
     */
    @Override
    public String[] getHeader() {
        return new String[]{
                "currentVersion", "nextVersion",
                "curId", "curAffected", "curType", "curShape",
                "nextId", "nextAffected", "nextType", "nextShape",
                "matched",
                "similarityScore"};
    }

    /**
     * Accepts an object to serialize into a list of records.
     * This method's implementation must populate the {@link #records} protected attribute.
     *
     * @param tracker the object to serialize into records of strings.
     */
    @Override
    public void accept(ASmellTracker tracker) {
        var scorer = tracker.getScorer();
        var bestMatch = scorer.bestMatch();
        for (var triple : scorer.getUnfilteredMatch()) {
            List<String> affectedA = triple.getA().getAffectedElements().stream().map(v -> v.value("name").toString()).collect(Collectors.toList());
            List<String> affectedB = triple.getB().getAffectedElements().stream().map(v -> v.value("name").toString()).collect(Collectors.toList());

            String shapeA = triple.getA() instanceof CDSmell ? ((CDSmell) triple.getA()).getShape().toString() : "NA";
            String shapeB = triple.getB() instanceof CDSmell ? ((CDSmell) triple.getB()).getShape().toString() : "NA";

            records.add(List.of(
                    tracker.getVersionOf(triple.getA()),
                    tracker.getVersionOf(triple.getB()),
                    String.valueOf(triple.getA().getId()), affectedA.toString(), triple.getA().getType().toString(), shapeA,
                    String.valueOf(triple.getB().getId()), affectedB.toString(), triple.getB().getType().toString(), shapeB,
                    String.valueOf(bestMatch.contains(triple)),
                    triple.getC().toString()));
        }
    }
}
