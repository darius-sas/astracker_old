package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Retrieves source code of Java classes from Java projects.
 */
public class JavaSourceCodeRetriever extends SourceCodeRetriever {

    private final static Logger logger = LoggerFactory.getLogger(JavaSourceCodeRetriever.class);

    /**
     * Instantiates a retriever for Java projects.
     * @param sourcePath the path to the directory which children are the packages of the Java classes.
     */
    public JavaSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    /**
     * Transforms element from a full name description (e.g. org.package.Class) to a file name path (e.g. org/package/Class.java).
     * @param element the element to extract the file path from.
     * @return a string that represents the name of the file defining the class and the packages containing
     * such class.
     */
    @Override
    protected String toFileName(Vertex element) {
        String elementName = element.value("name");
        return toFileName(elementName);
    }

    private String toFileName(String elementName){
        return elementName.replace('.', File.separatorChar) + ".java";
    }

    @Override
    public Optional<Path> getPathOf(String elementName) {
        var elementFile = Paths.get(sourcePath.toString(), toFileName(elementName)).toFile();
        if (!elementFile.exists() || !elementFile.isFile()){
            logger.error("Could not find file: {}", elementFile);
        }
        return elementFile.exists() ? Optional.of(elementFile.toPath()) : Optional.empty();
    }

}
