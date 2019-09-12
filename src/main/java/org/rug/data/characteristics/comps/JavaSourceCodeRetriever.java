package org.rug.data.characteristics.comps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Retrieves source code of Java classes from Java projects.
 */
public class JavaSourceCodeRetriever extends SourceCodeRetriever {

    /**
     * Instantiates a retriever for Java projects.
     * @param sourcePath the path to the directory which children are the packages of the Java classes.
     */
    public JavaSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    @Override
    public String getSource(String className) {
        var classFile = Paths.get(sourcePath.toString(),
                className.replace('.', File.separatorChar) + ".java").toFile();
        if (!classesCache.containsKey(className)) {
            if (classFile.exists() && classFile.isFile()) {
                try {
                    var source = Files.readString(classFile.toPath());
                    classesCache.putIfAbsent(className, source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return classesCache.getOrDefault(className, NOT_FOUND);
    }

}
