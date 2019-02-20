package org.rug.data;

import java.util.Comparator;
import java.util.Hashtable;

public class VersionComparator implements Comparator<String> {

    private final static Hashtable<String, String> orderStringVersions = new Hashtable<>();

    @Override
    public int compare(String o1, String o2) {

        // make same length

        o1 = standardize(o1);
        o2 = standardize(o2);
        
        return o1.compareTo(o2);
    }

    public String standardize(String version){
        return standardize(version, 5, '0');
    }

    public String standardize(String version, int padLength, char padCharacter){
        var splits = version.split("\\.");

        for (int i = 0; i < splits.length - 1; i++){
            splits[i] = String.format("%" + padLength + "s", splits[i]).replace(' ', padCharacter);
        }
        splits[splits.length - 1] = addOrderPrefixes(splits[splits.length - 1]);

        return String.join(".", splits);
    }


    public String addOrderPrefixes(String version){
        var versionParts = version.split("\\.");
        var prefixedVersionBuilder = new StringBuilder();
        for(int i = 0; i < versionParts.length; i++){
            var p = versionParts[i];
            orderStringVersions.keySet().stream().forEach(k -> {
                prefixedVersionBuilder.append(p.replace(k, orderStringVersions.get(k)));
            });
            if (i == versionParts.length - 1){
                prefixedVersionBuilder.append("-z");
            } else {
                prefixedVersionBuilder.append(".");
            }
        }

        return prefixedVersionBuilder.toString();
    }

    static {
        orderStringVersions.put("rc", "arc");
    }

}
