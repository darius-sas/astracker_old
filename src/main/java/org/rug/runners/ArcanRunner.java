package org.rug.runners;

import org.rug.data.project.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class ArcanRunner extends ToolRunner {

    private final Version version;

    /**
     * Initializes an arcan runner with the following smells CD, HL, and UD.
     */
    public ArcanRunner(String command, Version version, String outputDir, boolean isFolderOfFoldersOfJars, boolean useNeo4j){
        super("arcan", "java -Xmx63000m -jar " + command);
        var args = Arrays.asList("-p", version.getSourceCodePath().toAbsolutePath().toString(),
                isFolderOfFoldersOfJars ? "-folderOfJars" : "-jar",
                "-CD", "-HL", "-UD", "-CM", "-PM",
                "-out", outputDir + File.separator + "csv");

        if (useNeo4j)
            args.addAll(Arrays.asList("-neo4j", "-d", outputDir + File.separator + "neo4j-db"));
        setArgs(args.toArray(new String[0]));
        this.version = version;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {
        try {
            Files.move(Paths.get(getHomeDir(), "ToySystem-graph.graphml"),
                    version.getGraphMLPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new IOException("Could not move the graph file: " + e.getMessage());
        }
    }
}
