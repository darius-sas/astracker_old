package org.rug.simpletests.data.project;

import org.junit.jupiter.api.Test;
import org.rug.data.project.StringVersionComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringVersionComparatorTest {

    @Test
    void testCompareVersions(){
        var comparator = new StringVersionComparator();

        BiFunction<String, String, String> msgSupplier = (s1, s2) -> String.format("Comparing strings %s", comparator.standardize(s1, s2));

        var v1 = "1.10.1";
        var v2 = "1.9.1";

        assertTrue(comparator.compare(v1, v2) > 0, msgSupplier.apply(v1, v2));
        assertTrue(v1.compareTo(v2) < 0);

        var list = new ArrayList<String>();
        list.add(v1);
        list.add(v2);
        list.sort(comparator);
        assertEquals(list, Arrays.asList(v2, v1));

        v1 = "1.10.1-RC2-final";
        v2 = "1.10.1";

        assertTrue(comparator.compare(v1, v2) < 0, msgSupplier.apply(v1, v2));

        v1 = "2.1-final";
        v2 = "2.1-rc1";

        assertTrue(comparator.compare(v1, v2) > 0, msgSupplier.apply(v1, v2));

        v1 = "1-final";
        v2 = "0.9-rc1";

        assertTrue(comparator.compare(v1, v2) > 0, msgSupplier.apply(v1, v2));

        var versions = Arrays.asList(
                "0.3.0", "0.4.0", "0.5.0", "0.5.1", "0.5.2", "0.5.3", "0.6.0", "0.6.1", "0.7.0", "0.7.1",
                "0.7.2", "0.7.3", "0.7.4", "0.8.0", "0.8.1", "0.8.2", "0.8.3", "0.8.4", "0.9.0", "0.9.1",
                "0.9.2", "0.9.3", "0.9.4", "0.9.5", "0.10.0", "0.10.1", "0.10.2", "0.10.3", "0.10.4", "0.10.5",
                "0.10.6", "0.10.7-rc1", "0.10.7-final", "0.10.7");
        
        var rngVersions = new ArrayList<>(versions);
        Collections.shuffle(rngVersions);
        rngVersions.sort(comparator);
        assertEquals(versions, rngVersions);

        versions = Arrays.asList(
                "5.4.4", "5.5", "5.5.1", "5.6.2", "5.6.2.1", "5.6.3", "5.7", "5.7.1", "5.7.3", "5.7.3.1", "5.7.4",
                "5.7.4.1", "5.7.4.2", "5.7.4.3", "5.7.4.4", "5.7.4.5", "5.7.4.6", "5.7.4.7", "5.8.0.0", "5.8.1.1", 
                "5.8.2.0", "5.8.2.1", "5.8.3.1", "5.9.0.0", "5.9.1.0", "5.9.2.0", "5.9.2.1", "5.10.0.0", "5.10.0.1", 
                "5.10.2.0", "5.11.0.0", "5.11.0.1", "5.12.0.0", "5.12.0.1", "5.12.0.4", "5.12.1.0", "5.12.2.1", "5.13.0.0");
        
        rngVersions = new ArrayList<>(versions);
        Collections.shuffle(rngVersions);
        rngVersions.sort(comparator);
        assertEquals(versions, rngVersions);

        versions = Arrays.asList("0.8.1", "1.0", "1.1", "2.0.0beta1", "2.0.0beta2", "2.0.0beta3", "2.0.0beta4",
                "2.0.0rc2", "2.0.0final", "2.0", "2.0.1", "2.0.2", "2.0.3", "2.1.0beta1", "2.1.0beta2", "2.1.0beta3",
                "2.1.0beta3b", "2.1.0beta4", "2.1.0beta5", "2.1.0beta6", "2.1.0rc1", "2.1.0final", "2.1.1", "2.1.2",
                "2.1.3", "2.1.4", "2.1.5", "2.1.6", "2.1.7", "2.1.8", "3.0.0alpha", "3.0.0beta1", "3.0.0beta2",
                "3.0.0beta3", "3.0.0beta4", "3.0.0rc1", "3.0.0", "3.0.1", "3.0.2", "3.0.3", "3.0.4", "3.0.5",
                "3.1.0alpha1", "3.1.0beta1", "3.1.0beta2", "3.1.0beta3", "3.1.0rc1", "3.1.0rc2", "3.1.0rc3", "3.1", "3.1.1",
                "3.1.2", "3.1.3", "3.2.0alpha1", "3.2.0alpha2", "3.2.0cr1", "3.2.0cr2", "3.2.0cr3", "3.2.0cr4",
                "3.2.0cr5", "3.2.0ga", "3.2.1ga", "3.2.2ga", "3.2.3ga", "3.2.4ga", "3.2.4sp1", "3.2.5ga", "3.2.6ga",
                "3.2.7ga", "3.3.0cr1", "3.3.0cr2", "3.3.0ga", "3.3.0sp1", "3.3.1ga", "3.3.2ga", "3.5.0beta1",
                "3.5.0beta2", "3.5.0beta3", "3.5.0beta4", "3.5.0cr1", "3.5.0cr2", "3.5.3final", "3.5.5final",
                "3.6.0beta1", "3.6.0beta2", "3.6.0beta3", "3.6.0beta4", "3.6.0", "3.6.1", "3.6.2", "3.6.3", "3.6.4",
                "3.6.5", "3.6.6", "3.6.7", "3.6.8", "3.6.9", "3.6.10", "4.0.0", "4.0.1", "4.1.0", "4.1.1", "4.1.2",
                "4.1.3", "4.1.4", "4.1.5", "4.1.6", "4.1.7", "4.1.8", "4.1.9", "4.1.10", "4.1.11", "4.1.12",
                "4.2.0", "4.2.1", "4.2.2");

        rngVersions = new ArrayList<>(versions);
        Collections.shuffle(rngVersions);
        rngVersions.sort(comparator);
        assertEquals(versions, rngVersions);
    }
}