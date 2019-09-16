package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.PCCCMetric;
import org.rug.data.project.AbstractProject;
import org.rug.data.project.GitProject;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class PCCCMetricTest {

    @Test
    void testCalculate() throws IOException {
        // With this command line you can see the commits that modified a given file:
        // $ git log --first-parent --follow -- <file-name>
        GitProject pyne = new GitProject("pyne", "/home/fenn/git/pyne", AbstractProject.Type.JAVA);
        pyne.addSourceDirectory("/home/fenn/git/pyne");
        pyne.addGraphMLfiles("./test-data/output/arcanOutput/pyne");

        var pccc = new PCCCMetric();

        pyne.forEach(pccc::calculate);

        var graph = pyne.getVersionWith(3).getGraph();

        assertTrue(graph.traversal().V().has("name",
                TextP.startingWith("edu.rug")).has(pccc.getName()).count().next() > 0);

        double pcccValue = graph.traversal().V()
                .has("name", "edu.rug.pyne.api.parser.analysisprocessor.ClassAnalysis")
                .next().value(pccc.getName());
        assertTrue(Math.abs(2/3d * 100 - pcccValue) < 1e-5);
        graph.traversal()
                .V().hasLabel(P.within("class"))
                .has("name", TextP.startingWith("edu.rug"))
                .has(pccc.getName())
                .toSet()
                .forEach(v -> System.out.println(String.format("%s -> %.2f", v.value("name"), v.value(pccc.getName()))));

        //todo edu.rug.pyne.api.parser.Parser has been added after first commit and has changed at least once
        // but it is still computed as 0% percentage
    }
}
