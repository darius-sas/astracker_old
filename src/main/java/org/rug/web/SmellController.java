package org.rug.web;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.tracker.ASmellTracker;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SmellController {

    @RequestMapping(value = "/smell-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Smell> smellList(@RequestParam(value="system", defaultValue="") String system){
        var graphFile = "/home/fenn/git/data/trackASOutput/mina/condensed-graph-consecOnly.graphml";
        var graph = TinkerGraph.open();
        graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
        var smells = new ArrayList<Smell>();

        // More details in SmellCharacteristicsGenerator#accept()
        graph.traversal().V().hasLabel(ASmellTracker.SMELL).forEachRemaining(v -> {
            var smell = new Smell(v.value(ASmellTracker.UNIQUE_SMELL_ID), v.value(ASmellTracker.SMELL_TYPE));
            smells.add(smell);
        });
        return smells;
    }
}
