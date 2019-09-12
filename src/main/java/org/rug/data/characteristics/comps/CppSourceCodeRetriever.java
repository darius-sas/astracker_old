package org.rug.data.characteristics.comps;

import java.nio.file.Path;

public class CppSourceCodeRetriever extends SourceCodeRetriever {

    public CppSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    @Override
    public String getClassSource(String className) {
        return null;
    }

}
