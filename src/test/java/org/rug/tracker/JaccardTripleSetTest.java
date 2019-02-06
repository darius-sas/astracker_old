package org.rug.tracker;

import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.rug.tracker.JaccardTripleSet.*;

import java.util.Set;

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

        JaccardTriple jt1 = new JaccardTriple(smell1.get(), smell2.get(), 0.5);

        assertTrue(set.add(jt1));

        JaccardTriple jt2 = new JaccardTriple(smell1.get(), smell3.get(), 0.5);

        assertNotEquals(jt1, jt2); // assert that the couple is not equal, but the set does not add it
        assertFalse(set.add(jt2)); // this is needed in order to be able to link smells uniquely

        JaccardTriple jt3 = new JaccardTriple(smell2.get(), smell1.get(), 0.5);

        assertNotEquals(jt3, jt1);
        assertNotEquals(jt3, jt2);

        assertTrue(set.add(jt3));

    }
}