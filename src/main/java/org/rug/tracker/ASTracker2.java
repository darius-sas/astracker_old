package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks incrementally the architectural smells and saves them internally.
 */
public class ASTracker2 {

    private final static Logger logger = LoggerFactory.getLogger(ASTracker2.class);

    private static final String NAME = "name";
    private static final String SMELL_TYPE = "smellType";
    private static final String SMELL = "smell";
    private static final String VERSION = "version";
    private static final String SMELL_OBJECT = "smellObject";
    private static final String LATEST_VERSION = "latestVersion";
    private static final String EVOLVED_FROM = "evolvedFrom";
    private static final String HEAD = "head";
    private static final String UNIQUE_SMELL_ID = "uniqueSmellID";
    private static final String REAPPEARED = "reappeared";
    private static final String STARTED_IN = "startedIn";
    private static final String REMOVED = "removed";
    private static final String END = "end";

    private Graph trackGraph;
    private Vertex tail;
    private long uniqueSmellID;

    private final boolean trackNonConsecutiveVersions;

    /**
     * Builds an instance of this tracker.
     * @param trackNonConsecutiveVersions whether to track a smell through non-consecutive versions.
     *                                    This adds the possibility to track reappearing smells.
     */
    public ASTracker2(boolean trackNonConsecutiveVersions){
        this.trackGraph = TinkerGraph.open();
        this.tail = trackGraph.traversal().addV("tail").next();
        this.uniqueSmellID = 1L;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
    }

    /**
     * Builds an instance of this tracker that does not tracks smells through non-consecutive versions.
     */
    public ASTracker2(){
        this(false);
    }

    /**
     * Computes the tracking algorithm on the given system and saves internally the results
     * @param systemGraph the graph representing the system as computed by Arcan
     * @param nextVersion the version of the given system
     */
    public void track(Graph systemGraph, String nextVersion) {
        Map<Long, ArchitecturalSmell> nextVersionSmells = ArchitecturalSmell.toMap(ArchitecturalSmell.getArchitecturalSmellsIn(systemGraph));

        GraphTraversalSource g1 = trackGraph.traversal();

        if (g1.V(tail).outE().hasNext()) {
            GraphTraversalSource g2 = systemGraph.traversal();
            // Create a map between a smell vertex in the current track graph and the contained smell
            Map<Vertex, ArchitecturalSmell> currentVersionSmells = g1.V()
                    .where(__.in().is(tail)).toStream()
                    .collect(HashMap::new, (map, vertex) -> map.putIfAbsent(vertex, vertex.value(SMELL_OBJECT)), HashMap::putAll);

            if (!trackNonConsecutiveVersions)
                g1.V(tail).outE().drop().iterate();

            currentVersionSmells.forEach((smellVertex, smell) -> {
                // Get affected nodes names
                Set<String> nodesNames = smell.getAffectedElements().stream().map(vertex -> vertex.value(NAME).toString()).collect(Collectors.toSet());

                // Get nodes with such names in the next version
                // find all smells of the same type affecting such nodes in the next version and save their ids
                Set<Long> successorSmellsIds = g2.V()
                        .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                        .has(NAME, P.within(nodesNames))
                        .in().hasLabel(VertexLabel.SMELL.toString())
                        .has(SMELL_TYPE, smell.getType().toString())
                        .not(__.has(CDSmell.VISITED_SMELL_NODE, "true"))
                        .toSet().stream().mapToLong(vertex -> Long.parseLong(vertex.id().toString()))
                        .boxed().collect(Collectors.toSet());

                // Add the corresponding smells as successors and set adequately the tail to the latest version
                successorSmellsIds.forEach(id -> {
                    ArchitecturalSmell nextVersionSmell = nextVersionSmells.remove(id);
                    Vertex successor = g1.addV(SMELL)
                            .property(VERSION, nextVersion)
                            .property(SMELL_OBJECT, nextVersionSmell).next();
                    // Reset tail for this dynasty (allows tracking through non-consecutive versions)
                    g1.V(tail).outE().where(__.otherV().is(smellVertex)).drop().iterate();
                    if (tail.value(LATEST_VERSION).equals(smellVertex.value(VERSION)))
                        g1.addE(EVOLVED_FROM).from(successor).to(smellVertex).next();
                    else
                        g1.addE(REAPPEARED).from(successor).to(smellVertex).next();
                    g1.addE(LATEST_VERSION).from(tail).to(successor).next();
                });
            });
        }

        // all smells remained are new and can be added to trackGraph as newly arose smells assigning them a unique id
        nextVersionSmells.forEach((id, smell) -> {
                Vertex head = g1.addV(HEAD)
                        .property(VERSION, nextVersion)
                        .property(UNIQUE_SMELL_ID, uniqueSmellID++).next();
                Vertex v = g1.addV(SMELL)
                        .property(VERSION, nextVersion)
                        .property(SMELL_OBJECT, smell).next();
                g1.addE(STARTED_IN).from(head).to(v).next();
                g1.addE(LATEST_VERSION).from(tail).to(v).next();
        });

        // Add end vertex and edges to all vertices that do not have a successor/incoming edge.
        if (!trackNonConsecutiveVersions){
            g1.V().hasLabel(SMELL)
                    .where(__.not(__.in()))
                    .forEachRemaining(vertex -> {
                        Vertex end = g1.addV(END).next();
                        g1.addE(REMOVED).from(end).to(vertex).next();
                    });
        }

        tail.property(LATEST_VERSION, nextVersion);
    }

    /**
     * Builds the simplified of the tracking graph and returns the results.
     * The simplified graph basically collapses all SMELL vertices by walking the EVOLVED_FROM edges.
     * @return the graph representing the tracked smells including their characteristics (does not trigger the calculation).
     */
    public Graph getSimplifiedTrackGraph(){
        Graph simplifiedGraph = TinkerGraph.open();
        GraphTraversalSource g1 = trackGraph.traversal();
        GraphTraversalSource gs = simplifiedGraph.traversal();

        // Collapse on evolved edges
        g1.V().hasLabel(HEAD).out(STARTED_IN).match(null);
        // Create characteristic vertex
        return null;
    }

    public void writeSimplifiedGraph(String file){
        try {
            getSimplifiedTrackGraph().io(IoCore.graphml()).writeGraph(file);
        } catch (IOException e) {
            logger.error("Could not write simplified graph on file: {}", e.getMessage());
        }
    }

    public void writeTrackGraph(String file){
        try {
            trackGraph.io(IoCore.graphml()).writeGraph(file);
        } catch (IOException e) {
            logger.error("Could not write track graph on file: {}", e.getMessage());
        }
    }
}
