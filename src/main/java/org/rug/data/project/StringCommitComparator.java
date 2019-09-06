package org.rug.data.project;

import java.util.Comparator;

/**
 * A string comparator of commit version strings in the following format:
 * #versionPosition-#commitName
 */
public class StringCommitComparator implements Comparator<String> {
    @Override
    public int compare(String s, String t1) {
        long n1 = Long.parseLong(s.substring(0, s.lastIndexOf("-") - 1));
        long n2 = Long.parseLong(t1.substring(0, t1.lastIndexOf("-") - 1));
        return Long.compare(n1, n2);
    }

}
