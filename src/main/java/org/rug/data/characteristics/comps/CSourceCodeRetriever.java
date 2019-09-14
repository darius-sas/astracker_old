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
        String extension;
        switch (VertexLabel.fromString(element.label())){
            case HFILE:
                extension = ".h";
                break;
            case CFILE:
                extension =  ".c";
                break;
            case COMPONENT:
            default:
                extension = "";
        }
        return toFileName(elementName, extension);
    }

}
