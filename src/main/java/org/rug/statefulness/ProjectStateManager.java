package org.rug.statefulness;

import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;

import java.io.*;
import java.nio.file.Paths;

public class ProjectStateManager {

    private final File lastVersion;

    public ProjectStateManager(String dir){
        this(new File(dir));
    }

    public ProjectStateManager(File dir){
        if (!dir.isDirectory()){
            throw new IllegalArgumentException("Project state directory must not be a file.");
        }
        if (!dir.exists()){
            dir.mkdirs();
        }
        this.lastVersion = Paths.get(dir.getAbsolutePath(), "version.seo").toFile();
    }

    public void saveState(IProject project) throws IOException {
       saveState(project, project.versions().last());
    }

    public void saveState(IProject project, IVersion lastVersion) throws IOException {
        var oos = new ObjectOutputStream(new FileOutputStream(this.lastVersion));
        oos.writeObject(lastVersion.getVersionString()); // alternatively we can only serialize the versionString.
        oos.writeObject(lastVersion.getVersionPosition());
        oos.flush();
        oos.close();
    }

    public void loadState(IProject instance) throws IOException, ClassNotFoundException {
        var ois = new ObjectInputStream(new FileInputStream(this.lastVersion));
        var lastVersionString = (String)ois.readObject();
        var lastVersionposition = (long)ois.readObject();
        ois.close();

        if (instance.getVersionedSystem().containsKey(lastVersionString)){
            instance.setVersionedSystem(instance.getVersionedSystem().tailMap(lastVersionString));
            for(var v : instance.getVersionedSystem().values()){
                v.setVersionPosition(lastVersionposition++);
            }
        }else {
            throw new IllegalStateException("Cannot load state for current project: last version string is not contained in the starting project.");
        }
    }
}
