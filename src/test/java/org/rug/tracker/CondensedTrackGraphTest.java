package org.rug.tracker;

import org.junit.jupiter.api.Test;
import org.rug.data.Project;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CondensedTrackGraphTest {

    @Test
    void getCondensedGraph() throws IOException {
        var project = new Project("antlr");
        project.addGraphMLs("./qualitas-corpus/output/arcanOutput/" + project.getName());
        var tracker = new ASmellTracker(new JaccardSimilarityLinker(), false);

        project.getVersionedSystem().forEach((v, t) -> {
            tracker.track(project.getArchitecturalSmellsIn(v), v);
        });

        var condensedTrackGraph = new CondensedTrackGraph(tracker, project);

        var ctg1 = tracker.getCondensedGraph();
        var ctg2 = condensedTrackGraph.getCondensedGraph();

        assertEquals(ctg1.traversal().V().count().next(),
                     ctg2.traversal().V().count().next());
        assertTrue(ctg2.traversal().E().has(ASmellTracker.VERSION_POSITION).count().next() > 0);
    }
}