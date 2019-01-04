package org.rug.data;

import java.util.EnumSet;

public enum EdgeLabel {
    PARTOFCYCLE("partOfCycle"),
    DEPENDSON("dependsOn"),
    PARTOFSTAR("partOfStar"),
    ISCENTREOFSTAR("isCentreOfStar"),
    ISTINYSHAPED("isTinyShaped"),
    ISPARTOFCHAIN("isPartOfChain"),
    ISCIRCLESHAPED("isCircleShaped"),
    ISCLIQUESHAPED("isCliqueShaped"),
    HLAFFECTED("HLaffected"),
    UDAFFECTED("UDaffected"),
    HLIN("isHLin"),
    HLOUT("isHLout"),
    UDBADDEP("UDbadDep")
    ;

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
