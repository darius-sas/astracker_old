package org.rug.runners;

import org.rug.data.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class ArcanRunner extends ToolRunner {

    private final String version;
    private final Project project;

    /**
     * Initializes an arcan runner with the following smells CD, HL, and UD.
     */
    public ArcanRunner(String command, Project project, String version, String outputDir, boolean useNeo4j){
        super("arcan", "java -Xmx63000m -jar " + command);
        var args = Arrays.asList("-p", project.getVersionedSystem().get(version).getA().toString(),
                project.isFolderOfFoldersOfJarsProject() ? "-folderOfJars" : "-jar",
                "-CD", "-HL", "-UD", "-CM", "-PM",
                "-out", outputDir + File.separator + "csv");

        if (useNeo4j)
            args.addAll(Arrays.asList("-neo4j", "-d", outputDir + File.separator + "neo4j-db"));
        setArgs(args.toArray(new String[0]));
        this.version = version;
        this.project = project;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {
        try {
            Files.move(Paths.get(getHomeDir(), "ToySystem-graph.graphml"),
                    project.getVersionedSystem().get(version).getB(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new IOException("Could not move the graph file: " + e.getMessage());
        }
    }
}
