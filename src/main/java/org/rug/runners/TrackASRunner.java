package org.rug.runners;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.*;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.JaccardSimilarityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

/**
 * Models the execution of the tracking tool.
 */
public class TrackASRunner extends ToolRunner {

    private final static Logger logger = LoggerFactory.getLogger(TrackASRunner.class);

    private SortedMap<String, Graph> versionedSystem;
    private ASmellTracker tracker;
    private String inputDirectory;
    private String projectName;
    private boolean trackNonConsecutiveVersions;

    public TrackASRunner(String projectName, String inputDirectory, boolean trackNonConsecutiveVersions) {
        super(null);
        this.projectName = projectName;
        this.inputDirectory = inputDirectory;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
    }

    @Override
    public void start() {
        versionedSystem = ArcanDependencyGraphParser.parseGraphML(inputDirectory);
        tracker = new ASmellTracker(new JaccardSimilarityLinker(), trackNonConsecutiveVersions);

        logger.info("Starting tracking architectural smells of {} for {} versions", projectName, versionedSystem.size());
        logger.info("Tracking non consecutive versions: {}", trackNonConsecutiveVersions ? "yes" : "no");
        versionedSystem.forEach( (version, graph) -> {
            logger.info("Tracking version {}", version);
            List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);
            tracker.track(smells, version);
            PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
        });
        logger.info("Tracking complete, writing files to output directory...");
        PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
        PersistenceWriter.sendTo(TrackGraphGenerator.class, tracker);
        PersistenceWriter.sendTo(CondensedGraphGenerator.class, tracker);
    }

    @Override
    protected void preProcess() {}

    @Override
    protected void postProcess(Process p){}
}
