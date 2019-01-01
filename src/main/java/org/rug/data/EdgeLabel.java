package org.rug.data;

import java.util.EnumSet;

public enum EdgeLabel {
    PARTOFCYCLE("partOfCycle"),
    DEPENDSON("dependsOn"),
    PARTOFSTAR("partOfStar"),
    ISCENTREOF("isCentreOf");

    private final String value;

    EdgeLabel(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
