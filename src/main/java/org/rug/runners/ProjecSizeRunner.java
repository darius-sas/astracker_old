package org.rug.runners;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IProject;
import org.rug.persistence.PersistenceHub;
import org.rug.persistence.ProjectSizeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    public int run() {
        int exitCode;
        if (project.versions().size() <= 0){
            logger.error("Cannot measure size of a project with no versions.");
            exitCode = -1;
        }else {
            project.forEach(version -> {
                logger.info("Measuring size of {} in version {}", project.getName(), version.getVersionString());
                var graph = version.getGraph();
                var nPlabels = VertexLabel.allComponents().stream().map(VertexLabel::toString).collect(Collectors.toSet());
                var nClabels = VertexLabel.allFiles().stream().map(VertexLabel::toString).collect(Collectors.toSet());
                var nP = graph.traversal().V().hasLabel(P.within(nPlabels)).count().tryNext().orElse(0L);
                var nC = graph.traversal().V().hasLabel(P.within(nClabels)).count().tryNext().orElse(0L);
                var record = new ArrayList<String>();
                record.add(project.getName());
                record.add(version.getVersionString());
                record.add(String.valueOf(version.getVersionPosition()));
                record.add(String.valueOf(nP));
                record.add(String.valueOf(nC));
                PersistenceHub.sendToAndWrite(ProjectSizeGenerator.class, record);
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
