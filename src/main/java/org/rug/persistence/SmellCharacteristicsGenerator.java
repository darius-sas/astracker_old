package org.rug.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.project.IProject;
import org.rug.tracker.ASmellTracker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.rug.tracker.ASmellTracker.*;

/**
 * Retrieves the smell characteristics for each smell and writes them on file.
 * The writing is done for each observation (record) and no in-memory list of records is saved.
 * This is done to ensure low memory usage.
 */
public class SmellCharacteristicsGenerator extends CSVDataGenerator<ASmellTracker>{

    private List<String> header = new ArrayList<>();
    private IProject project;

    public SmellCharacteristicsGenerator(String outputFile, IProject project) {
        super(outputFile);
        this.project = project;
    }

    /**
     * Returns the header of the underlying data.
     *
     * @return a array containing the headers.
     */
    @Override
    public String[] getHeader() {
        return header.toArray(new String[0]);
    }

    /**
     * Accepts an object to serialize into a list of records.
     * This method's implementation must populate the {@link #records} protected attribute.
     *
     * @param object the object to serialize into records of strings.
     */
    public void accept(ASmellTracker object) {
        Graph simplifiedGraph = object.getCondensedGraph();
        GraphTraversalSource g = simplifiedGraph.traversal();

        Set<String> smellKeys = new TreeSet<>(g.V().hasLabel("smell").propertyMap().tryNext().orElse(Collections.emptyMap()).keySet());
        header.addAll(smellKeys);
        Set<String> characteristicKeys = new TreeSet<>();
        g.V().hasLabel("characteristic").forEachRemaining(v -> characteristicKeys.addAll(v.keys()));
        header.add("version");
        header.add("versionPosition");
        header.add("smellIdInVersion");
        header.addAll(characteristicKeys);
        header.add("affectedElements");

        Set<Vertex> smells = g.V().hasLabel("smell").toSet();
        smells.forEach(smell -> {
            List<String> commonRecord = new ArrayList<>();
            smellKeys.forEach(k -> commonRecord.add(smell.value(k).toString()));

            var affects = g.V(smell).outE(AFFECTS).toSet();

            g.V(smell).outE(HAS_CHARACTERISTIC).as("e")
                    .inV().as("v")
                    .select("e", "v")
                    .forEachRemaining(variables -> {
                        Edge incomingEdge = (Edge)variables.get("e");
                        Vertex characteristic = (Vertex)variables.get("v");
                        List<String> completeRecord = new ArrayList<>(commonRecord);
                        String version = incomingEdge.value(VERSION).toString();
                        completeRecord.add(version);
                        completeRecord.add(project.getVersionIndex(version).toString());
                        completeRecord.add(incomingEdge.value(SMELL_ID).toString());
                        characteristicKeys.forEach(k -> completeRecord.add(characteristic.property(k).orElse("NA").toString()));
                        var affected = Arrays.toString(affects.stream()
                                .filter(e -> e.value(VERSION).equals(version))
                                .map(e -> e.inVertex().value(NAME).toString()).sorted().toArray());
                        completeRecord.add(affected);
                        writeRecordOnFile(completeRecord);
                    });
        });
    }

    /**
     * Writes an individual record directly on file.
     * @param record the record to write as a list of values (i.e. the columns).
     */
    private void writeRecordOnFile(List<String> record){
            try {
                if (fileWriter == null) {
                    fileWriter = new BufferedWriter(new FileWriter(getOutputFile(), CHARSET, false));
                    printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(getHeader()));
                }
                printer.printRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public synchronized void writeOnFile() {

    }

    @Override
    public void close() {
        try {
            printer.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
