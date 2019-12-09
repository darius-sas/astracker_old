package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a node that is present in one or multiple versions of the system.
 */
public abstract class VersionSpanningNode {

    protected final TreeSet<Long> spanningVersions;
    protected final Map<Long, Map<String, String>> characteristics;

    public VersionSpanningNode() {
        this.spanningVersions = new TreeSet<>();
        this.characteristics  = new TreeMap<>();
    }

    /**
     * The last version this element affects.
     * @return a version index.
     */
    public long getLastVersion(){
        return spanningVersions.last();
    }

    /**
     * The first version this element affects.
     * @return a version index.
     */
    public long getFirstVersion(){
        return spanningVersions.first();
    }

    /**
     * The versions where this element is present.
     * @return a list of version indexes.
     */
    public TreeSet<Long> getSpanningVersions() {
        return spanningVersions;
    }

    /**
     * A map of maps. The keys of the main map are the version indexes whereas each map has the names of the characteristics as keys and their respective values as values.
     * For example, an entry could have the following structure when written as a JSON object:
     * {"23": {"characteristicName1": "0.231", "characteristicName2": "420"}, "24": ...}
     * @return a map of maps.
     */
    public Map<Long, Map<String, String>> getCharacteristics() {
        return characteristics;
    }

    /**
     * Utility method that copies all properties in a separated map.
     * @param vertex the vertex to extract the properties from
     * @return a map with the properties of vertex.
     */
    protected Map<String, String> propertiesToMap(Vertex vertex){
        var map = new HashMap<String, String>();
        vertex.keys().forEach(k -> map.put(k, vertex.value(k).toString()));
        return map;
    }
}
