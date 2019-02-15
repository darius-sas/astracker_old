package org.rug.runners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ArcanRunner extends ToolRunner {

    private final String version;
    private final String project;
    private final String outputDir;

    /**
     * Initializes an arcan runner with the following smells CD, HL, and UD.
     */
    public ArcanRunner(String inputJar, boolean isFolderOfJars, String project, String version, String outputDir){
        super("arcan", "-p", inputJar,
                isFolderOfJars ? "-folderOfJars" : "-jar",
                "-CD", "-HL", "-UD", "-CM", "-PM",
                "-out", outputDir + File.separator + "csv",
                "-neo4j", "-d", outputDir + File.separator + "neo4j-db");
        this.version = version;
        this.project = project;
        this.outputDir = outputDir;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {
        Files.move(Paths.get(getHomeDir(), "ToySystem-graph.graphml"),
                Paths.get(outputDir, String.format("%s-%s.graphml", project, version)));
    }
}
