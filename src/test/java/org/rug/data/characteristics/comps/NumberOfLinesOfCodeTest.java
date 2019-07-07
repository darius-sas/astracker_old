package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.jupiter.api.Test;
import org.rug.data.project.Project;

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

        retriever.setClassPath("target/trackas/trackas-0.6.jar");

        var src = retriever.getClassSource(AbstractComponentCharacteristic.class.getCanonicalName());
        assertFalse(src.isEmpty());
        var linesOfCode = src.split("[\n|\r]");
        var nbloc = Arrays.stream(linesOfCode).filter(line -> line.length() > 0).count();
        assertTrue(nbloc >= 50);

        project.addJars("qualitas-corpus/input/antlr");
        project.addGraphMLs("qualitas-corpus/output/arcanOutput/antlr");
        var charLOC = new NumberOfLinesOfCode();
        var vSys = project.getVersion("3.2");
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