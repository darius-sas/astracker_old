package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.Analysis;
import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tracks incrementally the architectural smells and saves them internally.
 */
public class ASTracker2 {

    private final static Logger logger = LoggerFactory.getLogger(ASTracker2.class);

    private static final String NAME = "name";
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
    private static final String SIMILARITY = "similarity";

    private Graph trackGraph;
    private Vertex tail;
    private long uniqueSmellID;
    private ISimilarityLinker scorer;
    private DecimalFormat decimal;

    private final boolean trackNonConsecutiveVersions;

    /**
     * Builds an instance of this tracker.
     * @param trackNonConsecutiveVersions whether to track a smell through non-consecutive versions.
     *                                    This adds the possibility to track reappearing smells.
     */
    public ASTracker2(ISimilarityLinker scorer, boolean trackNonConsecutiveVersions){
        this.trackGraph = TinkerGraph.open();
        this.tail = trackGraph.traversal().addV("tail").next();
        this.uniqueSmellID = 1L;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
        this.scorer = scorer;
        this.decimal = new DecimalFormat("0.0#");

    }

    /**
     * Builds an instance of this tracker that does not tracks smells through non-consecutive versions.
     * A JaccardSimilarityLinker is used to select the single successor of the given smell.
     */
    public ASTracker2(){
        this(new JaccardSimilarityLinker(), true);
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
            List<ArchitecturalSmell> currentVersionSmells;
            if (trackNonConsecutiveVersions)
                currentVersionSmells = g1.V(tail).out().values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell)o).collect(Collectors.toList());
            else
                currentVersionSmells = g1.V(tail).out().has(VERSION, tail.value(LATEST_VERSION).toString()).values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell)o).collect(Collectors.toList());

            LinkedHashSet<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch = scorer.bestMatch(currentVersionSmells, nextVersionSmells);

            Analysis.writeMatchScores(scorer.getUnfilteredMatch(), bestMatch, nextVersion);

            // Add smells that respect the threshold of the scorer as successors, or as newly arose smells if they
            // do not respect the threshold
            bestMatch.forEach(t -> {
                // If this fails it means that a successor has already been found.
                Vertex predecessor = g1.V(tail).out().has(SMELL_OBJECT, t.getA()).next();
                Vertex successor = g1.addV(SMELL)
                        .property(VERSION, nextVersion)
                        .property(SMELL_OBJECT, t.getB()).next();

                g1.V(tail).outE().where(__.otherV().is(predecessor)).drop().iterate();
                String eLabel = tail.value(LATEST_VERSION).equals(predecessor.value(VERSION)) ? EVOLVED_FROM : REAPPEARED;
                g1.addE(eLabel).property(SIMILARITY, decimal.format(t.getC())).from(successor).to(predecessor).next();
                g1.addE(LATEST_VERSION).from(tail).to(successor).next();
                currentVersionSmells.remove(t.getA());
                nextVersionSmells.remove(t.getB());
            });
            if (!trackNonConsecutiveVersions)
                currentVersionSmells.forEach(this::endDynasty);
        }
        nextVersionSmells.forEach(s -> addNewDynasty(s, nextVersion));
        tail.property(LATEST_VERSION, nextVersion);
    }

    /**
     * Begins a new dynasty for the given AS at the given starting version
     * @param s the starter of the dynasty
     * @param startingVersion the version
     */
    private void addNewDynasty(ArchitecturalSmell s, String startingVersion) {
        GraphTraversalSource g = trackGraph.traversal();
        Vertex successor = g.addV(SMELL)
                .property(VERSION, startingVersion)
                .property(SMELL_OBJECT, s).next();
        Vertex head = g.addV(HEAD)
                .property(VERSION, startingVersion)
                .property(UNIQUE_SMELL_ID, uniqueSmellID++).next();
        g.addE(STARTED_IN).from(head).to(successor).next();
        g.addE(LATEST_VERSION).from(tail).to(successor).next();
    }

    /**
     * Concludes the dynasty of the given smell (last smell in the dynasty)
     * @param smell the smell
     */
    private void endDynasty(ArchitecturalSmell smell){
        GraphTraversalSource g = trackGraph.traversal();
        Vertex lastHeir = g.V().has(SMELL_OBJECT, smell).next();
        Vertex end = g.addV(END).next();
        g.V(tail).outE().where(__.otherV().is(lastHeir)).drop().iterate();
        g.addE(REMOVED).from(end).to(lastHeir).next();
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
            GraphTraversalSource g = trackGraph.traversal();
            g.V(tail).out().forEachRemaining( v -> g.addE(END).from(g.addV(END).next()).to(v).next());
            tail.remove();
            trackGraph.io(IoCore.graphml()).writeGraph(file);
        } catch (IOException e) {
            logger.error("Could not write track graph on file: {}", e.getMessage());
        }
    }
}
