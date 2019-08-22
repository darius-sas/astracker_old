package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.project.ProjectCPP;
import org.rug.data.project.VersionCPP;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedMap;

class ASmellTrackerTest {

	private final static Logger logger = LoggerFactory.getLogger(ASmellTrackerTest.class);

	@Test
	void trackTest() throws IOException {
		var name = "pure";
		var dir = "./arcanCppOutput/pure";
		ProjectCPP pr = new ProjectCPP(name);
		pr.addGraphMLs(dir);

//		SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML(dir);

		ISimilarityLinker scorer = new JaccardSimilarityLinker();
		ASmellTracker tracker = new ASmellTracker(scorer, false);

		PersistenceWriter
				.register(new SmellSimilarityDataGenerator("test-data/jaccard-scores-antlr-consecutives-only.csv"));
		// PersistenceWriter.register(new
		// SmellCharacteristicsGenerator("test-data/smells-characteristics.csv", null));
		// // this test is out of date, added null to allow compilation

		pr.forEach(v -> {
			logger.info("Tracking version {}- {}", v, v.getGraph());
			List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(v.getGraph());
			smells.forEach(ArchitecturalSmell::calculateCharacteristics);
			tracker.track(smells, v);
			PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
		});
//		int counter = 1;
//      for (var entry : versionedSystem.entrySet()){
//          var version = new VersionCPP(Paths.get(dir+entry.getKey()));
//          version.setVersionPosition(counter++);
//          var graph = entry.getValue();
//          List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(graph);
//          smells.forEach(ArchitecturalSmell::calculateCharacteristics);
//          logger.info("Tracking version {}", version);
//          tracker.track(smells, version);
//          PersistenceWriter.sendTo(SmellSimilarityDataGenerator.class, tracker);
//      }
//		PersistenceWriter.sendTo(SmellCharacteristicsGenerator.class, tracker);
		PersistenceWriter.writeAllCSV();
//		logger.info("Tracking completed. Generating simplified graph...");
//		tracker.writeCondensedGraph("src/test/graphimages/simplified-trackgraph-consecutives.graphml");
//		tracker.writeTrackGraph("src/test/graphimages/trackgraph-consecutives.graphml");

	}

}