package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.tracker.ASmellTracker;

import java.util.*;

public class Smell {

    private final long id;
    private final String type;
    private final long age;
    private final String firstVersionAppeared;
    private final String lastVersionDetected;
    private final Set<Long> affectedVersions;
    private final Map<Long, Map<String, String>> characteristics;
    private final Map<Long, List<String>> affectedComponents;

    public Smell(Vertex smell) {
        this.id = smell.value(ASmellTracker.UNIQUE_SMELL_ID);
        this.type = smell.value(ASmellTracker.SMELL_TYPE);
        this.age = smell.value(ASmellTracker.AGE);
        this.firstVersionAppeared = smell.value(ASmellTracker.FIRST_APPEARED);
        this.lastVersionDetected = smell.value(ASmellTracker.LAST_DETECTED);
        this.characteristics = new TreeMap<>();
        this.affectedVersions = new TreeSet<>();
        this.affectedComponents = new TreeMap<>();
        setAffectedComponents(smell);
        setCharacteristics(smell);
    }

    private void setAffectedComponents(Vertex smell){
        var graph = smell.graph();
        var hasCharactEdges = graph.traversal().V(smell).outE(ASmellTracker.HAS_CHARACTERISTIC).toSet();
        for (Edge edge : hasCharactEdges) {
            long affectedVersion = edge.value(ASmellTracker.VERSION_INDEX);
            affectedVersions.add(affectedVersion);
            characteristics.put(affectedVersion, propertiesToMap(edge.inVertex()));
        }
    }

    private void setCharacteristics(Vertex smell){
        var graph = smell.graph();
        var hasCharactEdges = graph.traversal().V(smell).outE(ASmellTracker.AFFECTS).toSet();
        for (Edge edge : hasCharactEdges) {
            long affectedVersion = edge.value(ASmellTracker.VERSION_INDEX);
            affectedVersions.add(affectedVersion);
            affectedComponents.compute(affectedVersion, (k, v) -> {
                if (v == null){
                    v = new ArrayList<>();
                }
                v.add(edge.inVertex().value(ASmellTracker.NAME));
                return v;
            });
        }
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Long getAge() {
        return age;
    }

    public String getFirstVersionAppeared() {
        return firstVersionAppeared;
    }

    public String getLastVersionDetected() {
        return lastVersionDetected;
    }

    public Set<Long> getAffectedVersions() {
        return affectedVersions;
    }

    public Map<Long, Map<String, String>> getCharacteristics() {
        return characteristics;
    }

    public Map<Long, List<String>> getAffectedComponents() {
        return affectedComponents;
    }

    private Map<String, String> propertiesToMap(Vertex vertex){
        var map = new HashMap<String, String>();
        vertex.keys().forEach(k -> map.put(k, vertex.value(k)));
        return map;
    }
}
