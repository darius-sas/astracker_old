package org.rug.data.project;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.rug.data.characteristics.comps.ClassSourceCodeRetriever;

import java.nio.file.Path;

public class GitVersion extends AbstractVersion {

    private CheckoutCommand checkoutCommand;

    public GitVersion(Path path, CheckoutCommand checkoutCommand, ClassSourceCodeRetriever sourceCodeRetriever){
        super(path, sourceCodeRetriever);
        this.checkoutCommand = checkoutCommand;
    }


    @Override
    public ClassSourceCodeRetriever getSourceCodeRetriever() {
        checkoutCommand.setName(versionString);
        try{
            checkoutCommand.call();
        }catch (GitAPIException e){
            throw new IllegalArgumentException("Could not checkout the given commit: " + versionString);
        }

        return super.getSourceCodeRetriever();
    }
}
