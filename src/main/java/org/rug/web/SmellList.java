package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.tracker.ASmellTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SmellList {

    private final List<Smell> smellList = new ArrayList<>(100);
    private final Map<Long, String> versions = new TreeMap<>();

    public SmellList(Graph graph){
        graph.traversal().E()
                .has(ASmellTracker.VERSION)
                .has(ASmellTracker.VERSION_INDEX)
                .forEachRemaining(e -> versions.put(e.value(ASmellTracker.VERSION_INDEX), e.value(ASmellTracker.VERSION)));
        graph.traversal().V().hasLabel(ASmellTracker.SMELL).forEachRemaining(v -> smellList.add(new Smell(v)));
    }

    public List<Smell> getSmellList() {
        return smellList;
    }

    public Map<Long, String> getVersions() {
        return versions;
    }
}
