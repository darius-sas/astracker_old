package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class models the writing of different data on file and registers data generators.
 * This allows to implement optional method invocations for data generation, allowing
 * only registered generators to actually generate persistent data.
 */
public class PersistenceWriter {

    private final static Logger logger = LoggerFactory.getLogger(PersistenceWriter.class);
    private final static Map<Class<? extends IDataGenerator>, IDataGenerator> generatorInstances = new HashMap<>();

    public static void writeAllCSV(){
        generatorInstances.values().stream().filter(g -> g instanceof ICSVGenerator).map(g -> (ICSVGenerator)g).forEach(PersistenceWriter::writeCSV);
    }

    public static void writeAllGraphs(){
        generatorInstances.values().stream().filter(g -> g instanceof IGraphGenerator).map(g -> (IGraphGenerator)g).forEach(PersistenceWriter::writeGraphs);

    }

    public static void writeCSV(ICSVGenerator csvGenerator){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvGenerator.getOutputFile()));
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(csvGenerator.getHeader()));

            printer.printRecords(csvGenerator);

            printer.close();
            writer.close();
        }catch (IOException e){
            logger.error("Could not print to CSV: {}", e.getMessage());
        }
    }

    public static void writeGraphs(IGraphGenerator graphGenerator){
        graphGenerator.getGraph().traversal().io(graphGenerator.getOutputFile().getAbsolutePath()).write().iterate();
    }

    /**
     * Register a new CSVDataGenerator. If another instance of the same class is present, this operation does
     * not register the given instance.
     * @param instance the instance to use
     */
    public static void register(IDataGenerator instance){
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
    public static <T> void sendTo(Class<? extends IDataGenerator<T>> to, T data){
        if (generatorInstances.containsKey(to)){
            generatorInstances.get(to).accept(data);
        }
    }

}
