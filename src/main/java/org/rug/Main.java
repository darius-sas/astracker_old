package org.rug;

import com.beust.jcommander.JCommander;
import org.rug.args.Args;
import org.rug.persistence.PersistenceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;

/**
 * Main entry point of the system.
 */
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * The main of this tool sets up the computation of the necessary information in order to produce
     * the tracking output.
     * @param argv args to parse
     */
    public static void main(String... argv)  {
        try {
            Args args = new Args();
            JCommander jc = JCommander.newBuilder()
                    .addObject(args)
                    .build();

            jc.setProgramName("java -jar astracker.jar");
            jc.parse(argv);

            if (args.help) {
                jc.usage();
                System.exit(0);
            }

            Analysis analysis = new Analysis(args);

            boolean errorsOccurred = false;
            String errorRunnerName = "";
            long start = System.nanoTime();
            for (var r : analysis.getRunners()) {
                int exitCode = r.run();
                errorsOccurred = exitCode != 0;
                if (errorsOccurred) {
                    errorRunnerName = r.getToolName();
                    break;
                }
            }
            long end = System.nanoTime();
            if (errorsOccurred) {
                logger.error("Unexpected errors have occurred while running runner: {}", errorRunnerName);
                System.exit(-1);
            }

            logger.info("Writing to output directory...");
            PersistenceHub.closeAll();
            logger.info("Elapsed time: {}", toElapsedString(end - start));
        }catch (Exception e){
            logger.error("Unhandled error: {}", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Returns a formatted string of minutes and seconds elapsed.
     * @param elapsedNanoSeconds the nano seconds elapsed
     * @return a formatted string representing elapsed time as mm:s.
     */
    private static String toElapsedString(long elapsedNanoSeconds){
        double elapsedMinutes = (elapsedNanoSeconds * 1e-9) / 60d;
        long minutes = Math.round(Math.floor(elapsedMinutes));
        long seconds = Math.abs(Math.round((elapsedMinutes - minutes) * 100 * 0.6d));
        return String.format("%d minutes %s seconds", minutes, seconds);
    }
}
