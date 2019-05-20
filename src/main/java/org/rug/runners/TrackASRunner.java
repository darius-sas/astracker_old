package org.rug.runners;

import org.rug.data.project.Project;
import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval;
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
        tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), trackNonConsecutiveVersions);

        JarClassSourceCodeRetrieval retriever = project.hasJars() ? new JarClassSourceCodeRetrieval() : null;
        var componentCharacteristics = new ComponentCharacteristicSet(retriever).getCharacteristicSet();

        var numOfVersions = project.numberOfVersions();
        logger.info("Starting tracking architectural smells of {} for {} versions", project.getName(), numOfVersions);
        logger.info("Tracking non consecutive versions: {}", trackNonConsecutiveVersions ? "yes" : "no");
        project.forEach(version -> {
            logger.info("Tracking version {} (n. {} of {})", version.getVersionString(), version.getVersionPosition(), numOfVersions);
            var graph = version.getGraph();
            List<ArchitecturalSmell> smells = project.getArchitecturalSmellsIn(version);
            if (retriever != null) {
                retriever.setClassPath(version.getJarPath()); //update sources to current version
            }
            componentCharacteristics.forEach(c -> c.calculate(graph));
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            logger.info("Linked {} smells out of a total of {} in this version.", tracker.getScorer().bestMatch().size(), smells.size());
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
