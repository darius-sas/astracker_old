package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private boolean isFolderOfJar;
    private SortedMap<String, Triple<Path, Path, Graph>> versionedSystem;

    public Project(String name){
        this.versionedSystem = new TreeMap<>();
        this.name = name;
        this.isFolderOfJar = false;
    }

    public void addJars(String mainJarProjectDir) throws IOException {
        Path jarDirPath =Paths.get(mainJarProjectDir);
        this.isFolderOfJar = !containsJars(jarDirPath);

        Consumer<Path> addVersion = j ->{
            var version = parseVersion(j);
            var t = versionedSystem.getOrDefault(version, new InputTriple(null, null));
            t.setA(j);
            versionedSystem.putIfAbsent(version, t);
        };

        if (isFolderOfJar){
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


    public void addGraphMLs(String graphMLDir) throws IOException{
        File dir = new File(graphMLDir);
        if (!dir.exists() || !containsGraphml(dir.toPath())) {
            versionedSystem.forEach((version, inputTriple) -> {
                var graphmlFile = Paths.get(graphMLDir, name, version, name, version, ".graphml");
                inputTriple.setB(graphmlFile);
            });
        }else {
            Files.walk(Paths.get(graphMLDir))
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().matches(".*\\.graphml"))
                    .forEach(f -> {
                        var version = parseVersion(f);
                        var t = versionedSystem.getOrDefault(version, new InputTriple(null, null));
                        t.setB(f);
                        versionedSystem.putIfAbsent(version, t);
                    });
        }
    }


    public String getName() {
        return name;
    }

    public boolean isFolderOfJarsProject() {
        return isFolderOfJar;
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
        int endIndex = f.toFile().isDirectory() ? f.toString().length() : f.toString().lastIndexOf('.');
        String version = f.toString().substring(
                f.toString().lastIndexOf('-') + 1,
                endIndex);
        return version;
    }

    private boolean containsJars(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.jar"));
    }

    private boolean containsGraphml(Path dir) throws IOException{
        return Files.list(dir).anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(".*\\.graphml"));
    }

    /**
     * This input triple lazily loads the graphml when it is first requested. This allows for Arcan to
     * calculate the graphml and avoids errors.
     */
    static class InputTriple extends Triple<Path, Path, Graph>{

        public InputTriple(Path jarPath, Path graphMLpath) {
            super(jarPath, graphMLpath, null);
        }

        @Override
        public Graph getC() {
            if (super.getC() == null) {
                this.c = TinkerGraph.open();
                try {
                    this.c.io(IoCore.graphml()).readGraph(getB().toAbsolutePath().toString());
                } catch (IOException e) {
                    logger.error("Could not read file {}", getB());
                }
            }
            return super.getC();
        }
    }
}
