package org.rug.runners;

import org.rug.data.project.Project;
import org.rug.data.labels.VertexLabel;
import org.rug.persistence.PersistenceWriter;
import org.rug.persistence.ProjectSizeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Calculates the sizes of a project in terms of classes and packages.
 */
public class ProjecSizeRunner extends ToolRunner {

    private final static Logger logger = LoggerFactory.getLogger(ProjecSizeRunner.class);

    public Project project;

    public ProjecSizeRunner(Project project) {
        super(null, null);
        this.project = project;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    public int start() {
        int exitCode;
        if (!project.hasGraphMLs()){
            logger.error("No graphML files in this project.");
            exitCode = -1;
        }else {
            project.getVersionedSystem().forEach((v, t) -> {
                logger.info("Measuring size of {} in version {}", project.getName(), v);
                var graph = t.getC();
                var nP = graph.traversal().V().hasLabel(VertexLabel.PACKAGE.toString()).count().tryNext().orElse(0L);
                var nC = graph.traversal().V().hasLabel(VertexLabel.CLASS.toString()).count().tryNext().orElse(0L);
                var record = new ArrayList<String>();
                record.add(project.getName());
                record.add(v);
                record.add(String.valueOf(nP));
                record.add(String.valueOf(nC));
                PersistenceWriter.sendTo(ProjectSizeGenerator.class, record);
            });
            logger.info("Completed.");
            exitCode = 0;
        }
        return exitCode;
    }

    @Override
    protected void postProcess(Process p) throws IOException {

    }
}
