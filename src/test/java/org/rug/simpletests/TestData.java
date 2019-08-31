package org.rug.simpletests;

import org.rug.data.project.Project;

import java.io.IOException;

public class TestData {
    public final static Project antlr = new Project("antlr");
    private final static String antlrProjectDir = "./test-data/input/antlr";
    private final static String antlrGraphMLDir = "./test-data/output/arcanOutput/antlr";

    public final static Project pure = new Project("pure");
    private final static String pureProjectDir = "./test-data/input/antlr";
    private final static String pureGraphMLDir = "./test-data/output/arcanOutput/antlr";

    static {
        try {
            antlr.addJars(antlrProjectDir);
            antlr.addGraphMLs(antlrGraphMLDir);
        } catch (IOException e){
            System.err.println("Cannot load antlr project for tests execution. Are you sure the paths are correct?");
            System.err.println(String.format("Project dir: %s", antlrProjectDir));
            System.err.println(String.format("GraphML dir: %s", antlrGraphMLDir));
        }

        try {
            pure.addGraphMLs(antlrGraphMLDir);
        } catch (IOException e){
            System.err.println("Cannot load antlr project for tests execution. Are you sure the paths are correct?");
            System.err.println(String.format("Project dir: %s", antlrProjectDir));
            System.err.println(String.format("GraphML dir: %s", antlrGraphMLDir));
        }
    }

}
