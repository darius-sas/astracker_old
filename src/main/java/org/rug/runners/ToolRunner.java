package org.rug.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Models an external tool which command is defined in the 'tools.properties' file and arguments
 * can be given at runtime.
 */
public abstract class ToolRunner {

    private final static Logger logger = LoggerFactory.getLogger(ToolRunner.class);

    private ProcessBuilder builder;
    private String homeDir;
    private String command;
    private List<String> commandLine;
    private String toolName;

    /**
     * Initializes a tool with the given name and the given command.
     * @param toolName The prefix toolName of the tool used in the properties file.
     * @param command The command to execute.
     */
    public ToolRunner(String toolName, String command){
        this.toolName = toolName;
        this.command = command;
        this.homeDir = new java.io.File( "." ).getAbsolutePath();
    }

    /**
     * Sets the arguments to pass to this tool.
     * @param args the array containing the arguments.
     */
    protected final void setArgs(String... args){
        if (args == null)
            args = new String[]{""};
        commandLine = new ArrayList<>(Arrays.asList(command.split(" ")));
        commandLine.addAll(Arrays.asList(args));
        this.builder = new ProcessBuilder(commandLine);

    }

    /**
     * Builds the underlying process and invokes {@link ProcessBuilder#start()} that causes the execution
     * of the underlying process.
     * @return The process being executed.
     */
    public int start(){
        int exitCode;
        Process p;
        try {
            logger.info("Running {} with command: {}", getToolName(), String.join(" ", commandLine));
            preProcess();
            p = builder.start();
            exitCode = p.waitFor();
            if (exitCode == 0)
                postProcess(p);
            logger.info("Completed {} with exit code {}.", getToolName(), exitCode);
        }catch (IOException e) {
            logger.error("Could not start the following command: {}", String.join(" ", commandLine));
            logger.error("The following error message was generated: {}", e.getMessage());
            exitCode = -1;
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            exitCode = -1;
        }
        return exitCode;
    }

    protected abstract void preProcess();
    protected abstract void postProcess(Process p) throws IOException;

    public ProcessBuilder getBuilder() {
        return builder;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
        this.builder.directory(new File(this.homeDir));
    }

    public void inheritOutput(boolean showOutput){
        if (!showOutput) {
            this.builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            this.builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        } else {
            this.builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            this.builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
    }

    public String getToolName() {
        return toolName;
    }


    public String getHomeDir() {
        return homeDir;
    }

    @Override
    public String toString() {
        return String.join(" ", commandLine);
    }

}
