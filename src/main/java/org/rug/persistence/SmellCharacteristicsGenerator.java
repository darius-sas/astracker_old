package org.rug.persistence;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.tracker.ASmellTracker;

import java.util.*;

public class SmellCharacteristicsGenerator extends DataGenerator<ASmellTracker> {

    private List<String> header = new ArrayList<>();

    public SmellCharacteristicsGenerator(String outputFile) {
        super(outputFile);
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
    @Override
    public void accept(ASmellTracker object) {
        Graph simplifiedGraph = object.getCondensedGraph();
        GraphTraversalSource g = simplifiedGraph.traversal();

        Set<String> smellKeys = new TreeSet<>(g.V().hasLabel("smell").propertyMap().next().keySet());
        header.addAll(smellKeys);
        Set<String> characteristicKeys = new TreeSet<>();
        g.V().hasLabel("characteristic").forEachRemaining(v -> characteristicKeys.addAll(v.keys()));
        header.add("version");
        header.add("smellIdInVersion");
        header.addAll(characteristicKeys);

        Set<Vertex> smells = g.V().hasLabel("smell").toSet();
        smells.forEach(smell -> {
            List<String> commonRecord = new ArrayList<>();
            smellKeys.forEach(k -> commonRecord.add(smell.value(k).toString()));

            g.V(smell).outE("hasCharacteristic").as("e")
                    .inV().as("v")
                    .select("e", "v")
                    .forEachRemaining(variables -> {
                        Edge incomingEdge = (Edge)variables.get("e");
                        Vertex characteristic = (Vertex)variables.get("v");
                        List<String> completeRecord = new ArrayList<>(commonRecord);
                        completeRecord.add(incomingEdge.value("version").toString());
                        completeRecord.add(incomingEdge.value("smellId").toString());
                        characteristicKeys.forEach(k -> completeRecord.add(characteristic.property(k).orElse("NA").toString()));
                        //TODO add affected elements
                        records.add(completeRecord);
                    });
        });
    }
}
