package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class manages the retrieval of the source code of a class from
 * multiple sources.
 */
public abstract class SourceCodeRetriever {

    private final static Logger logger = LoggerFactory.getLogger(SourceCodeRetriever.class);

    protected Map<String, String> classesCache;
    protected Path sourcePath;

    protected final static String NOT_FOUND = "";

    /**
     * Instantiates a source code retriever.
     * @param sourcePath the path to the source directory.
     */
    public SourceCodeRetriever(Path sourcePath) {
        this.classesCache = new HashMap<>();
        this.sourcePath = sourcePath;
    }

    /**
     * Retrieves the source code as a string from the given class name.
     * @param elementName the full name of the element (suffix may or may not be necessary based on the implementation).
     * @return the source code of the element as string or {@link #NOT_FOUND} if no class is found.
     */
    public String getSource(String elementName) {
        if (!classesCache.containsKey(elementName)) {
            var classFile = getPathOf(elementName);
            try {
                if (classFile.isPresent()) {
                    var source = Files.readString(classFile.get());
                    classesCache.putIfAbsent(elementName, source);
                } else {
                    throw new IOException();
                }
            } catch (IOException e) {
                logger.error("Could not read source from: {}", elementName);
            }
        }

        return classesCache.getOrDefault(elementName, NOT_FOUND);
    }


    /**
     * Returns the source code of the given vertex element as described by {@link #getSource(String)}.
     * @param element the element to retrieve the source code of.
     * @return the source code of the element as string or {@link #NOT_FOUND} if no class is found.
     */
    public String getSource(Vertex element) {
        return getSource(toFileName(element));
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
     * Return the path of a component with the given `name` property.
     * @param elementName the name of the component.
     * @return the Path object to the given element or null if no element was found.
     */
    public Optional<Path> getPathOf(String elementName){

            Optional<Path> elementFile = Optional.empty();
            try (var walk = Files.walk(sourcePath)) {
                elementFile = walk.filter(p -> p.getFileName().endsWith(elementName)).findFirst();
            } catch (IOException e) {
                logger.error("Could not find source file: {}", elementName);
            }
            return elementFile;

    }

    /**
     * Returns the path of a component based on its `name` property and using {@link #toFileName(Vertex)}.
     * @param component the vertex (please note that only vertices that refer to files/packages/etc. will work.
     * @return the Path instance of the given component or null if no element was found.
     */
    public Optional<Path> getPathOf(Vertex component) {
        var elementName = toFileName(component);
        return getPathOf(elementName);
    }
}
