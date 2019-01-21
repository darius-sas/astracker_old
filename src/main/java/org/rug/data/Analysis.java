package org.rug.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
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

    private static int V = 0;

    public static void writeMatchScores(Collection<? extends Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch){
        try{
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("data/jaccard-%s.csv", V++)));
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("current", "next", "jaccard"));
            for (Triple<ArchitecturalSmell, ArchitecturalSmell, Double> triple : bestMatch) {
                printer.printRecord(formatSmell(triple.getA()), formatSmell(triple.getB()), triple.getC());
            }
            printer.close();
            writer.close();
        }catch (IOException e){
            logger.error("Could not print to CSV: {}", e.getMessage());
        }

    }

    private static String formatSmell(ArchitecturalSmell smell){
        List<String> smellNodes = smell.getAffectedElements().stream().map(v -> v.value("name").toString()).collect(Collectors.toList());
        if (smell instanceof CDSmell)
            return String.format("%s %s %s %s", smell.getId(), smellNodes, smell.getType(), ((CDSmell) smell).getShape());
        return String.format("%s %s %s", smell.getId(), smellNodes, smell.getType());
    }
}
