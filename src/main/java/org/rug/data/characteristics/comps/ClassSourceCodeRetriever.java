package org.rug.data.characteristics.comps;

import java.nio.file.Path;

/**
 * This class manages the retrieval of the source code of a class from
 * multiple sources.
 */
public abstract class ClassSourceCodeRetriever {

    /**
     * Retrieves the source code as a string from the given class name.
     * @param className the full name of the class without the .java suffix (e.g. org.package.Class).
     * @return the source code of the class as string.
     */
    public abstract String getClassSource(String className);

	public abstract void setClassPath(Path path);
}
