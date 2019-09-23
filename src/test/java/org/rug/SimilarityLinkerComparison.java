package org.rug;

import org.junit.jupiter.api.Test;
import org.rug.data.project.Project;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceHub;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.JaccardSimilarityLinker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.File;
import java.io.IOException;

public class SimilarityLinkerComparison {

    @Test
    void compareJaccardLinkers() throws IOException {
       compareJaccardLinkers("ant");
       compareJaccardLinkers("antlr");
    }

    void compareJaccardLinkers(String projectName) throws IOException {
        var project = new Project(projectName);
        var outputDir = "./test-data/output/trackASOutput/" + project.getName() + "/linker-tests/";
        var outputDirF = new File(outputDir);
        outputDirF.mkdirs();
        project.addGraphMLfiles("./test-data/output/arcanOutput/" + project.getName());

        var normalLinker = new JaccardSimilarityLinker();
        var simpleNamesLinker = new SimpleNameJaccardSimilarityLinker();

        var normalGenerator = new SmellSimilarityDataGenerator(outputDir + "test-normal-link-scores.csv");
        var simpleNameGenerator = new SmellSimilarityDataGenerator(outputDir + "test-simple-link-scores.csv");

        var normalTracker = new ASmellTracker(normalLinker, false);
        var simpleNameTracker = new ASmellTracker(simpleNamesLinker, false);

        project.forEach(version ->{
            var smells = project.getArchitecturalSmellsIn(version);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);

            normalTracker.track(smells, version);
            normalGenerator.accept(normalTracker);

            simpleNameTracker.track(smells, version);
            simpleNameGenerator.accept(simpleNameTracker);
        });

        normalGenerator.writeOnFile();
        simpleNameGenerator.writeOnFile();
        simpleNameGenerator.close();

        var normalCharactGen = new SmellCharacteristicsGenerator(outputDir + "test-normal-characteristics.csv", project);
        normalCharactGen.accept(normalTracker);
        normalCharactGen.writeOnFile();
        normalCharactGen.close();

        var simpleCharactGen = new SmellCharacteristicsGenerator(outputDir + "test-simple-characteristics.csv", project);
        simpleCharactGen.accept(simpleNameTracker);
        simpleCharactGen.writeOnFile();
        simpleCharactGen.close();
    }
}
