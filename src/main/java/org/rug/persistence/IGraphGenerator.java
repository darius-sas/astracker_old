package org.rug.persistence;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface IGraphGenerator<T> extends IDataGenerator<T> {
    Graph getGraph();
}
