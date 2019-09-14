package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

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
     * This method removes inner classes and returns the files were they are defined.
     * @param element the element to extract the file path from.
     * @return a string that represents the name of the file defining the class and the packages containing
     * such class.
     */
    @Override
    protected String toFileName(Vertex element) {
        String elementName = element.value("name");
        String extension;
        switch (VertexLabel.fromString(element.label())){
            case CLASS:
                extension = ".java";
                break;
            default:
                extension = "";
                break;
        }
        return toFileName(elementName, extension);
    }

    /**
     * Converts the dotted name of the given element to a concrete file name with the .java extension.
     * @param elementName the name of the element to convert.
     * @return a path suffix of the given element.
     */
    @Override
    protected String toFileName(String elementName, String extension){
        return clean(elementName).replace('.', File.separatorChar) + extension;
    }

    /**
     * Removes internal classes and lambdas from and extracts the file name without extension.
     * @param elementName the elementName to clean.
     * @return a cleaned element name string.
     */
    private String clean(String elementName) {
        if (elementName.contains("$")) {
            elementName = elementName.substring(0, elementName.indexOf("$"));
        }
        return elementName;
    }
}
