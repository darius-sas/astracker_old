package org.rug.data.project;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionCPP extends Version {

	private final static Logger logger = LoggerFactory.getLogger(VersionCPP.class);

	/**
     * Partially builds this instance by parsing the version string from the given path.
     * @param path the directory or file that respect {@link #parseVersion(Path)} version formatting.
     */
    public VersionCPP(Path path){
        super(path, null); //TO-DO: change null with the ClassSourceCodeRetriever for cpp 
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", super.getVersionString(), super.getGraphMLPath());
    }
}
