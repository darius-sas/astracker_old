package org.rug.data.characteristics.comps;

import org.junit.jupiter.api.Test;
import org.rug.data.Project;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NumberOfLinesOfCodeTest {

    Project project;

    public NumberOfLinesOfCodeTest() {
        project = new Project("antlr");
    }

    @Test
    void calculate() throws IOException {
        JarClassSourceCodeRetrieval retriever = new JarClassSourceCodeRetrieval();

        retriever.setClassPath("target/trackas/trackas-0.5.jar");

        var src = retriever.getClassSource(AbstractComponentCharacteristic.class.getCanonicalName());
        assertFalse(src.isEmpty());
        var linesOfCode = src.split("[\n|\r]");
        var nbloc = Arrays.stream(linesOfCode).filter(line -> line.length() > 0).count();
        assertTrue(nbloc > 50);

        project.addJars("");
        var charLOC = new NumberOfLinesOfCode(retriever);
        retriever.setClassPath(project.getVersionedSystem().get("3.2.1").getA());
    }
}