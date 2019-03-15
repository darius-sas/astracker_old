package org.rug.runners;

import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.Project;
import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.*;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Models the execution of the tracking tool.
 */
public class TrackASRunner extends ToolRunner {

    private final static Logger logger = LoggerFactory.getLogger(TrackASRunner.class);

    private ASmellTracker tracker;
    private Project project;
    private boolean trackNonConsecutiveVersions;

    public TrackASRunner(Project project, boolean trackNonConsecutiveVersions) {
        super("trackas", "");
        this.project = project;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
    }

    @Override
    public int start() {
        var versionedSystem = project.getVersionedSystem();
        tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), trackNonConsecutiveVersions);
        var componentCharacteristics = new ComponentCharacteristicSet().getCharacteristicSet();

        var count = new Counter(1);
        var total = versionedSystem.size();
        logger.info("Starting tracking architectural smells of {} for {} versions", project.getName(), total);
        logger.info("Tracking non consecutive versions: {}", trackNonConsecutiveVersions ? "yes" : "no");
        versionedSystem.forEach( (version, inputTriple) -> {
            logger.info("Tracking version {} (n. {} of {})", version, count.postIncrement(), total);
            var graph = inputTriple.getC();
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            componentCharacteristics.forEach(c -> c.calculate(graph));
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            logger.info("Linked {} smells out of a total of {} in this version.", tracker.getScorer().bestMatch().size(), smells.size());
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });
        logger.info("Tracking complete, processing data...");
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.sendTo(ComponentAffectedByGenerator.class, tracker);
        PersistenceWriter.sendTo(TrackGraphGenerator.class, tracker);
        PersistenceWriter.sendTo(CondensedGraphGenerator.class, tracker);
        return 0;
    }

    @Override
    protected void preProcess() {}

    @Override
    protected void postProcess(Process p){}

    private static class Counter{
        int counter;
        public Counter(int val){
            counter = val;
        }
        public int postIncrement(){
            return counter++;
        }
    }
}
