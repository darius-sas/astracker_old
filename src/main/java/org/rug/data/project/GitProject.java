package org.rug.data.project;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

/**
 * Class that manages git-based projects.
 */
public class GitProject extends AbstractProject {

    private Git git;

    /**
     * Instantiates this project and sets the given name.
     *
     * @param name        the name of the project.
     * @param projectType the type of the project (programming language).
     */
    public GitProject(String name, Type projectType) {
        super(name, projectType, new StringCommitComparator());
    }

    @Override
    public void addSourceDirectory(String sourceMainDir) throws IOException {
        git = Git.open(new File(sourceMainDir));
        super.versionInitializer = (f) -> new GitVersion(f, git.checkout(), projectType.getSourceCodeRetrieverInstance(f));
    }


    @Override
    protected void initVersionPositions() {
        // Version positions are initialized by the git version
    }

    @Override
    public boolean isFolderOfFoldersOfSourcesProject() {
        return false;
    }



}
