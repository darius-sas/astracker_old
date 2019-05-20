package org.rug.tracker;

import org.junit.jupiter.api.Test;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class JaccardTripleSetTest {

    private final static Logger logger = LoggerFactory.getLogger(JaccardTripleSetTest.class);

    @Test
    void add() {
        var systems = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr/");

        var smellsV1 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.5"));
        var smellsV2 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.6"));

        var smell1 = smellsV1.stream().filter(s -> s.getId() == 315).findFirst();
        var smell2 = smellsV2.stream().filter(s -> s.getId() == 325).findFirst();
        var smell3 = smellsV2.stream().filter(s -> s.getId() == 315).findFirst();

        assertTrue(smell1.isPresent());
        assertTrue(smell2.isPresent());
        assertTrue(smell3.isPresent());

        logger.info("Smell 1:\n{}", smell1.get());
        logger.info("Smell 2:\n{}", smell2.get());
        logger.info("Smell 3:\n{}", smell3.get());

        assertNotEquals(smell1.get(), smell2.get());
        assertNotEquals(smell1.get().hashCode(), smell2.get().hashCode());
        assertNotEquals(smell1.get(), smell3.get());
        assertNotEquals(smell1.get().hashCode(), smell3.get().hashCode());

        JaccardTripleSet set = new JaccardTripleSet();

        LinkScoreTriple jt1 = new LinkScoreTriple(smell1.get(), smell2.get(), 0.5);

        assertTrue(set.add(jt1));

        LinkScoreTriple jt2 = new LinkScoreTriple(smell1.get(), smell3.get(), 0.5);

        assertNotEquals(jt1, jt2); // assert that the couple is not equal, but the set does not add it
        assertFalse(set.add(jt2)); // this is needed in order to be able to link smells uniquely

        LinkScoreTriple jt3 = new LinkScoreTriple(smell2.get(), smell1.get(), 0.5);

        assertNotEquals(jt3, jt1);
        assertNotEquals(jt3, jt2);

        assertTrue(set.add(jt3));

    }

    @Test
    void testBestMatchSet(){
        var systems = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr/");

        var smellsV1 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.5"));
        var smellsV2 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.6"));

        var smell1 = smellsV1.stream().filter(s -> s.getId() == 315).findFirst();
        var smell2 = smellsV2.stream().filter(s -> s.getId() == 325).findFirst();
        var smell3 = smellsV2.stream().filter(s -> s.getId() == 315).findFirst();

        var jt1 = new LinkScoreTriple(smell1.get(), smell2.get(), 0.8);
        var jt2 = new LinkScoreTriple(smell1.get(), smell3.get(), 0.9);

        var jset = new BestMatchSet();

        assertTrue(jset.add(jt1));
        assertTrue(jset.add(jt2));
        assertEquals(1, jset.size());

        var jt3 = new LinkScoreTriple(smell3.get(), smell1.get(), 0.99);

        assertTrue(jset.add(jt3));
        assertEquals(2, jset.size());

        var jt4 = new LinkScoreTriple(smell1.get(), smell1.get(), 0.8);
        assertFalse(jset.add(jt4));
        assertEquals(2, jset.size());
    }

    @Test
    void testBestMatchSet2(){
        var systems = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr/");

        var smellsV1 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.2"));
        var smellsV2 = ArcanDependencyGraphParser.getArchitecturalSmellsIn(systems.get("2.7.5"));

        var smell1 = smellsV1.stream().filter(s -> s.getId() == 276).findFirst();
        var smell2 = smellsV1.stream().filter(s -> s.getId() == 277).findFirst();
        var smell3 = smellsV2.stream().filter(s -> s.getId() == 313).findFirst();

        assertNotEquals(smell1.get(), smell2.get());
        assertNotEquals(smell1.get(), smell3.get());

        var scorer = new JaccardSimilarityLinker();
        var jt1 = new LinkScoreTriple(smell1.get(), smell3.get(), scorer.calculateJaccardSimilarity(smell1.get(), smell3.get()));
        var jt2 = new LinkScoreTriple(smell2.get(), smell3.get(), scorer.calculateJaccardSimilarity(smell2.get(), smell3.get()));

        assertEquals(jt1, jt2);
        assertTrue(jt1.compareTo(jt2) > 0);
        assertTrue(jt2.compareTo(jt1) < 0);

        var jset = new BestMatchSet();

        assertTrue(jset.add(jt2));
        assertTrue(jset.add(jt1));
        assertEquals(1, jset.size());
        assertSame(jt1, jset.iterator().next());

        var smell4 = smellsV2.stream().filter(s -> s.getId() == 314).findFirst();
        var jt3 = new LinkScoreTriple(smell2.get(), smell4.get(), scorer.calculateJaccardSimilarity(smell2.get(), smell4.get()));

        assertTrue(jset.add(jt3));
        assertEquals(2, jset.size());
    }
}