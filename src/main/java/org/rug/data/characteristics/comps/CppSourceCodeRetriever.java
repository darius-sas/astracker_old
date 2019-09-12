package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Retrieves CPP source code.
 */
public class CppSourceCodeRetriever extends SourceCodeRetriever {

    private static final Logger logger = LoggerFactory.getLogger(CppSourceCodeRetriever.class);

    public CppSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    /**
     * Retrieves the source code of the given element. This method also looks for .c files in case
     * no .cpp or .h file is found with the given extension.
     * @param elementName the full name of the element with a suffix (e.g. .cpp, .c, .h)
     * @return the source code of the given element, or {@link #NOT_FOUND} if file is not found.
     */
    @Override
    public String getSource(String elementName) {
        if (!classesCache.containsKey(elementName)) {
            try (var walk = Files.walk(sourcePath)) {
                var elementFile = walk.filter(p -> p.getFileName().endsWith(elementName)).findFirst();
                if (elementFile.isPresent()) {
                    var source = Files.readString(elementFile.get());
                    classesCache.putIfAbsent(elementName, source);
                }else if (!elementName.endsWith(".c")){
                    var newElementName = elementName.substring(0, elementName.lastIndexOf("."));
                    newElementName = newElementName + ".c";
                    return getSource(newElementName);
                }
            } catch (IOException e) {
                logger.error("Could not read source code from file: {}", elementName);
            }
        }
        return classesCache.getOrDefault(elementName, NOT_FOUND);
    }

    /**
     * Retrieves the source code of the given vertex element using the name and the label of the element
     * to correctly invoke {@link #getSource(String)}.
     * @param element the element to retrieve the source code of.
     * @return See {@link #getSource(String)}.
     */
    @Override
    public String getSource(Vertex element) {
        String elementName = element.value("name");
        switch (VertexLabel.valueOf(element.label())){
            case CFILE:
                elementName = elementName + ".cpp";
                break;
            case HFILE:
                elementName = elementName + ".h";
                break;
        }
        return getSource(elementName);
    }
}
