package org.rug.simpletests.statefulness;

import org.junit.jupiter.api.Test;
import org.rug.data.project.IProject;
import org.rug.data.project.Project;
import org.rug.statefulness.ProjectStateManager;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectStateManagerTest {

    @Test
    void testStateManagerAntlr() throws IOException, ClassNotFoundException {
        Project antlr = new Project("antlr", Project.Type.JAVA);
        antlr.addSourceDirectory("./test-data/input/antlr");
        antlr.addGraphMLfiles("./test-data/output/arcanOutput/antlr");
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
