package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Parses Arcan's result from different formats into graphs representing the system analyzed
 * and the AS within it found by Arcan.
 */
public class ArcanDependencyGraphParser {

    private final static Logger logger = LoggerFactory.getLogger(ArcanDependencyGraphParser.class);

    private final static Map<Graph, List<ArchitecturalSmell>> cachedSmellLists = new HashMap<>();

    /**
     * Parses all the .graphml files in the given path and builds a sorted map with versions as keys and graphs as values.
     * If path is a file a map with a single entry is returned.
     * @param path the path to a directory of graphml files with names in the format '[projectname]-[version].graphml'.
     * @return A sorted map as described above.
     */
    public static SortedMap<String, Graph> parseGraphML(String path){
        SortedMap<String, Graph> versionedSystem = new TreeMap<>();

        Consumer<Path> addGraph = f -> {
            String version = f.getFileName().toString().substring(
                    f.getFileName().toString().lastIndexOf('-') + 1,
                    f.getFileName().toString().lastIndexOf('.'));
            Graph graph = TinkerGraph.open();
            try {
                graph.io(IoCore.graphml()).readGraph(f.toAbsolutePath().toString());
                versionedSystem.put(version, graph);
            } catch (IOException e) {
                logger.error("Unable to read graph from file: ", e.getMessage());
            }
        };

        Path f = Paths.get(path);

        if (Files.isDirectory(f)) {
            try {
                Files.walk(Paths.get(path))
                        .filter(Files::isRegularFile)
                        .filter(ff -> ff.getFileName().toString().matches(".*\\.graphml"))
                        .forEach(addGraph);
            } catch (IOException e) {
                logger.error("Unable to walk the given path: ", path);
            }
        }else {
            addGraph.accept(f);
        }

        return versionedSystem;
    }

    /**
     * Given the graph of a system, this methods builds a list of Architectural Smells that affect this system.
     * The list is cached internally for future retrievals.
     * @param graph the graph of the system.
     * @return an unmodifiable list containing the parsed smells.
     */
    @SuppressWarnings("unchecked")
    public static List<ArchitecturalSmell> getArchitecturalSmellsIn(Graph graph){
        if (!cachedSmellLists.containsKey(graph)){
            List<ArchitecturalSmell> architecturalSmells = new ArrayList<>();
            graph.traversal().V().hasLabel(VertexLabel.SMELL.toString()).toList()
                    .forEach(smellVertex -> {
                        String smellTypeProperty = smellVertex.value("smellType");
                        if (smellTypeProperty != null) {
                            ArchitecturalSmell.Type smellType = ArchitecturalSmell.Type.fromString(smellTypeProperty);
                            if (!smellVertex.property(CDSmell.VISITED_SMELL_NODE).orElse("false").equals("true")) {
                                ArchitecturalSmell as = smellType.getInstance(smellVertex);
                                if (as != null)
                                    architecturalSmells.add(as);
                                else
                                    logger.warn("AS type '{}' with id '{}' was ignored since no implementation exists for it.", smellVertex.value("smellType").toString(), smellVertex.id());
                            }
                        } else {
                            logger.warn("No 'smellType' property found for smell vertex {}.", smellVertex);
                        }
                    });
            cachedSmellLists.putIfAbsent(graph, Collections.unmodifiableList(architecturalSmells));
        }
        return cachedSmellLists.get(graph);
    }

    /**
     * Maps every architectural smell in the given list to its id.
     * @param list the list of AS to use
     * @return a map where the keys are the ids of the smell and the values are the smell instances.
     */
    public static Map<Long, ArchitecturalSmell> toMap(List<ArchitecturalSmell> list){
        return list.stream().collect(Collectors.toMap(ArchitecturalSmell::getId, smell -> smell));
    }
}
