package org.rug.data.labels;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum EdgeLabel {
    PARTOFCYCLE("partOfCycle"),
    DEPENDSON("dependsOn"),
    BELONGSTO("belongsTo"),
    ISIMPLEMENTATIONOF("isImplementationOf"),
    ISCHILDOF("isChildOf"),
    ISAFFERENTOF("isAfferentOf"),
    ISEFFERENTOF("isEfferentOf"),
    PACKAGEISAFFERENTOF("packageIsAfferentOf"),
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
    STARTOFCYCLE("startOfCycle"),
    GCAFFECTEDPACKAGE("GCAffectedPackage"),
    ;

    private final static EnumSet<EdgeLabel> dependency = EnumSet.of(DEPENDSON, PACKAGEISAFFERENTOF);
    private final static Set<String> dependencyStrings = dependency.stream().map(EdgeLabel::toString).collect(Collectors.toSet());

    public final static EnumSet<EdgeLabel> allDependencyEdges(){
        return dependency;
    }

    public static Set<String> getAllDependencyStrings() {
        return dependencyStrings;
    }

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
