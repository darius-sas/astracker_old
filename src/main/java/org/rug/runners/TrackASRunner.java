package org.rug.runners;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.Project;
import org.rug.data.characteristics.ComponentCharacteristicSet;
import org.rug.data.characteristics.IComponentCharacteristic;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.util.Triple;
import org.rug.persistence.*;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.JaccardSimilarityLinker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedMap;

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

        logger.info("Starting tracking architectural smells of {} for {} versions", project.getName(), versionedSystem.size());
        logger.info("Tracking non consecutive versions: {}", trackNonConsecutiveVersions ? "yes" : "no");
        versionedSystem.forEach( (version, inputTriple) -> {
            logger.info("Tracking version {}", version);
            var graph = inputTriple.getC();
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            componentCharacteristics.forEach(c -> c.calculate(graph));
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            logger.info("Linked {} smells out of a total of {} in this version.", tracker.getScorer().bestMatch().size(), smells.size());
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });
        logger.info("Tracking complete, writing files to output directory...");
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.sendTo(TrackGraphGenerator.class, tracker);
        PersistenceWriter.sendTo(CondensedGraphGenerator.class, tracker);
        return 0;
    }

    @Override
    protected void preProcess() {}

    @Override
    protected void postProcess(Process p){}
}
