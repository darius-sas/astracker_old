package org.rug.simpletests;

import org.junit.jupiter.api.Test;
import org.rug.Main;
import org.rug.persistence.PersistenceWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final String inputDirJars = "./test-data/input/";
    private final String inputDirGraphMLs = "./test-data/output/arcanOutput/";
    private final String outputDir = "./test-data/output/";
    private final String arcanCommand = "java -jar ./arcan/Arcan-1.4.0-SNAPSHOT.jar";


    void executeMainArcan(){
        executeMainProjectArcan("antlr");
        executeMainProjectArcan("ant");
    }

    void executeMainProjectArcan(String projectName){

        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
            Files.delete(Paths.get(outputDir, "arcanOutput", projectName));
        } catch (IOException e) {}

        Main.main(new String[]{"-p", projectName, "-i", inputDirJars, "-o", outputDir, "-rA", arcanCommand, "-pC", "-pS"});

        assertTrue(Files.exists(Paths.get(outputDir, "arcanOutput", projectName)),
                error(projectName, "checking existence arcanOutput directory"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence smell characteristics"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence score similarity file"));
    }

    @Test
    void integrationTestAnt(){
        executeMainProject("antlr");
    }


    void integrationTestPure(){
        executeMainProject("pure");
    }

    void executeMainProject(String projectName){

        try {
            Files.delete(Paths.get(outputDir, "trackASOutput", projectName));
        } catch (IOException e) {}

        PersistenceWriter.clearAll();

        Main.main(new String[]{"-p", projectName, "-i", inputDirGraphMLs, "-o", outputDir, "-pC", "-pS"});

        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "smell-characteristics-consecOnly.csv")),
                error(projectName, "checking existence of smell characteristics file"));
        assertTrue(Files.exists(Paths.get(outputDir, "trackASOutput", projectName, "similarity-scores-consecOnly.csv")),
                error(projectName, "checking existence similarity scores file"));
        PersistenceWriter.clearAll();
    }

    Supplier<String> error(String projectName, String cause){
            return ()-> String.format("Error %s for project %s.", cause, projectName);
    }
}