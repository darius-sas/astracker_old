package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Retrieves source code of C files.
 */
public class CSourceCodeRetriever extends SourceCodeRetriever {

    private static final Logger logger = LoggerFactory.getLogger(CSourceCodeRetriever.class);

    public CSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    /**
     * Reads the source code of the given file.
     * @param elementName the full name of the element (suffix may or may not be necessary based on the implementation).
     * @return a string containing the source code of the given element.
     */
    @Override
    public String getSource(String elementName) {
        if (!classesCache.containsKey(elementName)) {
            try (var walk = Files.walk(sourcePath)) {
                var elementFile = walk.filter(p -> p.getFileName().endsWith(elementName)).findFirst();
                if (elementFile.isPresent()) {
                    var source = Files.readString(elementFile.get());
                    classesCache.putIfAbsent(elementName, source);
                }
            } catch (IOException e) {
                logger.error("Could not read source code from file: {}", elementName);
            }
        }
        return classesCache.getOrDefault(elementName, NOT_FOUND);
    }

    /**
     * Returns the source code of the given vertex element as described by {@link #getSource(String)}.
     * @param element the element to retrieve the source code of.
     * @return the source code of the element as string.
     */
    @Override
    public String getSource(Vertex element) {
        String elementName = element.value("name");
        switch (VertexLabel.valueOf(element.label())){
            case HFILE:
                elementName = elementName + ".h";
                break;
            case CFILE:
                elementName = elementName + ".c";
                break;
        }
        return getSource(elementName);
    }

}
