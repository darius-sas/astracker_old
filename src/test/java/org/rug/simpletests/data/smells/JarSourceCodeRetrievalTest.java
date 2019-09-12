package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.JarSourceCodeRetrieval;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unitTests")
public class JarSourceCodeRetrievalTest {

    @Test
    void getClassSource() {
        JarSourceCodeRetrieval retriever = new JarSourceCodeRetrieval(new File("test-data/jars/astracker-0.7.jar").toPath());
        var oracle = "public class JarClassSourceCodeRetrieval\nextends ClassSourceCodeRetriever {";
        var src = retriever.getSource("org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval");
        assertFalse(src.isEmpty());
        assertTrue(src.contains(oracle));
    }
}