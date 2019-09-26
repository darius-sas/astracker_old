package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.project.IVersion;
import org.rug.data.smells.ArchitecturalSmell;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Tracks incrementally the architectural smells and saves them internally.
 */
public class ASmellTracker implements Serializable {

    public static final String NAME = "name";
    public static final String SMELL = "smell";
    public static final String VERSION = "version";
    public static final String VERSION_POSITION = "versionPosition";
    public static final String SMELL_OBJECT = "smellObject";
    public static final String LATEST_VERSION = "latestVersion";
    public static final String EVOLVED_FROM = "evolvedFrom";
    public static final String HEAD = "head";
    public static final String UNIQUE_SMELL_ID = "uniqueSmellID";
    public static final String REAPPEARED = "reappeared";
    public static final String STARTED_IN = "startedIn";
    public static final String END = "end";
    public static final String SIMILARITY = "similarity";
    public static final String CHARACTERISTIC = "characteristic";
    public static final String HAS_CHARACTERISTIC = "hasCharacteristic";
    public static final String COMPONENT = "component";
    public static final String AFFECTS = "affects";
    public static final String SMELL_TYPE = "smellType";
    public static final String AGE = "age";
    public static final String NA = "NA";
    public static final String FIRST_APPEARED = "firstAppeared";
    public static final String SMELL_ID = "smellId";
    public static final String COMPONENT_TYPE = "componentType";

