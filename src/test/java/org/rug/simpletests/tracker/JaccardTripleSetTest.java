package org.rug.simpletests.tracker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.tracker.BestMatchSet;
import org.rug.tracker.JaccardSimilarityLinker;
import org.rug.tracker.JaccardTripleSet;
import org.rug.tracker.LinkScoreTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.rug.simpletests.TestData.antlr;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unitTests")
public class JaccardTripleSetTest {

    private final static Logger logger = LoggerFactory.getLogger(JaccardTripleSetTest.class);
    List<ArchitecturalSmell> smellsV1;
    List<ArchitecturalSmell> smellsV2;

    ArchitecturalSmell smell1;
    ArchitecturalSmell smell2;
    ArchitecturalSmell smell3;
    ArchitecturalSmell smell4;

    @BeforeAll
    void init() throws IOException{
        smellsV1 = antlr.getArchitecturalSmellsIn("2.7.2");
        smellsV2 = antlr.getArchitecturalSmellsIn("2.7.3");

        var smell1Opt = smellsV1.stream().filter(s -> s.getId() == 4157).findFirst();
        var smell2Opt = smellsV1.stream().filter(s -> s.getId() == 4274).findFirst();
        var smell3Opt = smellsV2.stream().filter(s -> s.getId() == 4557).findFirst();
        var smell4Opt = smellsV2.stream().filter(s -> s.getId() == 4710).findFirst();
        assertTrue(smell1Opt.isPresent());
        assertTrue(smell2Opt.isPresent());
        assertTrue(smell3Opt.isPresent());
        assertTrue(smell4Opt.isPresent());
        smell1 = smell1Opt.get();
        smell2 = smell2Opt.get();
        smell3 = smell3Opt.get();
        smell4 = smell4Opt.get();
        assertNotEquals(smell1, smell2);
        assertNotEquals(smell1, smell3);
    }

    @Test
    void add() {

        assertNotEquals(smell1, smell2);
        assertNotEquals(smell1.hashCode(), smell2.hashCode());
        assertNotEquals(smell1, smell3);
        assertNotEquals(smell1.hashCode(), smell3.hashCode());

        JaccardTripleSet set = new JaccardTripleSet();

        LinkScoreTriple jt1 = new LinkScoreTriple(smell1, smell2, 0.5);

        assertTrue(set.add(jt1));

        LinkScoreTriple jt2 = new LinkScoreTriple(smell1, smell3, 0.5);

        assertNotEquals(jt1, jt2); // assert that the couple is not equal, but the set does not add it
        assertFalse(set.add(jt2)); // this is needed in order to be able to link smells uniquely

        LinkScoreTriple jt3 = new LinkScoreTriple(smell2, smell1, 0.5);

        assertNotEquals(jt3, jt1);
        assertNotEquals(jt3, jt2);

        assertTrue(set.add(jt3));

    }

    @Test
    void testBestMatchSet() {

        var jt1 = new LinkScoreTriple(smell1, smell2, 0.8);
        var jt2 = new LinkScoreTriple(smell1, smell3, 0.9);

        var jset = new BestMatchSet();

        assertTrue(jset.add(jt1));
        assertTrue(jset.add(jt2));
        assertEquals(1, jset.size());

        var jt3 = new LinkScoreTriple(smell3, smell1, 0.99);

        assertTrue(jset.add(jt3));
        assertEquals(2, jset.size());

        var jt4 = new LinkScoreTriple(smell1, smell1, 0.8);
        assertFalse(jset.add(jt4));
        assertEquals(2, jset.size());
    }

    @Test
    void testBestMatchSet2() {

        var scorer = new JaccardSimilarityLinker();
        var jt1 = new LinkScoreTriple(smell1, smell3, scorer.calculateJaccardSimilarity(smell1, smell3));
        var jt2 = new LinkScoreTriple(smell2, smell3, scorer.calculateJaccardSimilarity(smell2, smell3));

        assertTrue(jt1.compareTo(jt2) > 0);
        assertTrue(jt2.compareTo(jt1) < 0);

        var jset = new BestMatchSet();

        assertTrue(jset.add(jt2));
        assertTrue(jset.add(jt1));
        assertEquals(1, jset.size());
        assertSame(jt1, jset.iterator().next());

        var jt3 = new LinkScoreTriple(smell2, smell4, scorer.calculateJaccardSimilarity(smell2, smell4));

        assertTrue(jset.add(jt3));
        assertEquals(2, jset.size());
    }
}