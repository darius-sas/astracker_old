package org.rug.args;

import com.beust.jcommander.IStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class InputDirManager implements IStringConverter<File> {

    private final static Logger logger = LoggerFactory.getLogger(InputDirManager.class);

    @Override
    public File convert(String s) {
        File f = new File(s);
        if (!f.exists() || !f.canRead()) {
            logger.error("Unable to access input directory: {}", s);
            System.exit(-1);
        }
        return f;
    }
}
