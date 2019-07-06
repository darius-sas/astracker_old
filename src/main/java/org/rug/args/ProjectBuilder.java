package org.rug.args;

import org.rug.data.project.IProject;
import org.rug.data.project.Project;
import org.rug.data.project.Version;
import org.rug.runners.ArcanRunner;
import org.rug.runners.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ProjectBuilder.class.getName());

    private IProject project;
    private Args args;

    public ProjectBuilder(Args args){
        this.args = args;
    }

    public IProject getProject() throws IOException{
        if (project == null){
            if (containsFilesOfType(".*\\.jar")) {
                Project p = new Project(args.projectName);
                List<ToolRunner> runners = new ArrayList<>();
                if (args.runArcan()) {
                    p.addJars(args.getHomeProjectDirectory());
                    var outputDir = args.getArcanOutDir();
                    p.forEach(version -> {
                        Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                        outputDirVers.toFile().mkdirs();
                        var arcan = new ArcanRunner(args.getArcanJarFile(), (Version)version, outputDirVers.toString(), p.isFolderOfFoldersOfJarsProject(), false);
                        arcan.setHomeDir(args.getHomeProjectDirectory());
                        arcan.inheritOutput(args.showArcanOutput);
                        runners.add(arcan);
                    });
                    args.adjustProjDirToArcanOutput();
                }
                p.addGraphMLs(args.getHomeProjectDirectory());

                project = p;
            }

        }
        if (project == null)
            logger.error("The given directory was not a valid project directory: {}", args.inputDirectory);
        return project;
    }

    private boolean containsFilesOfType(String typeRegex){
        boolean r;
         try{
             r = Files.walk(args.inputDirectory.toPath())
                      .anyMatch(f -> Files.isRegularFile(f) && f.getFileName().toString().matches(typeRegex));
         }catch (IOException e){
             r = false;
         }
         return r;
    }
}
