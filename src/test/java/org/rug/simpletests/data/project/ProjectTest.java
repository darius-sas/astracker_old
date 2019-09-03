package org.rug.simpletests.data.project;

import org.junit.jupiter.api.Tag;
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
import static org.rug.simpletests.TestData.pure;

@Tag("unitTests")
public class ProjectTest {

    @Test
    void parseTest(){

        var project = parseTestInternal("antlr", "2.4.0", "2.5.0", "2.6.0", "2.7.0", "2.7.1", "2.7.2",
                "2.7.3", "2.7.4", "2.7.5", "2.7.6", "2.7.7", "3.0", "3.0.1", "3.1", "3.1.1", "3.1.2",
                "3.1.3", "3.2", "3.3", "3.4", "3.5", "4.0");

        assertTrue(project.isFolderOfFoldersOfSourcesProject());

        project = parseTestInternal("ant",   "1.1", "1.2", "1.3", "1.4", "1.4.1", "1.5",
                "1.5.1", "1.5.2", "1.5.3.1", "1.5.4", "1.6.0", "1.6.1", "1.6.2", "1.6.3",
                "1.6.4", "1.6.5", "1.7.0", "1.7.1", "1.8.0", "1.8.1", "1.8.2", "1.8.3", "1.8.4");
        assertTrue(project.isFolderOfFoldersOfSourcesProject());

    }

    Project parseTestInternal(String name, String... versions){
        var jarDirs = "./test-data/input/" + name;
        var graphMls = "./test-data/output/arcanOutput/" + name;
        Project pr = new Project(name);

        try {
            pr.addSourceDirectory(jarDirs);
            pr.addGraphMLfiles(graphMls);
        } catch (IOException e) {
            System.err.println("Error while reading data.");
        }

        assertEquals(Arrays.asList(versions), new ArrayList<>(pr.versions().stream().map(IVersion::getVersionString).collect(Collectors.toList())));
        pr.forEach(v -> {
            if(v instanceof Version){
                Version version = (Version)v;
                assertNotNull(version.getGraph());
                assertNotEquals(0, version.getGraph().traversal().V().count().next());
                assertNotEquals(0, version.getGraph().traversal().E().count().next());
                assertNotNull(version.getSourceCodePath());
            } else {
                fail("This test is only supposed to test for Java projects");
            }
        });

        var pr2 = new Project(name);
        var dir = new File(graphMls);
        var listDir = dir.list();
        if (listDir == null || listDir.length == 0)
            assertThrows(IllegalArgumentException.class, () -> pr2.addGraphMLfiles(graphMls), "Error while reading graphMLs.");
        else {
            assertDoesNotThrow(() -> pr2.addGraphMLfiles(graphMls), "Error while reading graphMLs.");
            assertTrue(pr2.numberOfVersions() > 0);
        }
        return pr;
    }

    @Test
    void parseTestCpp(){
        // Parsing for CPP projects should be the same, so we only do a quick test
        var versions = new String[] {"1.0.0.0", "1.0.0.1", "1.0.0.2", "1.0.0.3", "1.0.0.4"};
        assertEquals(Arrays.asList(versions), new ArrayList<>(pure.versions().stream().map(IVersion::getVersionString).collect(Collectors.toList())));
    }
}