package org.rug.data.characteristics.comps;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;

import java.util.*;

/**
 * Retrieves the source code of a class starting starting from a list of Jar files.
 * The Jars are decompiled using CFR decompiler. CFR errors are fully suppressed and
 * empty string is always returned in such case.
 */
public class JarClassSourceCodeRetrieval extends ClassSourceCodeRetriever {

    private String[] classPath;
    private SourceClassSink sourceClasses = new SourceClassSink();
    private boolean errorOccured = false;

    /**
     * Build the instance with the JAR files containing the classes of the project
     * version to decompile.
     * @param jarFiles the full paths to the Jar files.
     */
    public JarClassSourceCodeRetrieval(String... jarFiles){
        this.classPath = jarFiles;
    }

    /**
     * Retrieves the source code as a string from the given class name.
     *
     * @param className the full name of the class without the .java suffix (e.g. org.package.Class).
     * @return the source code of the class as string. If the class is not present, or an error has occurred,
     * an empty string is returned.
     */
    @Override
    public String getClassSource(String className) {
        if (errorOccured)
            return "";
        if (sourceClasses.isEmpty())
            decompile();
        return sourceClasses.getSource(className, "");
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
     * Whether CFR decompiler has encountered an error.
     * @return true if an error has occurred, false otherwise.
     */
    public boolean hasErrorOccured() {
        return errorOccured;
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

        private Map<String, String> classes = new HashMap<>();

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

