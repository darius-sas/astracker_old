package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

public class TinySmell extends CDSmell {

    public TinySmell(Vertex smell) {
        super(smell);
        assert getAffectedElements().size() == 2;
    }
}
