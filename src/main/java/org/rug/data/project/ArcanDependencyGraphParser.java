package org.rug.data.project;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
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

    public static int MAX_CACHED_GRAPH_COUNT = 1;
    public static AbstractProject.Type PROJECT_TYPE = AbstractProject.Type.JAVA;

    private final static Logger logger = LoggerFactory.getLogger(ArcanDependencyGraphParser.class);

    private final static Map<Graph, List<ArchitecturalSmell>> cachedSmellLists = new HashMap<>();

    public static List<ArchitecturalSmell> getArchitecturalSmellsIn(Graph graph, Project.Type projectType){
        PROJECT_TYPE = projectType;
        return getArchitecturalSmellsIn(graph);
    }

    /**
     * Given the graph of a system, this methods builds a list of Architectural Smells that affect this system.
     * The list is cached internally for future retrievals. A maximum of {@link #MAX_CACHED_GRAPH_COUNT} graphs are
     * cached to save memory.
     * @param graph the graph of the system.
     * @return an unmodifiable list containing the parsed smells.
     */
    public static List<ArchitecturalSmell> getArchitecturalSmellsIn(Graph graph){
        if (!cachedSmellLists.containsKey(graph)){
            List<ArchitecturalSmell> architecturalSmells = new ArrayList<>();
            graph.traversal().V().hasLabel(VertexLabel.SMELL.toString()).toList()
                    .forEach(smellVertex -> {
                        String smellTypeProperty = smellVertex.value("smellType");
                        if (smellTypeProperty != null) {
                            ArchitecturalSmell.Type smellType = ArchitecturalSmell.Type.fromString(smellTypeProperty);
                            if (!smellVertex.property(CDSmell.VISITED_SMELL_NODE).orElse("false").equals("true")) {
                                ArchitecturalSmell as = smellType.getInstance(smellVertex, PROJECT_TYPE);
                                if (as != null)
                                    architecturalSmells.add(as);
                                else
                                    logger.warn("AS type '{}' with id '{}' was ignored since no implementation exists for it.", smellVertex.value("smellType").toString(), smellVertex.id());
                            }
                        } else {
                            logger.warn("No 'smellType' property found for smell vertex {}.", smellVertex);
                        }
                    });
            if (cachedSmellLists.size() >= MAX_CACHED_GRAPH_COUNT)
                cachedSmellLists.clear();
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
