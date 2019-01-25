package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models the writing of different data on file.
 */
public class PersistenceWriter {

    private final static Logger logger = LoggerFactory.getLogger(PersistenceWriter.class);

    private final static List<DataGenerator> csvGenerators = new ArrayList<>();

    public static void  addCSVGenerator(DataGenerator generator){
        csvGenerators.add(generator);
    }

    public static void writeAllCSV(){
        csvGenerators.forEach(PersistenceWriter::writeCSV);
    }

    public static void writeCSV(DataGenerator dataGenerator){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataGenerator.getOutputFile()));
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(dataGenerator.getHeader()));

            printer.printRecords(dataGenerator);

            printer.close();
            writer.close();
        }catch (IOException e){
            logger.error("Could not print to CSV: {}", e.getMessage());
        }
    }

}
