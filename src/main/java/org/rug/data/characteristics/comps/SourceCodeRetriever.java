package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the retrieval of the source code of a class from
 * multiple sources.
 */
public abstract class SourceCodeRetriever {

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
    public abstract String getSource(String elementName);

    /**
     * Retrieves the source code of the given vertex element using the "name" property.
     * @param element the element to retrieve the source code of.
     * @return the source code of the element as string or {@link #NOT_FOUND} if no class is found.
     */
    public String getSource(Vertex element){
        return getSource(element.value("name").toString());
    }

    /**
     * Clear cached classes' source code.
     */
	public void clearCache(){
	    classesCache.clear();
    }
}
