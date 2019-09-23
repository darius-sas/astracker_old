package org.rug.persistence;

import java.io.File;

public interface IDataGenerator<T> {

    /**
     * Accepts an object to serialize into an output file.
     * @param object the object to serialize into an output file.
     */
    void accept(T object);

    /**
     * Returns the file where to write the records of this generator.
     * @return a file.
     */
    File getOutputFile();

    /**
     * Write data generated with the accept method on file.
     */
    void writeOnFile();

    /**
     * Closes the underlying data streams for writing on files.
     */
    void close();
}
