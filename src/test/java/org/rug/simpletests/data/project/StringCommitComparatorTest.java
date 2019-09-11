package org.rug.simpletests.data.project;

import org.junit.jupiter.api.Test;
import org.rug.data.project.StringCommitComparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringCommitComparatorTest {

    @Test
    void testCompareTo(){
        var s1 = "1-16e03e9ea1d416c8f3cd3ab79273245ce631ac92";
        var s2 = "003-16e03e9ea1d416c8f3cd3ab79273245ce631ac92";

        var comparator = new StringCommitComparator();

        assertEquals(-1, comparator.compare(s1, s2));
        assertEquals(1, comparator.compare(s2, s1));

        s1 = "000010000-a0723c8a3cefc206a7ccb15d9de1e2bf399fcff9";
        s2 = "1000-a0723c8a3cefc206a7ccb15d9de1e2bf399fcff9";

        assertEquals(1, comparator.compare(s1, s2));
        assertEquals(-1, comparator.compare(s2, s1));
    }
}
