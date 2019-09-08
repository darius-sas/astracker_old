package org.rug.data.project;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.rug.data.characteristics.comps.ClassSourceCodeRetriever;

import java.nio.file.Path;

public class GitVersion extends AbstractVersion {

    private CheckoutCommand checkoutCommand;
    private String versionDate;
    private String commitName;

    public GitVersion(Path path, CheckoutCommand checkoutCommand, ClassSourceCodeRetriever sourceCodeRetriever){
        super(path, sourceCodeRetriever);
        this.checkoutCommand = checkoutCommand;
    }


    @Override
    public ClassSourceCodeRetriever getSourceCodeRetriever() {
        checkoutCommand.setName(commitName);
        try{
            checkoutCommand.call();
        }catch (GitAPIException e){
            throw new IllegalArgumentException("Could not checkout the given commit: " + versionString);
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
        setVersionPosition(Long.parseLong(splits[1]));
        versionDate = String.join("-", splits[2]);
        commitName = splits[3];
        return String.join("-", String.valueOf(versionPosition), commitName);
    }

    /**
     * Returns the date string of this version.
     * @return a date in the format %dd-%mm-%yyyy.
     */
    public String getVersionDate() {
        return versionDate;
    }

    /**
     * Retrieves the name of the commit represented by this version instance.
     * @return the SHA-1 commit id parsed during initialization.
     */
    public String getCommitName() {
        return commitName;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", versionString, graphMLPath);
    }
}
