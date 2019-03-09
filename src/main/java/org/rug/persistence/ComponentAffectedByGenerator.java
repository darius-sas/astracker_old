package org.rug.persistence;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.tracker.ASmellTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates a file containing the affected elements for each smell.
 */
public class ComponentAffectedByGenerator extends CSVDataGenerator<ASmellTracker> {

    private List<String> header;


    public ComponentAffectedByGenerator(String outputFile) {
        super(outputFile);
        this.header = new ArrayList<>();
    }

    @Override
    public String[] getHeader() {
        return header.toArray(new String[]{});
    }

    @Override
    public void accept(ASmellTracker object) {
        GraphTraversalSource g = object.getCondensedGraph().traversal();
        header.addAll(Arrays.asList("name", "type", "version", "affectedBy"));

        g.V().hasLabel(ASmellTracker.COMPONENT).forEachRemaining(c ->{
            var edges = c.edges(Direction.IN, ASmellTracker.AFFECTS);
            edges.forEachRemaining(edge -> {
                var record = new ArrayList<String>();
                record.add(c.value(ASmellTracker.NAME).toString());
                record.add(c.value(ASmellTracker.COMPONENT_TYPE).toString());
                record.add(edge.value(ASmellTracker.VERSION).toString());
                record.add(edge.outVertex().value(ASmellTracker.UNIQUE_SMELL_ID).toString());
                records.add(record);
            });
        });
    }
}
