package org.rug.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VersionComparatorTest {

    @Test
    void testCompareVersions(){
        var comparator = new VersionComparator();

        var v1 = "1.10.1";
        var v2 = "1.9.1";

        System.out.println(comparator.standardize(v1));
        System.out.println(comparator.standardize(v2));

        assertTrue(comparator.compare(v1, v2) > 0);
        assertTrue(v1.compareTo(v2) < 0);

        var list = new ArrayList<String>();
        list.add(v1);
        list.add(v2);
        list.sort(comparator);
        assertEquals(list, Arrays.asList(v2, v1));

        v1 = "1.10.1-RC2-final";
        v2 = "1.10.1";

        System.out.println(comparator.addOrderPrefixes(comparator.standardize(v1)));
        System.out.println(comparator.addOrderPrefixes(comparator.standardize(v2)));

        assertTrue(comparator.compare(v1, v2) < 0);

        var rngVersions = Arrays.asList("0.10.0", "0.10.1", "0.10.2", "0.10.3", "0.10.4", "0.10.5", "0.10.6",
                "0.7.2", "0.7.3", "0.7.4", "0.8.0", "0.8.1", "0.8.2", "0.8.3", "0.8.4", "0.9.0", "0.9.1", "0.9.2",
                "0.9.3", "0.9.4", "0.9.5", "0.10.7-final",
                "0.10.7", "0.3.0", "0.4.0", "0.5.0", "0.5.1", "0.5.2", "0.5.3", "0.6.0", "0.6.1", "0.7.0", "0.7.1", "0.10.7-rc1");

        rngVersions.sort(comparator);

        System.out.println(rngVersions);

        v1 = "2.1-final";
        v2 = "2.1-rc1";
        System.out.println(comparator.standardize(v1));
        System.out.println(comparator.standardize(v2));
        assertTrue(comparator.compare(v1, v2) > 0); // This should not fail, realease Candidates come before final versions

        rngVersions = Arrays.asList("5.10.0.0", "5.10.0.1", "5.10.2.0", "5.11.0.0", "5.11.0.1", "5.12.0.0", "5.12.0.1",
                "5.12.0.4", "5.12.1.0", "5.12.2.1", "5.13.0.0", "5.4.4", "5.5", "5.5.1", "5.6.2", "5.6.2.1", "5.6.3",
                "5.7", "5.7.1", "5.7.3", "5.7.3.1", "5.7.4", "5.7.4.1", "5.7.4.2", "5.7.4.3", "5.7.4.4", "5.7.4.5",
                "5.7.4.6", "5.7.4.7", "5.8.0.0", "5.8.1.1", "5.8.2.0", "5.8.2.1", "5.8.3.1", "5.9.0.0", "5.9.1.0",
                "5.9.2.0", "5.9.2.1");

        rngVersions.sort(comparator);

        System.out.println(rngVersions);

    }
}