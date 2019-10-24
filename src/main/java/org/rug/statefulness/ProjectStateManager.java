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
       saveState(project.versions().last());
    }

    public void saveState(IVersion lastVersion) throws IOException {
        try(var oos = new ObjectOutputStream(new FileOutputStream(this.lastVersion))) {
            oos.writeObject(lastVersion.getVersionString()); // alternatively we can only serialize the versionString.
            oos.writeObject(lastVersion.getVersionPosition());
        }
    }

    public void loadState(IProject instance) throws IOException, ClassNotFoundException {
        String lastVersionString;
        long lastVersionposition;
        try(var ois = new ObjectInputStream(new FileInputStream(this.lastVersion))) {
            lastVersionString = (String) ois.readObject();
            lastVersionposition = (long) ois.readObject();
        }
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
