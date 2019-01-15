package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.VSetPair;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ASTracker {

    Map<String, List<String>> idMap;
    Map<String, Map<Vertex, List<VSetPair>>> trackedCDVertexMap;
    Map<ArchitecturalSmell, List<ArchitecturalSmell>> versionMap;

    public ASTracker(){
        this.idMap = new HashMap<>();
        this.trackedCDVertexMap = new HashMap<>();
    }

    public void trackCD2(Graph graphV1, Graph graphV2, List<CDSmell> smellsInV1, List<ArchitecturalSmell> smellsInV2){
        GraphTraversalSource g1 = graphV1.traversal();
        GraphTraversalSource g2 = graphV2.traversal();

        Map<Long, ArchitecturalSmell> smellsInV2map = ArchitecturalSmell.toMap(smellsInV2);

        for (CDSmell smell : smellsInV1){
            // Get affected nodes names
            Set<String> nodesNames = (Set<String>) (Set<?>)g1.V(smell.getAffectedElements()).values("name").toSet();

            // Get nodes with such names in the next version
            // find all smells affecting such nodes in the next version and save their ids
            Set<Long> successorSmellsIds = g2.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(nodesNames))
                    .in().hasLabel(VertexLabel.SMELL.toString())
                    .has("smellType", ArchitecturalSmell.Type.CD.toString())
                    .toSet().stream().mapToLong(vertex -> Long.parseLong(vertex.id().toString()))
                    .boxed().collect(Collectors.toSet());

            assert smellsInV2map.keySet().containsAll(successorSmellsIds);
            // Save a map of their ids
            idMap.put(String.valueOf(smell.getId()), successorSmellsIds.stream().map(Object::toString).collect(Collectors.toList()));
            List<ArchitecturalSmell> successorSmells = versionMap.getOrDefault(smell, new ArrayList<>());
            successorSmellsIds.forEach(id -> successorSmells.add(smellsInV2map.get(id)));
            versionMap.putIfAbsent(smell, successorSmells);
        }

    }

    /**
     * Track CD smells from the first graph to the second one.
     * @param graphV1 The dependency graph of the system at v1 (current version)
     * @param graphV2 The dependency graph of the system at v2 (next version)
     * @return A map containing for each shape of smell (key) another map as a value. The second map stores, for each CD
     * smell in the system as key, a list of pairs of sets, where the first set of the pair contains the vertices of the smell
     * at the current version (v1) whereas the second pair contains the pairs of vertices of the identified smell.
     * In case a smell splits, multiple pairs will be contained in the list.
     */
    public Map<String, Map<Vertex, List<VSetPair>>> trackCD(Graph graphV1, Graph graphV2) {
        // Forward pass
        GraphTraversalSource g1 = graphV1.traversal();
        GraphTraversalSource g2 = graphV2.traversal();

        Map<String, List<Vertex>> smellsInTheSystem = (Map<String, List<Vertex>>) (Map<?, ?>) g1.V()
                .hasLabel(VertexLabel.CYCLESHAPE.toString())
                .group()
                .by("shapeType").next();

        trackedCDVertexMap = new HashMap<>();
        Map<String, EdgeLabel> shapesELabelsMap = new HashMap<>();
        shapesELabelsMap.put("star", EdgeLabel.PARTOFSTAR);
        shapesELabelsMap.put("tiny", EdgeLabel.ISTINYSHAPED);
        shapesELabelsMap.put("clique", EdgeLabel.ISCLIQUESHAPED);
        shapesELabelsMap.put("chain", EdgeLabel.ISPARTOFCHAIN);
        shapesELabelsMap.put("circle", EdgeLabel.ISCIRCLESHAPED);

        for (Map.Entry<String, List<Vertex>> entry : smellsInTheSystem.entrySet()) {
            String shape = entry.getKey();
            List<Vertex> vertices = entry.getValue();
            trackedCDVertexMap.put(shape, getMapping(g1, g2, vertices, shapesELabelsMap.get(shape)));
        }

        return trackedCDVertexMap;
        // Backward pass ?
    }

    /**
     * Computes what vertices of g1 are present in g2 using the "name" property at package or class level.
     *
     * @param g1            the first graph to extract the smells from
     * @param g2            the second graph to extract the smells from. It is assumed that this is the evolution of g1
     * @param smellVertices the smells of the system
     * @param cdType        the edge label that describe the smell
     * @return A map where the keys is the smell vertex in smellVertices and the value is a Pair of sets,
     * with the first element containing vertices from g1 and the second element the elements from g2
     * part of the same smell.
     */
    private Map<Vertex, List<VSetPair>> getMapping(GraphTraversalSource g1,
                                                          GraphTraversalSource g2,
                                                          List<Vertex> smellVertices,
                                                          EdgeLabel cdType) {

        Map<Vertex, List<VSetPair>> versionsMapping = new HashMap<>();
        for (Vertex smell : smellVertices) {
            List<VSetPair> values = new ArrayList<>();
            List<String> smellIDValues = new ArrayList<>();
            Set<String> vNames = (Set<String>) (Set<?>)
                    g1.V(smell)
                            .out(cdType.toString())
                            .out(EdgeLabel.PARTOFCYCLE.toString())
                            .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                            .values("name").toSet();
            Set<Vertex> v1 = g1.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(vNames))
                    .toSet();

            g2.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(vNames))
                    .in(EdgeLabel.PARTOFCYCLE.toString())
                    .in(cdType.toString())
                    .hasLabel(P.within(VertexLabel.CYCLESHAPE.toString()))
                    .forEachRemaining(smellVertex -> {
                        Set<Vertex> vn = g2.V(smellVertex)
                                .out(cdType.toString())
                                .out(EdgeLabel.PARTOFCYCLE.toString())
                                .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString())).toSet();
                        if (!vn.isEmpty()){
                            values.add(new VSetPair(v1, vn));
                            smellIDValues.add(smellVertex.value("smellId").toString());
                        }
                    });
            this.idMap.put(smell.value("smellId"), smellIDValues);
            versionsMapping.put(smell, values);
        }
        return versionsMapping;
    }

    public Map<String, List<String>> getIdMap() {
        return Collections.unmodifiableMap(idMap);
    }

    public Map<String, Map<Vertex, List<VSetPair>>> getTrackedCDVertexMap() {
        return trackedCDVertexMap;
    }

    public Map<ArchitecturalSmell, List<ArchitecturalSmell>> getVersionMap() {
        return versionMap;
    }
}
