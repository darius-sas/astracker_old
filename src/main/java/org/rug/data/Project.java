package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Represents a project with multiple versions.
 */
public class Project {

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    private String name;
    private boolean isSingleJarPerVersionProject;
    private SortedMap<String, Triple<Path, Path, Graph>> versionedSystem;

    public Project(String name){
        this.versionedSystem = new TreeMap<>();
        this.name = name;
        this.isSingleJarPerVersionProject = true;
    }

    public void addJars(String mainJarProjectDir) throws IOException {
        Path jarDirPath =Paths.get(mainJarProjectDir);
        this.isSingleJarPerVersionProject = containsJars(jarDirPath);

        Consumer<Path> addVersion = j ->{
            var version = parseVersion(j);
            var t = versionedSystem.getOrDefault(version, new Triple<>(null, null, null));
            t.setA(j);
            versionedSystem.putIfAbsent(version, t);
        };

        if (isSingleJarPerVersionProject){
            Files.list(jarDirPath)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().matches(".*\\.jar"))
                    .forEach(addVersion);
        }else{
            Files.list(jarDirPath)
                    .filter(Files::isDirectory)
                    .forEach(addVersion);
        }
    }


    public void addGraphMLs(String graphMLDir) throws IOException {
        Files.walk(Paths.get(graphMLDir))
                .filter(Files::isRegularFile)
                .filter(f -> f.getFileName().toString().matches(".*\\.graphml"))
                .forEach(f -> {
                    var version = parseVersion(f);
                    Graph graph = TinkerGraph.open();
                    try {
                        graph.io(IoCore.graphml()).readGraph(f.toAbsolutePath().toString());
                    } catch (IOException e) {
                        logger.error("Unable to read graph from file: ", e.getMessage());
                    }
                    var t = versionedSystem.getOrDefault(version, new Triple<>(null, null, null));
                    t.setB(f);
                    t.setC(graph);
                    versionedSystem.putIfAbsent(version, t);
                });
    }


    public String getName() {
        return name;
    }

    public boolean isSingleJarPerVersionProject() {
        return isSingleJarPerVersionProject;
    }

    /**
     * Returns a sorted map where keys are versions of the system and values are triples
     * where the first element is the directory, or jar file, corresponding to the graphml file, saved as the second
     * element, and also to corresponding system graph, saved as third element.
     * @return a sorted map as described above.
     */
    public SortedMap<String, Triple<Path, Path, Graph>> getVersionedSystem() {
        return versionedSystem;
    }

    private String parseVersion(Path f){
        String version = f.toString().substring(
                f.toString().lastIndexOf('-') + 1,
                f.toString().lastIndexOf('.'));
        return version;
    }

    private boolean containsJars(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.jar"));
    }

}
