package org.rug.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
public class ToolRunner {

    private final static Logger logger = LoggerFactory.getLogger(ToolRunner.class);

    private ProcessBuilder builder;

    private ToolRunner(String homeDirectory, boolean showOutput, String... command){
        this.builder = new ProcessBuilder(command);
        this.builder.directory(new File(homeDirectory));
        if (!showOutput) {
            this.builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            this.builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        }else {
            this.builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            this.builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
    }

    /**
     * Builds the underlying process and invokes {@link ProcessBuilder#start()} that causes the execution
     * of the underlying process.
     * @return The process being executed.
     */
    public Process start(){
        Process p = null;
        try {
            p = builder.start();
        }catch (IOException e) {
            logger.error("Could not start the following command: {}", String.join(" ", builder.command()));
            logger.error("The following error message was generated: {}", e.getMessage());
        }
        return p;
    }

    public ProcessBuilder getBuilder() {
        return builder;
    }

    private static Properties properties;

    /**
     * Initializes an external tool to be run using the properties file 'tools.properties' to retrieve the option
     * for the tool.
     * @param toolname The prefix name of the tool used in the properties file.
     * @param args The arguments to use for the given tool
     * @return an instance of a ToolRunner on which is possible to start the process of the given tool.
     */
    public static ToolRunner getTool(String toolname, String... args){
        String command = properties.getProperty(String.join(".", toolname, "cmdLine"));
        String homeDir = properties.getProperty(String.join(".", toolname, "homeDirectory"));
        boolean showOutput = Boolean.valueOf(properties.getProperty(String.join(".", toolname, "showOutput"), "false"));

        if (args == null)
            args = new String[]{""};
        List<String> commandLine = new ArrayList<>(Arrays.asList(command.split(" ")));
        commandLine.addAll(Arrays.asList(args));
        return new ToolRunner(homeDir, showOutput, commandLine.toArray(new String[0]));
    }

    static {
        properties = new Properties();
        try(InputStream is = ToolRunner.class.getClassLoader().getResourceAsStream("tools.properties")){
            if (is == null)
                throw new IOException("Resource tools.properties not found.");
            properties.load(is);
        }catch (IOException e){
            logger.error("Could not read properties file because: {}.", e.getMessage());
        }
    }

    private static <T> T[] append(T[] arr, T lastElement) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N+1);
        arr[N] = lastElement;
        return arr;
    }

    private static <T> T[] prepend(T[] arr, T firstElement) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N+1);
        System.arraycopy(arr, 0, arr, 1, N);
        arr[0] = firstElement;
        return arr;
    }

}
