package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.rug.data.util.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.tracker.ASTracker2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Analysis {

    private final static Logger logger = LoggerFactory.getLogger(Analysis.class);

    public static void writeMatchScores(Collection<? extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> matches,
                                        Collection<? extends  Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch,
                                        String version){
        try{

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("data/jaccard-%s.csv", version)));
            CSVPrinter printer = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.withHeader("curID", "curAffected", "curType", "curShape",
                                                 "nextId", "nextAffected", "nextType", "nextShape",
                                                 "match",
                                                 "jaccard"));

            for (Triple<ArchitecturalSmell, ArchitecturalSmell, Double> triple : matches) {
                List<String> affectedA = triple.getA().getAffectedElements().stream().map(v -> v.value("name").toString()).collect(Collectors.toList());
                List<String> affectedB = triple.getB().getAffectedElements().stream().map(v -> v.value("name").toString()).collect(Collectors.toList());

                String shapeA = triple.getA() instanceof CDSmell ? ((CDSmell) triple.getA()).getShape().toString() : "NA";
                String shapeB = triple.getB() instanceof CDSmell ? ((CDSmell) triple.getB()).getShape().toString() : "NA";

                printer.printRecord(
                        String.valueOf(triple.getA().getId()), affectedA, triple.getA().getType().toString(), shapeA,
                        String.valueOf(triple.getB().getId()), affectedB, triple.getB().getType().toString(), shapeB,
                        bestMatch.contains(triple),
                        triple.getC());
            }
            printer.close();
            writer.close();
        }catch (IOException e){
            logger.error("Could not print to CSV: {}", e.getMessage());
        }

    }

    public static void recordScorer(ASTracker2 tracker){

    }

}
