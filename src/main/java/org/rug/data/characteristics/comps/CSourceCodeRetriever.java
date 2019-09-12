package org.rug.data.characteristics.comps;

import java.nio.file.Path;

public class CSourceCodeRetriever extends SourceCodeRetriever {

    public CSourceCodeRetriever(Path sourcePath) {
        super(sourcePath);
    }

    @Override
    public String getSource(String className) {
        return null;
    }

}
