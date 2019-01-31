package org.rug.args;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Args {

    @Parameter(names = {"-output", "-o"}, description = "This name will be used to generate an output directory where the output will be saved.", required = true, converter = OutputDirManager.class)
    File output;

    @Parameter(names = {"-input", "-i"}, description = "The input directory containing the JAR or graphML files. By default graphML files are used in order to avoid unnecessary computation. See -useJars for more information.", required = true)
    public String inputDirectory;

    @Parameter(names = {"-runArcan", "-rA"}, description = "Re-analyze JAR files with Arcan. This requires Arcan to be configured in the tools.properties file.")
    public boolean runArcan = false;

    @Parameter(names = {"-pSimilarity", "-pS"}, description = "Print similarity scores of the matched smells. This file is saved within the output directory.")
    public boolean similarityScores = false;

    @Parameter(names = {"-pCharacteristics", "-pC"}, description = "Print the characteristics of the tracked smells for every analyzed version.")
    public boolean smellCharacteristics = true;

    @Parameter(names = {"-trackNonConsec", "-tco"}, description = "Whether to track smells across non consecutive versions. This allows to track 'reappeared' smells on.")
    public boolean trackNonConsecutiveVersions = true;

    @Parameter(names = {"--help", "-h", "-help", "-?"}, help = true)
    public boolean help;

    public String getSimilarityScoreFile(){
        return Paths.get(output.getAbsolutePath(), "similarity-scores.csv").toString();
    }

    public String getSmellCharacteristicsFile(){
        return Paths.get(output.getAbsolutePath(), "smell-characteristics.csv").toString();
    }

    public String getCondensedGraphFile(){
        String fileName = String.format("condensed-graph-%s.graphml", (trackNonConsecutiveVersions ? "consecOnly" : "nonConsec"));
        return Paths.get(output.getAbsolutePath(), fileName).toString();
    }

    public String getTrackGraphFileName(){
        String fileName = String.format("track-graph-%s.graphml", (trackNonConsecutiveVersions ? "consecOnly" : "nonConsec"));
        return Paths.get(output.getAbsolutePath(), fileName).toString();
    }

    public File getOutput() {
        return output;
    }

    public List<String> getJarFilesList(){
        try {
            return Files.walk(Paths.get(inputDirectory))
                    .filter(Files::isRegularFile)
                    .filter(ff -> ff.getFileName().toString().matches(".*\\.jar"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
