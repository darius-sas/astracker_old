package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

/**
 * A utility class for storing pairs of sets of vertices.
 */
public class VSetPair extends Pair<Set<Vertex>, Set<Vertex>> {
    public VSetPair(Set<Vertex> a, Set<Vertex> b){
        super(a,b);
    }
}
