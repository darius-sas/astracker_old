package org.rug;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void executeMainArcan(){
        //executeMainProjectArcan("antlr");
        executeMainProjectArcan("argouml");
    }

    void executeMainProjectArcan(String projectName){
        var inputDir = "./test-data/input/" + projectName;
        var outputDir = "./test-data/output/";

        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
            Files.delete(Paths.get(outputDir, "arcanOutput", projectName));
        } catch (IOException e) {}

        Main.main(new String[]{"-p", projectName, "-i", inputDir, "-o", outputDir, "-rA", "-pC", "-pS", "-dNC"});

        assertTrue(Files.exists(Paths.get(outputDir, "arcanOutput", projectName)),
                error(projectName, "checking existence arcanOutput directory"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence smell characteristics"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence score similarity file"));
    }

    @Test
    void executeMain(){
        executeMainProject("antlr");
       // executeMainProject("argouml");
    }

    void executeMainProject(String projectName){

        var inputDir = "./test-data/output/arcanOutput/" + projectName;
        var outputDir = "./test-data/output/";
        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
        } catch (IOException e) {}

        Main.main(new String[]{"-p", projectName, "-i", inputDir, "-o", outputDir, "-pC", "-pS", "-dNC"});

        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence of smell characteristics file"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence similarity scores file"));
    }

    Supplier<String> error(String projectName, String cause){
        return ()-> String.format("Error %s for project %s.", cause, projectName);
    }
}