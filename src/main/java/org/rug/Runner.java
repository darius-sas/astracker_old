package org.rug;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.tracker.ASTracker;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class Runner {

    public void run(String inputFileOrDir){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML(inputFileOrDir);
        ASTracker tracker = new ASTracker();
        for (Map.Entry<String, Graph> entry : versionedSystem.entrySet()){
            List<ArchitecturalSmell> smellsInTheSystem = ArchitecturalSmell.getArchitecturalSmellsIn(entry.getValue());

            // Calculate smell characteristics
            smellsInTheSystem.forEach(ArchitecturalSmell::calculateCharacteristics);

            // TODO: implement tracking state
            tracker.trackCD(null, null);

            // TODO: implement save on graph data and smell characteristic
            // code here
        }
    }
}
