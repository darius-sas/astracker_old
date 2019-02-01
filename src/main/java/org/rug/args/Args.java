package org.rug.args;

import com.beust.jcommander.Parameter;
import org.rug.data.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Args {

    private final static Logger logger = LoggerFactory.getLogger(Args.class);
    public final static String JAR_FILES_REGEX = ".*\\.jar";
    public final static String GRAPHML_FILES_REGEX = ".*\\.graphml";

    @Parameter(names = {"-outputDir", "-o"}, description = "This name will be used to generate an outputDir directory where the outputDir will be saved.", required = true, converter = OutputDirManager.class)
    public File outputDir;

    @Parameter(names = {"-input", "-i"}, description = "The input directory containing the JAR or graphML files. By default graphML files are used in order to avoid unnecessary computation. See -useJars for more information.", required = true, converter = InputDirManager.class)
    public File inputDirectory;

    @Parameter(names = {"-runArcan", "-rA"}, description = "Re-analyze JAR files with Arcan. This requires Arcan to be configured in the tools.properties file.")
    public boolean runArcan = false;

    @Parameter(names = {"-pSimilarity", "-pS"}, description = "Print similarity scores of the matched smells. This file is saved within the outputDir directory.")
    public boolean similarityScores = false;

    @Parameter(names = {"-pCharacteristics", "-pC"}, description = "Print the characteristics of the tracked smells for every analyzed version.")
    public boolean smellCharacteristics = true;

    @Parameter(names = {"-trackNonConsec", "-tco"}, description = "Whether to track smells across non consecutive versions. This allows to track 'reappeared' smells on.")
    public boolean trackNonConsecutiveVersions = true;

    @Parameter(names = {"--help", "-h", "-help", "-?"}, help = true)
    public boolean help;

    public String getSimilarityScoreFile(){
        return getOutputFileName("similarity-scores", "csv");
    }

    public String getSmellCharacteristicsFile(){
        return getOutputFileName("smell-characteristics", "csv");
    }

    public String getCondensedGraphFile(){
        return getOutputFileName("condensed-graph", "graphml");
    }

    public String getTrackGraphFileName(){
        return getOutputFileName("track-graph", "graphml");
    }

    private String getOutputFileName(String name, String format){
        String fileName = String.format("%s-%s.%s", name, (!trackNonConsecutiveVersions ? "consecOnly" : "nonConsec"), format);
        return Paths.get(outputDir.getAbsolutePath(), fileName).toString();
    }

    public File getOutputDir() {
        return outputDir;
    }


    /**
     * Gets all the input files that match the give format.
     * @param regex the regexp to use
     * @return a list of triples where the first element is the input file, the second element is the
     * project name and the third element is the version.
     */
    public List<Triple<String, String, String>> getInputTriples(String regex){
        Pattern p = Pattern.compile("[/\\\\][\\w\\d\\s]*-\\d");
        Pattern v = Pattern.compile("\\d+\\.\\d+(\\.\\d+\\w*)?");

        List<Triple<String, String, String>> inputTriples = new ArrayList<>();

        try {
            var files = Files.walk(inputDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(ff -> ff.getFileName().toString().matches(regex))
                    .map(Path::toString)
                    .collect(Collectors.toList());
            files.forEach(f -> {
                Matcher matcher = v.matcher(f);
                Matcher matcher2 = p.matcher(f);
                if (matcher.find() && matcher2.find()) {
                    String version = matcher.group();
                    String project = matcher2.group();
                    project = project.substring(1, project.length()-2);
                    inputTriples.add(new Triple<>(f, project, version));
                }else {
                    logger.error("Could not match the version of file: {}", f);
                    logger.info("The file was ignored.");
                    logger.info("Please make sure the input files match the following name convention: (projectName)-(version).(jar|graphml).");
                    logger.info("The version must match the following regex as well: {}", v.pattern());
                }
            });
            if (files.isEmpty())
                throw new IOException("Could not find any JAR file in the given input directory and subdirectories.");
        } catch (IOException e) {
            logger.error("Unable to read from input directory: {}", e.getMessage());
        }
        inputTriples.sort(Comparator.comparing(Triple::getC));
        return inputTriples;
    }
}
