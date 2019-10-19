package org.rug.statefulness;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;
import org.rug.tracker.ASmellTracker;

import java.io.*;
import java.nio.file.Paths;

import static org.rug.tracker.ASmellTracker.*;

public class ASmellTrackerStateManager {

    private File condensedGraph;
    private File trackGraph;
    private File trackerFile;

    public ASmellTrackerStateManager(String dir){
        this(new File(dir));
    }

    public ASmellTrackerStateManager(File dir){
        if (!dir.isDirectory()){
            throw new IllegalArgumentException("State directory argument must be a directory.");
        }
        if (!dir.exists()){
            dir.mkdirs();
        }
        this.condensedGraph = Paths.get(dir.getAbsolutePath(), "condensed.graphml").toFile();
        this.trackGraph = Paths.get(dir.getAbsolutePath(), "track.graphml").toFile();
        this.trackerFile = Paths.get(dir.getAbsolutePath(), "tracker.seo").toFile();
    }

    public void save(ASmellTracker tracker) throws IOException {
        var outStream = new ObjectOutputStream(new FileOutputStream(trackerFile));
        outStream.writeObject(tracker);
        tracker.getTrackGraph().traversal().V().properties(ASmellTracker.SMELL_OBJECT).drop().iterate();
        tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
        tracker.getTrackGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
        outStream.flush();
        outStream.close();
    }


    public ASmellTracker load(IProject project, IVersion version) throws IOException, ClassNotFoundException {
        var inpStream = new ObjectInputStream(new FileInputStream(trackerFile));
        ASmellTracker tracker = (ASmellTracker)inpStream.readObject();
        tracker.setCondensedGraph(TinkerGraph.open());
        tracker.getCondensedGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTrackGraph(TinkerGraph.open());
        tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTail(tracker.getTrackGraph().traversal().V().hasLabel(TAIL).next());

        var lastVersionSmellVertices = tracker.getTrackGraph().traversal().V().hasLabel(TAIL).out().has(VERSION, version.getVersionString()).toSet();
        var lastVersionSmells = project.getArchitecturalSmellsIn(version);

        assert lastVersionSmells.size() == lastVersionSmellVertices.size();

        for (var smell : lastVersionSmells){
            var smellVertex = lastVersionSmellVertices.stream().filter(v -> v.value(ASmellTracker.SMELL_ID).equals(smell.getId())).findFirst().get();
            smellVertex.property(ASmellTracker.SMELL_OBJECT, smell);
        }

        return tracker;
    }

}
