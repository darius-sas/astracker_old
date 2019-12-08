package org.rug.data.project;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.rug.data.characteristics.comps.SourceCodeRetriever;

import java.nio.file.Path;

public class GitVersion extends AbstractVersion {

    private transient Repository repository;
    private transient CheckoutCommand checkoutCommand;
    private String commitName;
    private boolean isCheckedOut;

    public GitVersion(Path path, Repository repository, CheckoutCommand checkoutCommand, SourceCodeRetriever sourceCodeRetriever){
        super(path, sourceCodeRetriever);
        this.checkoutCommand = checkoutCommand;
        this.repository = repository;
        this.isCheckedOut = false;
    }


    @Override
    public synchronized SourceCodeRetriever getSourceCodeRetriever() {
        if (!isCheckedOut) {
                checkoutCommand.setName(commitName);
                try {
                    checkoutCommand.call();
                    isCheckedOut = true;
                } catch (GitAPIException e) {
                    throw new IllegalArgumentException("Could not checkout the given commit: " + getVersionString());
                }
        }
        return super.getSourceCodeRetriever();
    }

    /**
     * Parses a file name (presumably of a GraphML file) that contains information
     * about the version in the following format:
     * {@code graph-#versionPosition-#day-#month-#year-#commitName.graphML}
     * Note that the file extension is not used.
     * @param f the path object to use for parsing the string version from the name.
     * @return the versionString of this version in the following format {@code #versionPosition-#commitName}.
     */
    @Override
    public String parseVersionString(Path f) {
        var fileName = f.getFileName().toString();
        int endIndex = f.toFile().isDirectory() ? fileName.length() : fileName.lastIndexOf('.');
        var splits = fileName.substring(0, endIndex).split("-");
        setVersionIndex(Long.parseLong(splits[1]));
        versionDate = String.join("-", splits[2].split("_"));
        commitName = splits[3];
        return String.join("-", String.valueOf(versionIndex), commitName);
    }

    /**
     * Retrieves the name of the commit represented by this version instance.
     * @return the SHA-1 commit id parsed during initialization.
     */
    public String getCommitName() {
        return commitName;
    }

    /**
     * Returns the current commit name as an object id.
     * @return an object id instance
     */
    public ObjectId getCommitObjectId(){
        return ObjectId.fromString(commitName);
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getVersionString(), getGraphMLPath().toString());
    }

}
