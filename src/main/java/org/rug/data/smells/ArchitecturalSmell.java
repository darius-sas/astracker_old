package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public abstract class ArchitecturalSmell {
    private long id;
    private List<Vertex> smellNodes;
    private List<Vertex> affectedElements;

    public ArchitecturalSmell(Vertex smell){

    }
}
