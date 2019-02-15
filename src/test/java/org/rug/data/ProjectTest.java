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
        var name = "antlr";
        var jarDirs = "./test-data/input/" + name;
        var graphMls = "./test-data/output/arcanOutput/" + name;
        Project pr = new Project(name);
        try {
            pr.addJars(jarDirs);
            pr.addGraphMLs(graphMls);
        } catch (IOException e) {
            System.err.println("Error while reading data.");
            e.printStackTrace();
        }


        List<String> antlrVersions = Arrays.asList("2.7.1", "2.7.2", "2.7.5", "2.7.6", "2.7.7", "3.0",
                "3.0.1", "3.1", "3.1.1", "3.1.2", "3.1.3", "3.2", "3.3", "3.4", "3.5");

        assertEquals(antlrVersions, new ArrayList<>(pr.getVersionedSystem().keySet()));
        //TODO add another project with multiple jars
    }

}