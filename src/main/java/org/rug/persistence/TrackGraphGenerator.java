package org.rug.persistence;

import org.rug.tracker.ASmellTracker;

public class TrackGraphGenerator extends GraphDataGenerator<ASmellTracker> {

    public TrackGraphGenerator(String outputFile){
        super(outputFile);
    }

    @Override
    public void accept(ASmellTracker object) {
        super.graph = object.getFinalizedTrackGraph();
    }
}
