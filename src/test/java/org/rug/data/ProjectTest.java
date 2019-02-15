package org.rug.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void parseTest(){

        parseTestInternal("antlr", "2.7.1", "2.7.2", "2.7.5", "2.7.6", "2.7.7", "3.0",
                "3.0.1", "3.1", "3.1.1", "3.1.2", "3.1.3", "3.2", "3.3", "3.4", "3.5");


        parseTestInternal("argouml",  "0.16.1", "0.18.1", "0.20", "0.22", "0.24", "0.26",
                "0.26.2","0.28", "0.28.1","0.30", "0.30.1","0.30.2", "0.32","0.32.1", "0.32.2", "0.34");

    }

    void parseTestInternal(String name, String... versions){
        var jarDirs = "./test-data/input/" + name;
        var graphMls = "./test-data/output/arcanOutput/" + name;
        Project pr = new Project(name);
        try {
            pr.addJars(jarDirs);
            pr.addGraphMLs(graphMls);
        } catch (IOException e) {
            System.err.println("Error while reading data.");
        }

        assertEquals(Arrays.asList(versions), new ArrayList<>(pr.getVersionedSystem().keySet()));

        pr.getVersionedSystem().forEach((s, pathPathGraphTriple) -> {
            System.out.printf("%s -> %s%n", s, pathPathGraphTriple);
        });
    }

}