package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks incrementally the architectural smells and saves them internally.
 */
public class ASTracker2 {

    private Graph trackGraph;
    private Vertex tail;
    private long uniqueSmellID;

    private final boolean trackNonConsecutiveVersions;

    /**
     * Builds an instance of this tracker.
     * @param trackNonConsecutiveVersions whether to track a smell through non-consecutive versions.
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
                    .in().is(tail).toStream()
                    .collect(HashMap::new, (map, vertex) -> map.putIfAbsent(vertex, vertex.value("smellObject")), HashMap::putAll);

            if (!trackNonConsecutiveVersions)
                g1.V(tail).outE().drop().iterate();

            currentVersionSmells.forEach((smellVertex, smell) -> {
                // Get affected nodes names
                Set<String> nodesNames = smell.getAffectedElements().stream().map(vertex -> vertex.value("name").toString()).collect(Collectors.toSet());

                // Get nodes with such names in the next version
                // find all smells affecting such nodes in the next version and save their ids
                Set<Long> successorSmellsIds = g2.V()
                        .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                        .has("name", P.within(nodesNames))
                        .in().hasLabel(VertexLabel.SMELL.toString())
                        .has("smellType", ArchitecturalSmell.Type.CD.toString())
                        .not(__.has("visitedStar", "true"))
                        .toSet().stream().mapToLong(vertex -> Long.parseLong(vertex.id().toString()))
                        .boxed().collect(Collectors.toSet());

                // Add the corresponding smells as successors and set adequately the tail to the latest version
                successorSmellsIds.forEach(id -> {
                    ArchitecturalSmell nextVersionSmell = nextVersionSmells.remove(id);
                    Vertex successor = g1.addV("smell")
                            .property("version", nextVersion)
                            .property("smellObject", nextVersionSmell).next();
                    // Reset tail for this dynasty (allows tracking through non-consecutive versions)
                    g1.E().from(tail).to(smellVertex).drop().iterate();
                    g1.addE("evolvedFrom").from(successor).to(smellVertex).next();
                    g1.addE("latestVersion").from(tail).to(successor).next();
                });
            });
        }
        // all smells remained are new and can be added to trackGraph as newly arose smells
        Vertex head = g1.addV("head").property("version", nextVersion).property("uniqueSmellID", uniqueSmellID++).next();
        nextVersionSmells.forEach((id, smell) -> {
                Vertex v = g1.addV("smell")
                        .property("version", nextVersion)
                        .property("smellObject", smell).next();
                g1.addE("startedIn").from(head).to(v).next();
                g1.addE("latestVersion").from(tail).to(v).next();
        });

        if (!trackNonConsecutiveVersions){
            g1.addE("removed")
                    .from(g1.addV("end").next())
                    .to(g1.V().hasLabel("smell")
                            .where(__.not(__.inV())))
                    .iterate();
        }

    }

    /**
     * Builds the simplified of the tracking graph and returns the results
     * @return the graph representing the tracked smells
     */
    public Graph getSimplifiedTrackGraph(){
        return null;
    }
}
