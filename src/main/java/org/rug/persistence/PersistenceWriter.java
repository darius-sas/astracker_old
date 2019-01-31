package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class models the writing of different data on file and registers data generators.
 * This allows to implement optional method invocations for data generation, allowing
 * only registered generators to actually generate persistent data.
 */
public class PersistenceWriter {

    private final static Logger logger = LoggerFactory.getLogger(PersistenceWriter.class);
    private final static Map<Class<? extends DataGenerator>, DataGenerator> generatorInstances = new HashMap<>();

    public static void writeAllCSV(){
        generatorInstances.values().forEach(PersistenceWriter::writeCSV);
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

    /**
     * Register a new DataGenerator. If another instance of the same class is present, this operation does
     * not register the given instance.
     * @param instance the instance to use
     * @param <T> the type of the generator class
     */
    public static <T> void register(DataGenerator<T> instance){
        generatorInstances.putIfAbsent(instance.getClass(), instance);
    }

    /**
     * Sends to the instance of the registered class the given data. If no instance of that generator is found
     * no invocation is performed and this methods has no effect.
     * @param to the class of the instances that has to receive
     * @param data the data to use for the generation
     * @param <T> the type of the data to pass to the generator
     */
    @SuppressWarnings("unchecked")
    public static <T> void sendTo(Class<? extends DataGenerator<T>> to, T data){
        if (generatorInstances.containsKey(to)){
            generatorInstances.get(to).accept(data);
        }
    }

}
