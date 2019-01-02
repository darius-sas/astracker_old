package org.rug.data.smells;

public enum SmellType {
    CD("cyclicDep"),
    UD("unstableDep"),
    HL("hublikeDep"),
    ICPD("ixpDep")
    ;

    private String value;

    SmellType(String value){
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
