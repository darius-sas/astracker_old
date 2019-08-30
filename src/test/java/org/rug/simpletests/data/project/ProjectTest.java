package org.rug.simpletests.data.project;

import org.junit.jupiter.api.Test;
import org.rug.data.project.IVersion;
import org.rug.data.project.Project;
import org.rug.data.project.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {

    @Test
    void parseTest(){

        var project = parseTestInternal("antlr", "2.4.0", "2.5.0", "2.6.0", "2.7.0", "2.7.1", "2.7.2",
                "2.7.3", "2.7.4", "2.7.5", "2.7.6", "2.7.7", "3.0", "3.0.1", "3.1", "3.1.1", "3.1.2",
                "3.1.3", "3.2", "3.3", "3.4", "3.5", "4.0");

        assertTrue(project.isFolderOfFoldersOfJarsProject());

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

        assertEquals(Arrays.asList(versions), new ArrayList<>(pr.versions().stream().map(IVersion::getVersionString).collect(Collectors.toList())));


        var pr2 = new Project(name);
        var dir = new File(graphMls);
        var listDir = dir.list();
        if (listDir == null || listDir.length == 0)
            assertThrows(IllegalArgumentException.class, () -> pr2.addGraphMLs(graphMls), "Error while reading graphMLs.");
        else {
            assertDoesNotThrow(() -> pr2.addGraphMLs(graphMls), "Error while reading graphMLs.");
            assertTrue(pr2.numberOfVersions() > 0);
        }
        return pr;
    }
}