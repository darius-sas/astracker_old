package org.rug.data.project;

import org.eclipse.jgit.api.Git;
import org.rug.data.characteristics.comps.JavaClassSourceCodeRetriever;

import java.io.File;
import java.io.IOException;

/**
 * Class that manages git-based projects.
 */
public class GitProject extends AbstractProject {

    private Git gitRepo;

    /**
     * Instantiates this project and sets the given name.
     *
     * @param name        the name of the project.
     * @param projectType
     */
    public GitProject(String name, Type projectType) {
        super(name, projectType, new StringCommitComparator());
    }

    @Override
    public void addSourceDirectory(String sourceMainDir) throws IOException {
        gitRepo = Git.open(new File(sourceMainDir));
        super.versionInitializer = (f) -> new GitVersion(f, gitRepo.checkout(), new JavaClassSourceCodeRetriever());
    }

    @Override
    public boolean isFolderOfFoldersOfSourcesProject() {
        return false;
    }

}
