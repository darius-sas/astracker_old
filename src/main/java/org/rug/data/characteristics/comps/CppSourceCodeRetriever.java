package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
     * @param elementName the full name of the element.
     * @param extension the extension to use.
     * @return the source code of the given element, or {@link #NOT_FOUND} if file is not found.
     */
    @Override
    public String getSource(String elementName, String extension) {
        var src = super.getSource(elementName, extension);
        if (src.isEmpty() && !extension.equals(".c")){
            src = getSource(elementName, ".c");
            if (!src.isEmpty()){
                logger.info("Using same-name file with `.c` extension");
            }
        }
        return src;
    }

    @Override
    protected String toFileName(Vertex element) {
        String elementName = element.value("name");
        String extension;
        switch (VertexLabel.fromString(element.label())){
            case CFILE:
                extension = ".cpp";
                break;
            case HFILE:
                extension = ".h";
                break;
            case COMPONENT:
            default:
                extension = "";
                break;
        }
        return toFileName(elementName, extension);
    }

}
