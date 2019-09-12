package org.rug.data.labels;

public enum EdgeLabel {
    PARTOFCYCLE("partOfCycle"),
    DEPENDSON("dependsOn"),
    BELONGSTO("belongsTo"),
    ISIMPLEMENTATIONOF("isImplementationOf"),
    ISCHILDOF("isChildOf"),
    ISAFFERENTOF("isAfferentOf"),
    ISEFFERENTOF("isEfferentOf"),
    PACKAGEISAFFERENTOF("packageIsAfferentOf"),
    AFFERENT("afferent"),
    PARTOFSTAR("partOfStar"),
    ISCENTREOFSTAR("isCentreOfStar"),
    ISTINYSHAPED("isTinyShaped"),
    ISPARTOFCHAIN("isPartOfChain"),
    ISCIRCLESHAPED("isCircleShaped"),
    ISCLIQUESHAPED("isCliqueShaped"),
    HLAFFECTEDPACK("HLAffectedPackage"),
    HLAFFECTEDCLASS("HLAffectedClass"),
    UDAFFECTED("UDAffectedPackage"),
    HLIN("isHLin"),
    HLOUT("isHLout"),
    UDBADDEP("badDep"),
    STARTOFCYCLE("startOfCycle")
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
