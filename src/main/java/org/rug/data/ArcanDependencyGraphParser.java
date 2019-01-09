package org.rug.data;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
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
 * Parses Arcan's result from different formats into graphs representing the system analyzed
 * and the AS within it found by Arcan.
 */
public class ArcanDependencyGraphParser {

    private static Logger logger = LoggerFactory.getLogger(ArcanDependencyGraphParser.class);

    /**
     * Parses all the .graphml files in the given path and builds a sorted map with versions as keys and graphs as values.
     * If path is a file a map with a single entry is returned.
     * @param path the path to a directory of graphml files with names in the format '[projectname]-[version].graphml'.
     * @return A sorted map as described above.
     */
    public static SortedMap<String, Graph> parseGraphML(String path){
        SortedMap<String, Graph> versionedSystem = new TreeMap<>();


        Consumer<Path> addGraph = f -> {
            String version = f.getFileName().toString().substring(
                    f.getFileName().toString().lastIndexOf('-') + 1,
                    f.getFileName().toString().lastIndexOf('.'));
            Graph graph = TinkerGraph.open();
            try {
                graph.io(IoCore.graphml()).readGraph(f.toAbsolutePath().toString());
                versionedSystem.put(version, graph);
            } catch (IOException e) {
                logger.error("Unable to read graph from file: ", e.getMessage());
            }
        };

        Path f = Paths.get(path);

        if (Files.isDirectory(f)) {
            try {
                Files.walk(Paths.get(path))
                        .filter(Files::isRegularFile)
                        .filter(ff -> ff.getFileName().toString().matches(".*\\.graphml"))
                        .forEach(addGraph);
            } catch (IOException e) {
                logger.error("Unable to walk the given path: ", path);
            }
        }else {
            addGraph.accept(f);
        }

        return versionedSystem;
    }

}
