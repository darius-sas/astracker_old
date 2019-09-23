package org.rug.persistence;

import org.rug.tracker.ASmellTracker;

public class CondensedGraphGenerator extends GraphDataGenerator<ASmellTracker> {


    public CondensedGraphGenerator(String outputFile){
        super(outputFile);
    }


    @Override
    public void accept(ASmellTracker object) {
        graph = object.getCondensedGraph();
    }

}
