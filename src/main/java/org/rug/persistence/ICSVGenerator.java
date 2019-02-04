package org.rug.persistence;

public interface ICSVGenerator<T> extends IDataGenerator<T>{

    /**
     * Returns the header of the underlying data.
     * @return a array containing the headers.
     */
    String[] getHeader();

}
