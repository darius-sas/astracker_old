package org.rug.persistence;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.File;

public abstract class GraphDataGenerator<T> implements IGraphGenerator<T> {

    protected File outputFile;
    protected Graph graph;

    public GraphDataGenerator(String outputFile) {
        this.outputFile = new File(outputFile);
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public void writeOnFile() {
        this.graph.traversal().io(getOutputFile().getAbsolutePath())
                .write().with(IO.writer, IO.graphml).iterate();
    }

    @Override
    public void close() {}
}
