package org.rug.data.characteristics.comps;

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
     * @param className the full name of the class without the .java suffix (e.g. org.package.Class).
     * @return the source code of the class as string or {@link #NOT_FOUND} if no class is found.
     */
    public abstract String getClassSource(String className);


    /**
     * Clear cached classes' source code.
     */
	public void clearCache(){
	    classesCache.clear();
    }
}
