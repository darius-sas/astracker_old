package org.rug.data.project;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Compares versions that match the following regex (\d\.)+\d\w*, namely dot-separated versions with
 * eventual word descriptors at the end.
 * The supported word descritors are, at the moment: "rc", "final" and every descriptor which lexicographical
 * order respects release order.
 */
public class StringVersionComparator implements Comparator<String> {

    private final static Hashtable<String, String> orderStringVersions = new Hashtable<>();
    private final String sep = "\\.";
    private final String dot = ".";

    @Override
    public int compare(String o1, String o2) {

        var standardForm = standardize(o1, o2, "0");

        o1 = standardForm[0];
        o2 = standardForm[1];

        return o1.compareTo(o2);
    }

    /**
     * Convenience method for {@link #standardize(String, String, String)} where fillString = "0".
     * @param s1 A version to standardize
     * @param s2 A version to standardize.
     * @return Same as {@link #standardize(String, String, String)}, but as a List of strings.
     */
    public List<String> standardize(String s1, String s2){
        return Arrays.asList(standardize(s1, s2, "0"));
    }

    /**
     * Conforms the two strings to the same format by adding missing trailing versions and filling them
     * with {@code fillString}. Additionally, every split (version figures) are padded using
     * {@link #standardize(String)}, excluding the last split.
     * This method also takes care to add order prefixes to the last split.
     * @param s1 A dot-separated version.
     * @param s2 A dot-separated version.
     * @param fillString a string to use for padding, should be "0" to guarantee lexicographical order correctness.
     * @return an array where the first element is {@code s1} and the second element is {@code s2} standardized
     * as described above.
     */
    public String[] standardize(String s1, String s2, String fillString){
        var split1 = s1.toLowerCase().replace("-", "").split(sep);
        var split2 = s2.toLowerCase().replace("-", "").split(sep);

        var splitsToAdd = split1.length - split2.length;
        if (splitsToAdd != 0) {
            var shorterSplit = new ArrayList<>(Arrays.asList(split1.length < split2.length ? split1 : split2));
            IntStream.range(0, Math.abs(splitsToAdd)).forEach(i -> shorterSplit.add(fillString));
            if (splitsToAdd < 0)
                split1 = shorterSplit.toArray(new String[0]);
            else
                split2 = shorterSplit.toArray(new String[0]);
        }

        var regex = ".*[a-zA-Z]+.*";
        var split1Matches = split1[split1.length - 1].matches(regex);
        var split2Matches = split2[split2.length - 1].matches(regex);

        if (split1Matches && !split2Matches)
            split2[split1.length - 1] = split2[split2.length - 1] + "z";
        else if (!split1Matches && split2Matches)
            split1[split1.length - 1] = split1[split1.length - 1] + "z";
        else if (split1Matches && split2Matches) {
            split1[split1.length - 1] = addOrderPrefixes(split1[split1.length - 1]);
            split2[split2.length - 1] = addOrderPrefixes(split2[split2.length - 1]);
        }else{
            split1[split1.length - 1] = String.format("%" + 5 + "s", split1[split1.length - 1]).replace(' ', '0');
            split2[split2.length - 1] = String.format("%" + 5 + "s", split2[split2.length - 1]).replace(' ', '0');
        }

        return new String[]{standardize(String.join(dot, split1)),
                            standardize(String.join(dot, split2))};
    }

    /**
     * Convenience method for {@link #standardize(String[], int, char)} with {@code padLength = 5}
     * and {@code padCharacter = '0'}.
     * @param version A version to pad.
     * @return the padded dot-separated version.
     */
    public String standardize(String version){
        return standardize(version.split(sep), 5, '0');
    }

    /**
     * Pads the given version.
     * @param splits the version in the form of splits.
     * @param padLength the length of the padding.
     * @param padCharacter the character to use for padding.
     * @return a dot-separated version padded.
     */
    public String standardize(String[] splits, int padLength, char padCharacter){
        for (int i = 0; i < splits.length - 1; i++){
            splits[i] = String.format("%" + padLength + "s", splits[i]).replace(' ', padCharacter);
        }

        return String.join(".", splits);
    }

    /**
     * Replaces all occurrences of descriptors that are non lexicographically-ordered with a lexicographically-ordered
     * version of them.
     * @param split the split of the version that contains the character to replace.
     * @return {@code split} with ordered descriptors.
     */
    private String addOrderPrefixes(String split){
        var prefixedVersionBuilder = new StringBuilder();
        orderStringVersions.forEach((k, v) -> {
            prefixedVersionBuilder.append(split.replace(k, v));
        });
        return prefixedVersionBuilder.toString();
    }

    static {
        orderStringVersions.put("rc", "arc");
        orderStringVersions.put("final", "zfinal");
    }

}
