package org.rug.persistence;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.tracker.ASmellTracker;

import java.io.File;

public class TrackGraphGenerator implements IGraphGenerator<ASmellTracker> {

    private File outputFile;
    private Graph graph;

    public TrackGraphGenerator(String outputFile){
        this.outputFile = new File(outputFile);
    }

    @Override
    public void accept(ASmellTracker object) {
        graph = object.getTrackGraph();
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }
}
