package org.rug.simpletests.data.smells;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.PCCCMetric;
import org.rug.data.project.AbstractProject;
import org.rug.data.project.GitProject;

import java.io.IOException;

public class PCCCMetricTest {

    @Test
    void testCalculate() throws IOException {
        GitProject pyne = new GitProject("pyne", "/home/fenn/git/pyne", AbstractProject.Type.JAVA);
        pyne.addSourceDirectory("/home/fenn/git/pyne");
        pyne.addGraphMLfiles("./test-data/output/arcanOutput/pyne");

        var pccc = new PCCCMetric();

        pyne.forEach(version ->{
            var graph = version.getGraph();
            pccc.calculate(version);
            graph.traversal().V().values(pccc.getName()).hasNext();
        });
    }
}
