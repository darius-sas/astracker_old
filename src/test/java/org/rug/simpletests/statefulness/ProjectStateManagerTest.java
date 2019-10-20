package org.rug.simpletests.statefulness;

import org.junit.jupiter.api.Test;
import org.rug.data.project.IProject;
import org.rug.statefulness.ProjectStateManager;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rug.simpletests.TestData.antlr;

public class ProjectStateManagerTest {

    @Test
    void testStateManagerAntlr() throws IOException, ClassNotFoundException {
        testStateSaveAndLoad(antlr);
    }

    void testStateSaveAndLoad(IProject project) throws IOException, ClassNotFoundException {
        var lastVersion = project.versions().last();

        var stateManger = new ProjectStateManager("test-data/output/states");
        stateManger.saveState(project);

        stateManger.loadState(project);

        assertEquals(lastVersion, project.versions().first());
    }
}
