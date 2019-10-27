package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
/**
 * Base modeling for CSV data generators.
 * @param <T>
 */
public abstract class CSVDataGenerator<T> implements ICSVGenerator<T>{

    private static final Logger logger = LoggerFactory.getLogger(CSVDataGenerator.class);

    protected final List<List<String>> records;
    private final Path outputFile;
    protected Writer fileWriter;
    protected CSVPrinter printer;
    protected final static Charset CHARSET = Charset.forName(StandardCharsets.UTF_8.toString());
    private CompletableFuture<Void> future;

    public CSVDataGenerator(String outputFile) {
        this.records = new ArrayList<>();
        this.outputFile = Paths.get(outputFile);
    }

    /**
     * Returns an iterator over the records of this instances.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<List<String>> iterator() {
        return records.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(Consumer<? super List<String>> action) {
        records.forEach(action);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="../util/Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public Spliterator<List<String>> spliterator() {
        return records.spliterator();
    }

    /**
     * Returns the file where to write the records of this generator.
     * @return a file.
     */
    public File getOutputFile(){
        return this.outputFile.toFile();
    }

    /**
     * Writes the given current list of records on file asynchronously.
     * This method is synchronized in order to prevent concurrent writes on file.
     * It is necessary to manually call {@link #close()} to ensure all the data has been written on the stream.
     */
    @Override
    public synchronized void writeOnFile() {
        future = CompletableFuture.runAsync(()-> {
            try {
                if (fileWriter == null) {
                    fileWriter = new BufferedWriter(new FileWriter(getOutputFile(), CHARSET, false));
                    printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(getHeader()));
                }
                printer.printRecords(records);
                records.clear();
            } catch (IOException e) {
                logger.error("Could not write records on CSV data file: {}", outputFile.toAbsolutePath());
            }
        });
    }

    /**
     * Waits for the remaining data to be written on file and then closes the underlying writer objects.
     */
    public void close(){
        if (future != null) {
            try {
                future.thenRun(() -> {
                    try {
                        printer.flush();
                        fileWriter.flush();
                        printer.close();
                        fileWriter.close();
                    } catch (IOException e) {
                        logger.error("Could not close properly data writing on file: {}", outputFile.toAbsolutePath());
                    }
                    fileWriter = null;
                }).get();
            } catch (ExecutionException e) {
                logger.error("Error while writing concurrently data on file: {}", outputFile.toAbsolutePath());
            } catch (InterruptedException e) {
                logger.info("Concurrent file save was interrupted for data on file: {}", outputFile.toAbsolutePath());
            }
        }
    }
}
