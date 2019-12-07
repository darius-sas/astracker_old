package org.rug.runners;

import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;
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
    private IProject project;
    private boolean trackNonConsecutiveVersions;

    public TrackASRunner(IProject project, boolean trackNonConsecutiveVersions) {
        super("trackas", "");
        this.project = project;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
    }

    @Override
    public int run() {
        tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), trackNonConsecutiveVersions);

        var componentCharacteristics = new ComponentCharacteristicSet().getCharacteristicSet();

        logger.info("Starting tracking architectural smells of {} for {} versions", project.getName(), project.numberOfVersions());
        logger.info("Tracking non consecutive versions: {}", trackNonConsecutiveVersions ? "yes" : "no");

        project.forEach((version, index) -> {
            logger.info("Tracking version {} (n. {} of {})", version.getVersionString(), index, project.numberOfVersions());
            List<ArchitecturalSmell> smells = project.getArchitecturalSmellsIn(version);

            logger.debug("Computing component characteristics...");
            componentCharacteristics.forEach(c -> c.calculate(version));
            logger.debug("Computing smell characteristics...");
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);

            logger.debug("Tracking smells...");
            tracker.track(smells, version);

            logger.info("Linked {} smells out of a total of {} in this version.", tracker.smellsLinked(), smells.size());
            PersistenceHub.sendToAndWrite(SmellSimilarityDataGenerator.class, tracker);
            PersistenceHub.sendToAndWrite(ComponentMetricGenerator.class, version);
            version.clearGraph();
        });

        logger.info("Tracking complete, processing data...");
        PersistenceHub.sendToAndWrite(SmellCharacteristicsGenerator.class, tracker);
        PersistenceHub.sendToAndWrite(ComponentAffectedByGenerator.class, tracker);
        PersistenceHub.sendToAndWrite(TrackGraphGenerator.class, tracker);
        PersistenceHub.sendToAndWrite(CondensedGraphGenerator.class, tracker);
        return 0;
    }

    @Override
    protected void preProcess() {}

    @Override
    protected void postProcess(Process p){}

}
