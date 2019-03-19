package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.Project;
import org.rug.data.smells.ArchitecturalSmell;

import static org.rug.tracker.ASmellTracker.*;

import java.util.Set;

public class CondensedTrackGraph {

    private ASmellTracker tracker;
    private Project project;
    private Graph condensedGraph;
    private Graph finalizedTrackGraph;

    public CondensedTrackGraph(ASmellTracker tracker, Project project){
        this.project = project;
        this.tracker = tracker;
        this.condensedGraph = null;
        this.finalizedTrackGraph = this.tracker.getFinalizedTrackGraph();
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
            GraphTraversalSource g1 = finalizedTrackGraph.traversal();
            GraphTraversalSource gs = condensedGraph.traversal();

            Set<Path> dynasties = g1.V().hasLabel(HEAD).out(STARTED_IN)
                    .repeat(__.in(EVOLVED_FROM, REAPPEARED, END))
                    .until(__.hasLabel(END))
                    .path().toSet();
            dynasties.parallelStream().forEach(p -> {
                //for (Path p : dynasties) {
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

                            as.getAffectedElements().stream().map(e -> e.value(NAME)).forEach(name -> {
                                if (!gs.V().has(NAME, name).hasNext()) {
                                    gs.addV(COMPONENT)
                                            .property(NAME, name)
                                            .property(COMPONENT_TYPE, as.getLevel().toString()).next();
                                }
                                gs.addE(AFFECTS)
                                        .from(smellVertex).to(gs.V().has(NAME, name).next())
                                        .property(VERSION, v.value(VERSION))
                                        .property(VERSION_POSITION, project.getVersionIndex(v.value(VERSION)))
                                        .next();
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

}
