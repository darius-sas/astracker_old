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
    public GitProject(String name, String gitDir, Type projectType) throws IOException {
        super(name, projectType, new StringCommitComparator());
        this.git = Git.open(new File(gitDir));
    }

    @Override
    public void addSourceDirectory(String sourceMainDir) {
        var srcDirPath = new File(sourceMainDir).toPath();
        super.versionInitializer = (f) ->
                new GitVersion(f, git.getRepository(), git.checkout(),
                        projectType.getSourceCodeRetrieverInstance(srcDirPath));
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
