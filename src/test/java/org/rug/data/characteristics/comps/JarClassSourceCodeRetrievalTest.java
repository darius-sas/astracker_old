package org.rug.data.characteristics.comps;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class JarClassSourceCodeRetrievalTest {

    JarClassSourceCodeRetrieval retriever = new JarClassSourceCodeRetrieval();


    @Test
    void getClassSource() {
        var oracle = "public class JarClassSourceCodeRetrieval\nextends ClassSourceCodeRetriever {";
        retriever.setClassPath("target/trackas/trackas-0.6.jar");
        var src = retriever.getClassSource("org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval");
        assertFalse(src.isEmpty());
        assertTrue(src.contains(oracle));

        retriever.setClassPath(Paths.get("target/trackas/"));
        src = retriever.getClassSource("org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval");
        assertFalse(src.isEmpty());
        assertTrue(src.contains(oracle));
    }
}