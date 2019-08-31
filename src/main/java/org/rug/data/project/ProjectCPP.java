package org.rug.data.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectCPP extends AbstractProject {

	private final static Logger logger = LoggerFactory.getLogger(ProjectCPP.class);

	private boolean hasGraphMLs;

	public ProjectCPP(String name) {
		super(name);
		this.hasGraphMLs = false;
		this.versionedSystem = new TreeMap<>(new StringVersionComparator());
	}
	
	/**
     * Adds the given directory of graphML files to the current versioned system.
     * If directory does not exist, this method will fill the current versioned systems
     * with the paths to the ghost graphMl files. In that case, the paths will have
     * the following format: graphMLDir/name/version/name-version.graphml.
     * @param graphMLDir the directory where to read graph files from, or where they should be written.
     * @throws IOException
     */
    public void addGraphMLs(String graphMLDir) throws IOException{
        File dir = new File(graphMLDir);

        var graphMlFiles = getGraphMls(dir.toPath());
        if (!graphMlFiles.isEmpty())
            graphMlFiles.forEach(f -> 
            		addVersion(f, version -> 
            					version.setGraphMLPath(f)));
        else
            versionedSystem.values().forEach(version -> {
                var graphmlFile = Paths.get(graphMLDir, name + "-" + version.getVersionString() + ".graphml");
                version.setGraphMLPath(graphmlFile);
            });
        hasGraphMLs = true;
        initVersionPositions();
    }
    
    private List<Path> getGraphMls(Path dir) throws IOException{
        return Files.list(dir).filter(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.graphml")).collect(Collectors.toList());
    }
    
    /**
     * Initializes the version positions.
     */
    private void initVersionPositions(){
        long counter = 1;
        for (var version : getVersionedSystem().values()){
            version.setVersionPosition(counter++);
        }
    }

    /**
     * Helper method that adds a file to the versions of the system.
     * @param f the file to add.
     * @param versionPathSetter a function that given a Version object, sets the path parameter(s) based
     *                          on their file format.
     */
    private void addVersion(Path f, Consumer<VersionCPP> versionPathSetter){
        var versionString = VersionCPP.parseVersion(f);
        var version = versionedSystem.getOrDefault(versionString, new VersionCPP(f));
        versionPathSetter.accept((VersionCPP) version);
        versionedSystem.putIfAbsent(version.getVersionString(), version);
    }
}
