package org.rug.data.characteristics.comps;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieves the source code of a class starting starting from a list of Jar files.
 * The Jars are decompiled using CFR decompiler. CFR errors are fully suppressed and
 * empty string is always returned in such case.
 *
 * NOTE: {@link #setClassPath(String[])} needs to be explicitly invoked as it
 * specifies the paths where this object will look for classes.
 *
 * @author Jasper Mohlmann
 * @author Darius Sas
 */
public class JarSourceCodeRetriever extends SourceCodeRetriever {

    private String[] classPath;
    private SourceClassSink sourceClasses;
    private boolean errorOccured = false;

    public JarSourceCodeRetriever(Path classPathDir) {
        super(classPathDir);
        sourceClasses = new SourceClassSink(classesCache);
        setClassPath(sourcePath);
    }

    /**
     * Retrieves the source code as a string from the given class name.
     *
     * @param className the full name of the class without the .java suffix (e.g. org.package.Class).
     * @return the source code of the class as string. If the class is not present, or an error has occurred,
     * an empty string is returned.
     */
    @Override
    public String getSource(String className) {
        if (errorOccured || classPath == null)
            return NOT_FOUND;
        if (sourceClasses.isEmpty())
            decompile();
        return sourceClasses.getSource(className, NOT_FOUND);
    }

    @Override
    protected String toFileName(Vertex element) {
        return element.value("name");
    }

    @Override
    public Optional<Path> getPathOf(Vertex component) {
        throw new UnsupportedOperationException("Cannot retrieve the path of an element when using JAR as sources.");
    }

    @Override
    public Optional<Path> getPathOf(String elementName) {
        throw new UnsupportedOperationException("Cannot retrieve the path of an element when using JAR as sources.");
    }

    /**
     * This method sets the jar files where the classes will be looked for.
     * The method clears the currently cached decompiled classes automatically.
     * @param classPath an array of paths to JAR files.
     */
    private void setClassPath(String[] classPath) {
        clear();
        this.classPath = classPath;
    }

    /**
     * @see #setClassPath(String[])
     * @param classPath a list of paths to JAR files.
     */
    private void setClassPath(List<String> classPath){
        setClassPath(classPath.toArray(new String[classPath.size()]));
    }

    /**
     * @see #setClassPath(String[])
     * @param directory The directory that contains the JAR files. If directory is a file, it is
     *                  assumed it is a JAR file.
     */
    private void setClassPath(Path directory){
        if (directory.toFile().isDirectory()) {
            try (var walk = Files.walk(directory)) {
                var jars = walk.filter(Files::isRegularFile)
                        .map(f -> f.toAbsolutePath().toString())
                        .filter(f -> f.endsWith(".jar"))
                        .collect(Collectors.toList());
                setClassPath(jars);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else
            setClassPath(directory.toFile().getAbsolutePath());
    }


    /**
     * @see #setClassPath(String[])
     * @param file A JAR file that contains the classes.
     */
    private void setClassPath(String file){
        setClassPath(new String[]{file});
    }

    /**
     * Decompile all the classes and cache their source.
     */
    private void decompile(){
        try {
            CfrDriver driver = new CfrDriver.Builder().withOutputSink(sourceClasses).build();
            driver.analyse(Arrays.asList(classPath));
        }catch (Exception e){
            errorOccured = true;
        }
    }


    /**
     * Explicitly free the memory of the current source code classes.
     */
    public void clear(){
        sourceClasses.clear();
    }

    /**
     * Class dedicated to store decompiled classes using CFR decompiler.
     */
    protected static class SourceClassSink implements OutputSinkFactory {

        private Map<String, String> classes;

        SourceClassSink(Map<String, String> classes) {
            this.classes = classes;
        }

        @Override
        public List<OutputSinkFactory.SinkClass> getSupportedSinks(OutputSinkFactory.SinkType
        sinkType, Collection< OutputSinkFactory.SinkClass > collection) {

            if (sinkType == OutputSinkFactory.SinkType.JAVA && collection.contains(OutputSinkFactory.SinkClass.DECOMPILED)) {
                return Arrays.asList(OutputSinkFactory.SinkClass.DECOMPILED, OutputSinkFactory.SinkClass.STRING);
            } else {
                return Collections.singletonList(OutputSinkFactory.SinkClass.STRING);
            }
        }

        @Override
        public <T> Sink<T> getSink(OutputSinkFactory.SinkType sinkType, OutputSinkFactory.SinkClass sinkClass) {
            if (sinkType == OutputSinkFactory.SinkType.JAVA && sinkClass == OutputSinkFactory.SinkClass.DECOMPILED) {
                return x -> {
                    SinkReturns.Decompiled d = (SinkReturns.Decompiled) x;
                    classes.put(String.join(".", d.getPackageName(), d.getClassName()), d.getJava());
                };
            }
            return ignore -> {
            };
        }

        public boolean isEmpty(){
            return classes.isEmpty();
        }

        public boolean contains(String className){
            return classes.containsKey(className);
        }

        public String getSource(String className, String defaultString){
            return classes.getOrDefault(className, defaultString);
        }

        public void clear(){
            classes.clear();
        }
    }
}

