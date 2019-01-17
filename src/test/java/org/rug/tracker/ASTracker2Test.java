package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;

import java.util.SortedMap;


class ASTracker2Test {

    @Test
    void trackTest(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr");

        ASTracker2 tracker = new ASTracker2();
        versionedSystem.forEach( (version, graph) -> {
            tracker.track(graph, version);
        });

        tracker.writeTrackGraph("src/test/graphimages/trackgraph.graphml");
    }

}