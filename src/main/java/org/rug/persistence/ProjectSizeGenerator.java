package org.rug.persistence;

import java.util.List;

public class ProjectSizeGenerator extends CSVDataGenerator<List<String>> {

    public ProjectSizeGenerator(String outputFile) {
        super(outputFile);
    }

    /**
     * Returns the header of the underlying data.
     *
     * @return a array containing the headers.
     */
    @Override
    public String[] getHeader() {
        return new String[]{"project", "version", "versionDate", "versionIndex", "nPackages", "nClasses"};
    }

    /**
     * Accepts an object to serialize into an output file.
     *
     * @param object the object to serialize into an output file.
     */
    @Override
    public void accept(List<String> object) {
        records.add(object);
    }
}
