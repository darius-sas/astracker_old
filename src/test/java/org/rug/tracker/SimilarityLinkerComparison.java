package org.rug.tracker;

import org.junit.jupiter.api.Test;
import org.rug.data.Project;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.SmellCharacteristicsGenerator;
import org.rug.persistence.SmellSimilarityDataGenerator;
import org.rug.persistence.TrackGraphGenerator;

import java.io.File;
import java.io.IOException;

public class SimilarityLinkerComparison {

    @Test
    void compareJaccardLinkers() throws IOException, InterruptedException {
       compareJaccardLinkers("argouml");
       //compareJaccardLinkers("antlr");
    }

    void compareJaccardLinkers(String projectName) throws IOException, InterruptedException {
        var project = new Project(projectName);
        var outputDir = "./test-data/output/trackASOutput/" + project.getName() + "/linker-tests/";
        var outputDirF = new File(outputDir);
        outputDirF.mkdirs();
        project.addGraphMLs("./test-data/output/arcanOutput/" + project.getName());

        var normalLinker = new JaccardSimilarityLinker();
        var simpleNamesLinker = new SimpleNameJaccardSimilarityLinker();

        var normalGenerator = new SmellSimilarityDataGenerator(outputDir + "test-normal-link-scores.csv");
        var simpleNameGenerator = new SmellSimilarityDataGenerator(outputDir + "test-simple-link-scores.csv");

        var normalTracker = new ASmellTracker(normalLinker, false);
        var simpleNameTracker = new ASmellTracker(simpleNamesLinker, false);

        project.getVersionedSystem().forEach((version, inputTriple) ->{
            var smells = project.getArchitecturalSmellsIn(version);
            smells.forEach(ArchitecturalSmell::calculateCharacteristics);

            normalTracker.track(smells, version);
            normalGenerator.accept(normalTracker);

            simpleNameTracker.track(smells, version);
            simpleNameGenerator.accept(simpleNameTracker);
        });

        PersistenceWriter.writeCSV(normalGenerator);
        PersistenceWriter.writeCSV(simpleNameGenerator);

        var normalCharactGen = new SmellCharacteristicsGenerator(outputDir + "test-normal-characteristics.csv");
        normalCharactGen.accept(normalTracker);
        PersistenceWriter.writeCSV(normalCharactGen);

        var simpleCharactGen = new SmellCharacteristicsGenerator(outputDir + "test-simple-characteristics.csv");
        simpleCharactGen.accept(simpleNameTracker);
        PersistenceWriter.writeCSV(simpleCharactGen);

        var linkingTabScript = new ProcessBuilder("Rscript", "./data-analysis/jaccard-linking.r",
                normalGenerator.getOutputFile().toString(),
                outputDir + "test-normal-link-scores.pdf");
        linkingTabScript.start();

        linkingTabScript = new ProcessBuilder("Rscript", "./data-analysis/jaccard-linking.r",
                normalGenerator.getOutputFile().toString(),
                outputDir + "test-simple-link-scores.pdf");
        linkingTabScript.start();


        var notebookScript = new ProcessBuilder("R", "-e",
                "e<-new.env();e[['project']]<-'"+project.getName()+
                        "';e[['type']]<-'normal';e[['similarity_scores_file']]<-'"+normalGenerator.getOutputFile().getAbsolutePath()+
                        "';e[['characteristics_file']]<-'"+normalCharactGen.getOutputFile().getAbsolutePath()+
                        "';rmarkdown::render('./data-analysis/as-history-in-system.Rmd', output_file='"+outputDirF.getAbsolutePath()+
                        "/as-history-in-system-normal.nb.html',envir=e)");
        notebookScript.inheritIO();
        var p = notebookScript.start().waitFor();

        System.out.println("Normal notebook exit code " + p);

        notebookScript = new ProcessBuilder("R", "-e",
                "e<-new.env();e[['project']]<-'"+project.getName()+
                        "';e[['type']]<-'simple';e[['similarity_scores_file']]<-'"+simpleNameGenerator.getOutputFile().getAbsolutePath()+
                        "';e[['characteristics_file']]<-'"+simpleCharactGen.getOutputFile().getAbsolutePath()+
                        "';rmarkdown::render('./data-analysis/as-history-in-system.Rmd', output_file='"+outputDirF.getAbsolutePath()+
                        "/as-history-in-system-simple.nb.html',envir=e)");
        notebookScript.inheritIO();
        p = notebookScript.start().waitFor();
        System.out.println("Simple notebook exit code " + p);
    }
}
