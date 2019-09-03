package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.AbstractComponentCharacteristic;
import org.rug.data.characteristics.comps.JarClassSourceCodeRetrieval;
import org.rug.data.characteristics.comps.NumberOfLinesOfCode;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.rug.simpletests.TestData.antlr;

public class NumberOfLinesOfCodeTest {

    String classPath;

    public NumberOfLinesOfCodeTest() {
        classPath = "test-data/jars/astracker-0.7.jar";
    }

    @Test
    void calculate() throws IOException {
        JarClassSourceCodeRetrieval retriever = new JarClassSourceCodeRetrieval();

        retriever.setClassPath(classPath);

        var src = retriever.getClassSource(AbstractComponentCharacteristic.class.getCanonicalName());
        assertFalse(src.isEmpty());
        var linesOfCode = src.split("[\n|\r]");
        var nbloc = Arrays.stream(linesOfCode).filter(line -> line.length() > 0).count();
        assertTrue(nbloc >= 50);

        var charLOC = new NumberOfLinesOfCode();
        var vSys = antlr.getVersion("3.2");
        charLOC.setSourceRetriever(vSys.getSourceCodeRetriever());

        var vertex = vSys.getGraph().traversal().V().has("name", "antlr.DefineGrammarSymbols").next();
        assertNotNull(vertex);
        charLOC.calculate(vertex);
        assertTrue((Long)(vertex.value(charLOC.getName())) > 0);

        retriever.clear();

        vertex = vSys.getGraph().traversal().V().hasLabel("package").has("name", "antlr").next();
        assertNotNull(vertex);
        charLOC.calculate(vertex);

        var loc = (Long)vertex.value("linesOfCode");
        assertTrue(loc > 0);

        vertex.vertices(Direction.IN, "belongsTo").forEachRemaining(charLOC::calculate);
        assertEquals(loc, vertex.value("linesOfCode"));

    }

}