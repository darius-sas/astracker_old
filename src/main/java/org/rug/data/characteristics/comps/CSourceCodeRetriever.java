package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Retrieves source code of C files.
 */
public class CSourceCodeRetriever extends SourceCodeRetriever {

    private static final Logger logger = LoggerFactory.getLogger(CSourceCodeRetriever.class);

    public CSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    /**
     * Attaches the file extension based on the label of the vertex
     * @param element the element to extract the file path from.
     * @return a file name without any path component.
     */
    protected String toFileName(Vertex element){
        String elementName = element.value("name");
        switch (VertexLabel.valueOf(element.label())){
            case HFILE:
                elementName = elementName + ".h";
                break;
            case CFILE:
                elementName = elementName + ".c";
                break;
        }
        return elementName;
    }

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
}
