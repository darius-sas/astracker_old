package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class manages the retrieval of the source code of a class from
 * multiple sources.
 * This class handles the logic to retrieve the source code files.
 */
public abstract class SourceCodeRetriever {

    private final static Logger logger = LoggerFactory.getLogger(SourceCodeRetriever.class);

    protected Map<String, String> classesCache;
    protected Path sourcePath;
    protected Set<Path> deepPaths;

    protected final static String NOT_FOUND = "";

    /**
     * Instantiates a source code retriever.
     * @param sourcePath the path to the source directory.
     */
    public SourceCodeRetriever(Path sourcePath) {
        this.classesCache = new HashMap<>();
        this.sourcePath = sourcePath;
        try( var stream = Files.walk(sourcePath)){
            this.deepPaths = stream.filter(p -> p.toFile().isFile()).collect(Collectors.toSet());
        } catch (IOException e) {
            this.deepPaths = new HashSet<>();
        }
    }

    /**
     * Retrieves the source code as a string from the given class name.
     * @param elementName the full name of the element (suffix may or may not be necessary based on the implementation).
     * @return the source code of the element as string or {@link #NOT_FOUND} if no class is found.
     */
    public String getSource(String elementName, String extension) {
        String key = toFileName(elementName, extension);
        if (!classesCache.containsKey(key)) {
            var classFile = getPathOf(elementName, extension);
            try {
                if (classFile.isPresent()) {
                    var source = Files.readString(classFile.get());
                    classesCache.putIfAbsent(key, source);
                } else {
                    throw new IOException();
                }
            } catch (IOException e) {
                logger.error("Could not read source from: {}", key);
            }
        }

        return classesCache.getOrDefault(key, NOT_FOUND);
    }


    /**
     * Returns the source code of the given vertex element as described by {@link #getSource(String, String)}.
     * @param element the element to retrieve the source code of.
     * @return the source code of the element as string or {@link #NOT_FOUND} if no class is found.
     */
    public String getSource(Vertex element) {
        if (element.values("ClassType", "Type").next().toString().toLowerCase().contains("retrieved")) {
            return "";
        }
        var fileName = toFileName(element);
        var extension = fileName.substring(fileName.lastIndexOf("."));
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return getSource(fileName, extension);
    }

    /**
     * Clear cached classes' source code.
     */
	public void clearCache(){
	    classesCache.clear();
    }

    /**
     * Transform the given element into a file name based on the specific implementation.
     * For example, C/CPP projects might attach the file extension to the `name` property, whereas
     * Java project may get the full path based on the full name of the class/package retrieved.
     * Please refer to actual implementations for more details.
     * @param element the element to extract the file path from.
     * @return a string containing the file name.
     */
    protected abstract String toFileName(Vertex element);

    /**
     * Default implementation pastes the element name and extension together.
     * @param elementName the name of the element
     * @param extension the extension with a . as a prefix.
     * @return a string in the following format `#elementName.#extension`.
     */
    protected String toFileName(String elementName, String extension) {
        return String.format("%s%s", elementName, extension);
    }

    /**
     * Return the path of a component with the given `name` property.
     * @param elementName the name of the component.
     * @return the Path object to the given element or null if no element was found.
     */
    public Optional<Path> getPathOf(String elementName, String extension){
        return findFile(toFileName(elementName, extension));
    }

    /**
     * Returns the path of a component based on its `name` property and using {@link #toFileName(Vertex)}.
     * This method ignores retrieved classes (e.g. Java lang classes, primitives, etc.).
     * @param component the vertex (please note that only vertices that refer to files/packages/etc. will work.
     * @return the Path instance of the given component or null if no element was found.
     */
    public Optional<Path> getPathOf(Vertex component) {
        if (component.values("ClassType", "Type").next().toString().toLowerCase().contains("retrieved")) {
            return Optional.empty();
        }
        return findFile(toFileName(component));
    }

    /**
     * Finds a file by searching recursively in the current source path for a file path that
     * ends with the given file name(or path) suffix.
     * @param fileName the file (optionally including the path) to find.
     * @return an optional Path.
     */
    protected Optional<Path> findFile(String fileName){
        Optional<Path> elementFile = deepPaths.stream().filter(p -> p.endsWith(fileName)).findFirst();
        if (elementFile.isEmpty()) {
            logger.debug("Could not find source file: {}", fileName);
        }else {
            logger.debug("Found file: {}", fileName);
        }
        return elementFile;
    }

    /**
     * Returns the given path as a relative path starting from the current {@link #sourcePath} directory.
     * @param path the path to relativize
     * @return the path relative to the source path or an empty optional otherwise.
     */
    public Optional<Path> relativize(Optional<Path> path){
        if (path.isEmpty())
            return Optional.empty();
        return path.map(value -> sourcePath.relativize(value));
    }
}
