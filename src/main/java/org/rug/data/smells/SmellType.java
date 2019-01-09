package org.rug.data.smells;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum SmellType {
    CD("cyclicDep"),
    UD("unstableDep"),
    HL("hublikeDep"),
    ICPD("ixpDep"),
    MAS("multipleAS")
    ;

    private String value;

    SmellType(String value){
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SmellType getValueOf(String name){
        return lookup.get(name);
    }

    private static final Map<String, SmellType> lookup = new HashMap<>();

    static
    {
        for(SmellType type : SmellType.values())
        {
            lookup.put(type.value, type);
        }
    }

}
