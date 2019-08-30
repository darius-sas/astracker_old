package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


public class JarClassSourceCodeRetrievalTest {

    JarClassSourceCodeRetrieval retriever = new JarClassSourceCodeRetrieval();
    String classPath;

    public JarClassSourceCodeRetrievalTest(){
        classPath = "test-data/jars/astracker-0.7.jar";
    }

    @Test
    void getClassSource() {
        var oracle = "public class JarClassSourceCodeRetrieval\nextends ClassSourceCodeRetriever {";
        retriever.setClassPath(classPath);
        var src = retriever.getClassSource("org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval");
        assertFalse(src.isEmpty());
        assertTrue(src.contains(oracle));
    }
}