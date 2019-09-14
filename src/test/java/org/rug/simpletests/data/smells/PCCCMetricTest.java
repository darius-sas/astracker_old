package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.P;
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

        pyne.forEach(pccc::calculate);

        var version = pyne.getVersionWith(3);
        version.getGraph().traversal()
                .V().hasLabel(P.within("class"))
                .has(pccc.getName())
                .toSet()
                .forEach(v -> System.out.println(String.format("%s -> %s", v.value("name"), v.value(pccc.getName()))));
        pccc.getName();
        //TODO we can find the vertex the inner class belongs to and update that one instead
        //FIXME fix bug "could not retrieve changes" for certain files
    }
}
