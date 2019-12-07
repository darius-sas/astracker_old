package org.rug.web;

public class Smell {

    private final long id;
    private final String type;

    public Smell(long id, String type) {
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
