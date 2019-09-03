package org.rug.runners;

import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.project.IProject;
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

        project.forEach(version -> {
            logger.info("Tracking version {} (n. {} of {})", version.getVersionString(), version.getVersionPosition(), project.numberOfVersions());
            List<ArchitecturalSmell> smells = project.getArchitecturalSmellsIn(version);

            componentCharacteristics.forEach(c -> c.calculate(version));
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);

            tracker.track(smells, version);

            logger.info("Linked {} smells out of a total of {} in this version.", tracker.smellsLinked(), smells.size());
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
            PersistenceWriter.sendTo(ComponentMetricGenerator.class, version);
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

}
