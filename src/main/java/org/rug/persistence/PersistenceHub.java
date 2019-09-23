package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * This class shuts a message consisting of objects or data to a specific object of a specific class.
 * This allows to implement optional method invocations for data generation, allowing
 * only registered generators to actually generate persistent data.
 */
public class PersistenceHub {

    private final static Logger logger = LoggerFactory.getLogger(PersistenceHub.class);
    private final static Map<Class<? extends IDataGenerator>, IDataGenerator> generatorInstances = new HashMap<>();

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

    /**
     * Sends to the instance of the registered class the given data and then invokes the writing on file method
     * on the instance of the given generator. If no instance of that generator is found
     * no invocation is performed and this methods has no effect.
     * To ensure the data has been properly written on file, {@link #closeAll()} must be manually invoked.
     * @param to the class of the instances that has to receive
     * @param data the data to use for the generation
     * @param <T> the type of the data to pass to the generator
     */
    @SuppressWarnings("unchecked")
    public static <T> void sendToAndWrite(Class<? extends IDataGenerator<T>> to, T data){
        if (generatorInstances.containsKey(to)) {
            var gen = generatorInstances.get(to);
            gen.accept(data);
            gen.writeOnFile();
        }
    }

    public static void clearAll(){
        generatorInstances.clear();
    }

    public static void closeAll(){
        generatorInstances.values().forEach(IDataGenerator::close);
    }

}
