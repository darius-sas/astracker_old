package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
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
    private ISuccessorMatcher matcher;

    private final boolean trackNonConsecutiveVersions;

    /**
     * Builds an instance of this tracker.
     * @param trackNonConsecutiveVersions whether to track a smell through non-consecutive versions.
     *                                    This adds the possibility to track reappearing smells.
     */
    public ASTracker2(ISuccessorMatcher matcher, boolean trackNonConsecutiveVersions){
        this.trackGraph = TinkerGraph.open();
        this.tail = trackGraph.traversal().addV("tail").next();
        this.uniqueSmellID = 1L;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
        this.matcher = matcher;
    }

    /**
     * Builds an instance of this tracker that does not tracks smells through non-consecutive versions.
     * A JaccardMatcher is used to select the single successor of the given smell.
     */
    public ASTracker2(){
        this(new JaccardMatcher(), false);
    }

    /**
     * Computes the tracking algorithm on the given system and saves internally the results
     * @param systemGraph the graph representing the system as computed by Arcan
     * @param nextVersion the version of the given system
     */
    public void track(Graph systemGraph, String nextVersion){
        List<ArchitecturalSmell> nextVersionSmells = ArchitecturalSmell.getArchitecturalSmellsIn(systemGraph);

        GraphTraversalSource g1 = trackGraph.traversal();

        if (g1.V(tail).outE().hasNext()) {
            // Create a map between a smell vertex in the current track graph and the contained smell
            List<ArchitecturalSmell> currentVersionSmells = g1.V(tail)
                    .out().toStream()
                    .map(vertex -> (ArchitecturalSmell) vertex.value(SMELL_OBJECT))
                    .collect(Collectors.toList());

            if (!trackNonConsecutiveVersions)
                g1.V(tail).outE().drop().iterate();

            List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch = matcher.bestMatch(currentVersionSmells, nextVersionSmells);

            // Add smells that respect the threshold of the matcher as successors, or as newly arose smells if they
            // do not respect the threshold
            bestMatch.forEach(t -> {
                Vertex successor = g1.addV(SMELL)
                        .property(VERSION, nextVersion)
                        .property(SMELL_OBJECT, t.getB()).next();
                if (t.getC() >= matcher.getThreshold()) {
                    Vertex predecessor = g1.V(tail).out().is(t.getA()).next();
                    // Reset tail for this dynasty (allows tracking through non-consecutive versions)
                    g1.V(tail).outE().where(__.otherV().is(predecessor)).drop().iterate();
                    if (tail.value(LATEST_VERSION).equals(predecessor.value(VERSION)))
                        g1.addE(EVOLVED_FROM).from(successor).to(predecessor).next();
                    else
                        g1.addE(REAPPEARED).from(successor).to(predecessor).next();
                    g1.addE(LATEST_VERSION).from(tail).to(successor).next();
                } else {
                    Vertex head = g1.addV(HEAD)
                            .property(VERSION, nextVersion)
                            .property(UNIQUE_SMELL_ID, uniqueSmellID++).next();
                    g1.addE(STARTED_IN).from(head).to(successor).next();
                    g1.addE(LATEST_VERSION).from(tail).to(successor).next();
                }
            });
        }else {
            // TODO remove this duplicated code
            nextVersionSmells.forEach(smell -> {
                Vertex successor = g1.addV(SMELL)
                        .property(VERSION, nextVersion)
                        .property(SMELL_OBJECT, smell).next();
                Vertex head = g1.addV(HEAD)
                        .property(VERSION, nextVersion)
                        .property(UNIQUE_SMELL_ID, uniqueSmellID++).next();
                g1.addE(STARTED_IN).from(head).to(successor).next();
                g1.addE(LATEST_VERSION).from(tail).to(successor).next();
            });
        }

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
