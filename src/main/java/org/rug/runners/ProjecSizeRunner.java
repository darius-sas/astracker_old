package org.rug.runners;

import org.rug.data.project.IProject;
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

    public IProject project;

    public ProjecSizeRunner(IProject project) {
        super(null, null);
        this.project = project;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    public int start() {
        int exitCode;
        if (project.versions().size() <= 0){
            logger.error("Cannot analyse a project of size 0.");
            exitCode = -1;
        }else {
            project.forEach(version -> {
                logger.info("Measuring size of {} in version {}", project.getName(), version.getVersionString());
                var graph = version.getGraph();
                var nP = graph.traversal().V().hasLabel(VertexLabel.PACKAGE.toString()).count().tryNext().orElse(0L);
                var nC = graph.traversal().V().hasLabel(VertexLabel.CLASS.toString()).count().tryNext().orElse(0L);
                var record = new ArrayList<String>();
                record.add(project.getName());
                record.add(version.getVersionString());
                record.add(String.valueOf(version.getVersionPosition()));
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
