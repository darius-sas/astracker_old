package org.rug;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final String inputDir = "./test-data/input/";
    private final String outputDir = "./test-data/output/";
    private final String arcanCommand = "java -jar ./arcan/Arcan-1.4.0-SNAPSHOT.jar";


    void executeMainArcan(){
        executeMainProjectArcan("antlr");
        executeMainProjectArcan("argouml");
    }

    void executeMainProjectArcan(String projectName){
        var inputDir = this.inputDir;

        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
            Files.delete(Paths.get(outputDir, "arcanOutput", projectName));
        } catch (IOException e) {}

        Main.main(new String[]{"-p", projectName, "-i", inputDir, "-o", outputDir, "-rA", arcanCommand, "-pC", "-pS"});

        assertTrue(Files.exists(Paths.get(outputDir, "arcanOutput", projectName)),
                error(projectName, "checking existence arcanOutput directory"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence smell characteristics"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence score similarity file"));
    }


    void executeMain(){
        executeMainProject("antlr");
        executeMainProject("argouml");
    }

    void executeMainProject(String projectName){

        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
        } catch (IOException e) {}

        Main.main(new String[]{"-p", projectName, "-i", inputDir, "-o", outputDir, "-pC", "-pS"});

        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence of smell characteristics file"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence similarity scores file"));
    }

    Supplier<String> error(String projectName, String cause){
        return ()-> String.format("Error %s for project %s.", cause, projectName);
    }
    

    void testExecute(){
        Main.main(new String[]{"-p", "antlr", "-i", "./qualitas-corpus/input/", "-o", "./qualitas-corpus/output", "-pC", "-sAO", "-pS", "-rA", "java -jar arcan/Arcan-1.4.0-SNAPSHOT.jar"});
    }


    void testExecuteNew(){
        Main.main(new String[]{"-p", "antlr", "-i", "./qualitas-corpus/output/arcanOutput/",
                "-o", "./qualitas-corpus/outputNew", "-pC", "-sAO", "-pS",
                "-pCC", "./qualitas-corpus/input/"});
    }
}