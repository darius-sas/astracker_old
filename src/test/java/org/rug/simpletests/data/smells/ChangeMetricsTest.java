package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.ChangeMetrics;
import org.rug.data.characteristics.comps.PCCCMetric;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.AbstractProject;
import org.rug.data.project.GitProject;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTests")
public class ChangeMetricsTest {

    @Test
    void testCalculate() throws IOException {
        // With this command line you can see the commits that modified a given file:
        // $ git log --first-parent --follow -- <file-name>
        GitProject pyne = new GitProject("pyne", "/home/fenn/git/pyne", AbstractProject.Type.JAVA);
        pyne.addSourceDirectory("/home/fenn/git/pyne");
        pyne.addGraphMLfiles("./test-data/output/arcanOutput/pyne");

        var pccc = new ChangeMetrics();

        pyne.forEach(pccc::calculate);

        var graph = pyne.getVersionWith(3).getGraph();

        assertTrue(graph.traversal().V().has("name",
                TextP.startingWith("edu.rug")).has(pccc.getName()).count().next() > 0);

        double pcccValue = graph.traversal().V()
                .has("name", "edu.rug.pyne.api.parser.analysisprocessor.ClassAnalysis")
                .next().value(PCCCMetric.NAME);
//        assertTrue(Math.abs(2/3d * 100 - pcccValue) < 1e-5);

        pcccValue = graph.traversal().V()
                .has("name", "edu.rug.pyne.api.parser.Parser")
                .next().value(PCCCMetric.NAME);

        assertTrue(Math.abs(1/3d * 100 - pcccValue) < 1e-5);

        graph.traversal()
                .V().hasLabel((P<String>) P.within(VertexLabel.allTypes().stream().map(VertexLabel::toString).collect(Collectors.toSet())))
                .has("name", TextP.startingWith("edu.rug"))
                .has(PCCCMetric.NAME)
                .toSet()
                .forEach(v -> System.out.println(String.format("%s -> %.2f", v.value("name"), v.<Double>value(PCCCMetric.NAME))));
    }
}
