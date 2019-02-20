package org.rug.data;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void parseTest(){

        var project = parseTestInternal("antlr", "2.7.1", "2.7.2", "2.7.5", "2.7.6", "2.7.7", "3.0",
                "3.0.1", "3.1", "3.1.1", "3.1.2", "3.1.3", "3.2", "3.3", "3.4", "3.5");

        assertTrue(!project.isFolderOfFoldersOfJarsProject());

        project = parseTestInternal("argouml",  "0.16.1", "0.18.1", "0.20", "0.22", "0.24", "0.26",
                "0.26.2","0.28", "0.28.1","0.30", "0.30.1","0.30.2", "0.32","0.32.1", "0.32.2", "0.34");

        assertTrue(project.isFolderOfFoldersOfJarsProject());

    }

    Project parseTestInternal(String name, String... versions){
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

        var pr2 = new Project(name);
        var dir = new File(graphMls);
        var listDir = dir.list();
        if (listDir == null || listDir.length == 0)
            assertThrows(IllegalArgumentException.class, () -> pr2.addGraphMLs(graphMls), "Error while reading graphMLs.");
        else {
            assertDoesNotThrow(() -> pr2.addGraphMLs(graphMls), "Error while reading graphMLs.");
            assertTrue(pr2.getVersionedSystem().size() > 0);
        }
        pr.getVersionedSystem().forEach((s, pathPathGraphTriple) -> {
            System.out.printf("%s -> %s%n", s, pathPathGraphTriple);
        });
        return pr;
    }

    @Test
    void qualitasParseTest() throws IOException {
        var project = new Project("ant");
        var jarDirs = "./qualitas-corpus/input/" + project.getName();

        var antversions = Arrays.asList("1.1", "1.2", "1.3", "1.4", "1.4.1", "1.5", "1.5.1", "1.5.2", "1.5.3.1",
                "1.5.4", "1.6.0", "1.6.1", "1.6.2", "1.6.3", "1.6.4", "1.6.5", "1.7.0", "1.7.1", "1.8.0",
                "1.8.1", "1.8.2", "1.8.3", "1.8.4");

        project.addJars(jarDirs);

        assertEquals(antversions, new ArrayList<>(project.getVersionedSystem().keySet()));

        var wekaversions = Arrays.asList(
        "3.0.1", "3.0.2", "3.0.3", "3.0.4", "3.0.5", "3.0.6", "3.1.7", "3.1.8", "3.1.9", "3.2", "3.2.1", "3.2.2",
                "3.2.3", "3.3", "3.3.1", "3.3.2", "3.3.3", "3.3.4", "3.3.5", "3.3.6", "3.4", "3.4.1", "3.4.10",
                "3.4.11", "3.4.12", "3.4.13", "3.4.2", "3.4.3", "3.4.4", "3.4.5", "3.4.6", "3.4.7", "3.4.8", "3.4.9",
                "3.5.0", "3.5.1", "3.5.2", "3.5.3", "3.5.4", "3.5.5", "3.5.6", "3.5.7", "3.5.8", "3.6.0", "3.6.1",
                "3.6.2", "3.6.3", "3.6.4", "3.6.5", "3.6.6", "3.6.7", "3.6.8", "3.6.9", "3.7.0", "3.7.1", "3.7.2",
                "3.7.3", "3.7.4", "3.7.5", "3.7.6", "3.7.7", "3.7.8", "3.7.9");

        project = new Project("weka");

        project.addJars("./qualitas-corpus/input/" + project.getName());

        assertEquals(wekaversions, new ArrayList<>(project.getVersionedSystem().keySet()));
    }
}