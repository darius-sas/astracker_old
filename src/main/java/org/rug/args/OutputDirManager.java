package org.rug.args;

import com.beust.jcommander.IStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OutputDirManager implements IStringConverter<File> {

    private final static Logger logger = LoggerFactory.getLogger(OutputDirManager.class);

    @Override
    public File convert(String s) {
        File f = new File(s);
        if (!f.exists()) {
            if(!f.mkdirs()) {
                logger.error("Unable to create output directory: {}", s);
                System.exit(-1);
            }
        }
        return f;
    }
}
