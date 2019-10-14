package org.rug.simpletests.runners;

import org.junit.jupiter.api.Test;
import org.rug.runners.ProjecSizeRunner;

import static org.rug.simpletests.TestData.antlr;

public class ProjectSizeRunnerTest {

    @Test
    void testProjectSize(){
        ProjecSizeRunner runner = new ProjecSizeRunner(antlr);
        runner.run();
    }

}
