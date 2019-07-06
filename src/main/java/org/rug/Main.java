package org.rug;

import com.beust.jcommander.JCommander;
import org.rug.args.Args;
import org.rug.data.project.Project;
import org.rug.data.project.Version;
import org.rug.persistence.*;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ProjecSizeRunner;
import org.rug.runners.ToolRunner;
import org.rug.runners.TrackASRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * The main of this tool sets up the computation of the necessary information in order to produce
     * the tracking output.
     * @param argv args to parse
     */
    public static void main(String[] argv)  {
        try {
            Args args = new Args();
            JCommander jc = JCommander.newBuilder()
                    .addObject(args)
                    .build();

            jc.setProgramName("java -jar trackas.jar");
            jc.parse(argv);

            if (args.help) {
                jc.usage();
                System.exit(0);
            }

            Analysis analysis = new Analysis(args);

            boolean errorsOccurred = false;
            for (var r : analysis.getRunners()) {
                int exitCode = r.start();
                errorsOccurred = exitCode != 0;
                if (errorsOccurred) {
                    break;
                }
            }
            if (!errorsOccurred) {
                logger.info("Writing to output directory...");
                PersistenceWriter.writeAllCSV();
                PersistenceWriter.writeAllGraphs();
            } else {
                System.exit(-1);
            }
        }catch (Exception e){
            logger.error("Unhandled error: {}", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