    private Graph trackGraph;
    private Graph condensedGraph;
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
    public ASmellTracker(ISimilarityLinker scorer, boolean trackNonConsecutiveVersions){
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
    public ASmellTracker(){
        this(new JaccardSimilarityLinker(), false);
    }

    /**
     * Computes the tracking algorithm on the given system and saves internally the results
     * @param smellsInVersion the architectural smells identified in version
     * @param version the version of the given system
     */
    public void track(List<ArchitecturalSmell> smellsInVersion, IVersion version){
        List<ArchitecturalSmell> nextVersionSmells = new ArrayList<>(smellsInVersion);

        GraphTraversalSource g1 = trackGraph.traversal();

        if (g1.V(tail).outE().hasNext()) {
            List<ArchitecturalSmell> currentVersionSmells;
            if (trackNonConsecutiveVersions)
                currentVersionSmells = g1.V(tail).out().values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell)o).collect(Collectors.toList());
            else
                currentVersionSmells = g1.V(tail).out().has(VERSION, tail.value(LATEST_VERSION).toString()).values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell)o).collect(Collectors.toList());

            Set<LinkScoreTriple> bestMatch = scorer.bestMatch(currentVersionSmells, nextVersionSmells);

            bestMatch.forEach(t -> {
                // If this fails it means that a successor has already been found, which should never happen!
                Vertex predecessor = g1.V(tail).out().has(SMELL_OBJECT, t.getA()).next();
                Vertex successor = g1.addV(SMELL)
                        .property(VERSION, version.getVersionString())
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
        nextVersionSmells.forEach(s -> addNewDynasty(s, version.getVersionString()));
        tail.property(LATEST_VERSION, version.getVersionString());
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
        Vertex end = g.addV(END).property(VERSION, currentVersion()).next();
        g.V(tail).outE().where(__.otherV().is(lastHeir)).drop().iterate();
        g.addE(END).from(end).to(lastHeir).next();
    }

    /**
     * Get the scorer used to instantiate this instance.
     * @return The scorer used to link the smells between versions.
     */
    public ISimilarityLinker getScorer() {
        return scorer;
    }

    /**
     * Returns the latest version of update of this tracker.
     * @return a string representing the version or {@link #NA} if no current version is available.
     */
    public String currentVersion(){
        return tail.property(LATEST_VERSION).orElse(NA).toString();
    }

    /**
     * Builds the condensed graph of the tracking graph and returns the result.
     * The simplified graph basically collapses all SMELL vertices by walking the EVOLVED_FROM edges.
     * This operation drops the tail and all of its outgoing edges from the graph. The tracking graph
     * is no more usable after this operation has been executed.
     * @return the graph representing the tracked smells including their characteristics.
     */
    public Graph getCondensedGraph(){
        if (condensedGraph == null) {
            condensedGraph = TinkerGraph.open();
            GraphTraversalSource g1 = trackGraph.traversal();
            GraphTraversalSource gs = condensedGraph.traversal();

            g1.V(tail).out().forEachRemaining(v -> g1.addE(END).from(g1.addV(END).property(VERSION, tail.value(LATEST_VERSION)).next()).to(v).next());
            g1.V(tail).outE().drop().iterate();
            tail.remove();

            Set<Path> dynasties = g1.V().hasLabel(HEAD).out(STARTED_IN)
                    .repeat(__.in(EVOLVED_FROM, REAPPEARED, END))
                    .until(__.hasLabel(END))
                    .path().toSet();
            dynasties.parallelStream().forEach(p -> {
                Vertex smellVertex = gs.addV(SMELL).next();
                int age = 0;
                for (Object o : p) { // beware: the path unfolds the visited vertices backwards
                    if (o instanceof Vertex) {
                        Vertex v = (Vertex) o;
                        if (((Vertex) o).label().equals(HEAD)) {
                            smellVertex.property(UNIQUE_SMELL_ID, v.value(UNIQUE_SMELL_ID))
                                    .property(FIRST_APPEARED, v.values(VERSION));
                        } else if (((Vertex) o).label().equals(SMELL)) {
                            ArchitecturalSmell as = v.value(SMELL_OBJECT);
                            if (!smellVertex.property(SMELL_TYPE).isPresent()) {
                                smellVertex.property(SMELL_TYPE, as.getType().toString());
                            }
                            Vertex characteristics = gs.addV(CHARACTERISTIC).next();
                            as.getCharacteristicsMap().forEach(characteristics::property);
                            gs.addE(HAS_CHARACTERISTIC).from(smellVertex).to(characteristics)
                                    .property(VERSION, v.value(VERSION))
                                    .property(SMELL_ID, as.getId()).next();

                            as.getAffectedElements().forEach(affected -> {
                                var name = affected.value(NAME);
                                var component = gs.V().has(NAME, name).tryNext().orElse(null);
                                if (component == null) {
                                    component = gs.addV(COMPONENT)
                                            .property(NAME, name)
                                            .property(COMPONENT_TYPE, as.getLevel().toString()).next();
                                }
                                gs.addE(AFFECTS)
                                        .from(smellVertex).to(component)
                                        .property(VERSION, v.value(VERSION))
                                        .next();
                                var cce = gs.V(component).outE(HAS_CHARACTERISTIC)
                                        .has(VERSION, v.value(VERSION).toString())
                                        .tryNext();
                                if (cce.isEmpty()){
                                    final var componentCharacteristics = gs.addV("componentCharacteristic").next();
                                    affected.keys().stream().filter(k -> !k.equals("name")).forEach(k->
                                        componentCharacteristics.property(k, affected.value(k))
                                    );
                                    gs.addE(HAS_CHARACTERISTIC).from(component).to(componentCharacteristics)
                                            .property(VERSION, v.value(VERSION)).next();
                                }
                            });

                            age++;
                        } else if ((((Vertex) o).label().equals(END))) {
                            smellVertex.property(AGE, age).property("lastDetected", ((Vertex) o).value(VERSION));
                        }
                    }
                }   
            });
        }
        return condensedGraph;
    }

    public Graph getTrackGraph(){
        return trackGraph;
    }

    /**
     * Returns the number of smells linked in the current iteration.
     * @return the number of smells linked.
     */
    public long smellsLinked(){
        return this.getScorer().bestMatch().size();
    }

    /**
     * Closes the current trackgraph and returns the graph object. This operation removes the tail from the graph.
     * The result is that this trackgraph is no more usable.
     */
    public Graph getFinalizedTrackGraph(){
        GraphTraversalSource g = trackGraph.traversal();
        g.V(tail).out().forEachRemaining( v -> g.addE(END).from(g.addV(END).property(VERSION, tail.value(LATEST_VERSION)).next()).to(v).next());
        tail.remove();

         g.V().has(SMELL_OBJECT).forEachRemaining(vertex -> {
             ArchitecturalSmell as = vertex.value(SMELL_OBJECT);
             vertex.property(SMELL_TYPE, as.getType().toString());
             as.getCharacteristicsMap().forEach(vertex::property);
             Set<String> affectedElements = as.getAffectedElements().stream()
                     .map(v -> v.value(NAME).toString())
                     .collect(Collectors.toCollection(TreeSet::new));
             vertex.property("affectedElements", affectedElements.toString());
         });
        return trackGraph;
    }

}
