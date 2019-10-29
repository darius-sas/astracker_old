package org.rug.statefulness;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;
import org.rug.tracker.ASmellTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;

import static org.rug.tracker.ASmellTracker.*;

public class ASmellTrackerStateManager {

    private final static Logger logger = LoggerFactory.getLogger(ASmellTrackerStateManager.class);

    private File condensedGraph;
    private File trackGraph;
    private File trackerFile;

    public ASmellTrackerStateManager(String dir){
        this(new File(dir));
    }

    public ASmellTrackerStateManager(File dir){
        if (!dir.exists()){
            dir.mkdirs();
        }
        if (!dir.isDirectory()){
            dir.delete();
            throw new IllegalArgumentException("Tracker state directory argument must be a directory.");
        }
        this.condensedGraph = Paths.get(dir.getAbsolutePath(), "condensed.graphml").toFile();
        this.trackGraph = Paths.get(dir.getAbsolutePath(), "track.graphml").toFile();
        this.trackerFile = Paths.get(dir.getAbsolutePath(), "tracker.seo").toFile();
    }

    public void saveState(ASmellTracker tracker) throws IOException {
        try(var outStream = new ObjectOutputStream(new FileOutputStream(trackerFile))) {
            outStream.writeObject(tracker);
            tracker.getTrackGraph().traversal().V().properties(ASmellTracker.SMELL_OBJECT).drop().iterate();
            tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
            tracker.getTrackGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
        }
    }


    public ASmellTracker loadState(IProject project, IVersion version) throws IOException, ClassNotFoundException {
        ASmellTracker tracker;
        try(var inpStream = new ObjectInputStream(new FileInputStream(trackerFile))) {
           tracker = (ASmellTracker) inpStream.readObject();
        }
        tracker.setCondensedGraph(TinkerGraph.open());
        tracker.getCondensedGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTrackGraph(TinkerGraph.open());
        tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTail(tracker.getTrackGraph().traversal().V().hasLabel(TAIL).next());

        var lastVersionSmellVertices = tracker.getTrackGraph().traversal().V().hasLabel(TAIL).out().has(VERSION, version.getVersionString()).toSet();
        var lastVersionSmells = project.getArchitecturalSmellsIn(version);

        assert lastVersionSmells.size() == lastVersionSmellVertices.size();

        for (var smell : lastVersionSmells){
            var smellVertex = lastVersionSmellVertices.stream()
                    .filter(v -> v.value(ASmellTracker.SMELL_ID).equals(smell.getId()))
                    .findFirst();
            if (smellVertex.isEmpty()){
                logger.error("Unable to find a match for smell with ID: {}", smell.getId());
                continue;
            }
            smellVertex.get().property(ASmellTracker.SMELL_OBJECT, smell);
        }

        return tracker;
    }

}
