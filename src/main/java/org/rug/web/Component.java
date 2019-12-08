package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.tracker.ASmellTracker;

import java.util.*;

/**
 * Represents a component (class, package, file, folder, etc.) of the system existing in
 * multiple versions and affected by multiple smells.
 */
public class Component extends VersionSpanningNode {

    private final String name;
    private final String type;
    private final Map<Long, List<Long>> affectedBy;

    /**
     * Build the component starting from a component vertex from the condensed graph.
     * @param component the vertex.
     */
    public Component(Vertex component) {
        this.name = component.value(ASmellTracker.NAME);
        this.type = component.value(ASmellTracker.COMPONENT_TYPE);
        this.affectedBy = new TreeMap<>();
        setCharacteristics(component);
        setAffectedBy(component);
    }

    /**
     * The identifier of this component.
     * @return a string representing the name of this component.
     */
    public String getName() {
        return name;
    }

    /**
     * The type of this component (class, package, file, header, etc.)
     * @return a string representing the type.
     */
    public String getType() {
        return type;
    }

    /**
     * A map where the keys are the version indexes and the values are lists of smell ids.
     * @return a map.
     */
    public Map<Long, List<Long>> getAffectedBy() {
        return affectedBy;
    }

    private void setAffectedBy(Vertex component){
        var graph = component.graph();
        var affectsEdges = graph.traversal().V(component).inE(ASmellTracker.AFFECTS).toSet();
        for (Edge edge : affectsEdges) {
            affectedBy.compute(edge.value(ASmellTracker.VERSION_INDEX), (k, v) -> {
                if (v == null){
                    v = new ArrayList<>();
                }
                v.add(edge.outVertex().value(ASmellTracker.UNIQUE_SMELL_ID));
                return v;
            });
        }
    }

    private void setCharacteristics(Vertex component){
        var graph = component.graph();
        var hasCharactEdges = graph.traversal().V(component).outE(ASmellTracker.HAS_CHARACTERISTIC).toSet();
        for (Edge edge : hasCharactEdges) {
            long affectedVersion = edge.value(ASmellTracker.VERSION_INDEX);
            spanningVersions.add(affectedVersion);
            characteristics.put(affectedVersion, propertiesToMap(edge.inVertex()));
        }
    }
}
