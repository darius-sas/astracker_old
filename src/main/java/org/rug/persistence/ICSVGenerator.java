package org.rug.persistence;

import java.util.List;

public interface ICSVGenerator<T> extends IDataGenerator<T>, Iterable<List<String>>{

    /**
     * Returns the header of the underlying data.
     * @return a array containing the headers.
     */
    String[] getHeader();

}
