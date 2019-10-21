package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.smells.PageRank;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.rug.simpletests.TestData.antlr;
import static org.rug.simpletests.TestData.pure;

public class PageRankTest {
    @Test
    void testPageRankCalculate(){
        var version = antlr.getVersionWith(2);
        var smells = antlr.getArchitecturalSmellsIn(version);
        PageRank pr = new PageRank();
        smells.forEach(smell ->{
            var pageRank = Double.parseDouble(smell.accept(pr));
            assertTrue(pageRank > 0);
        });

        version = pure.getVersionWith(2);
        smells = pure.getArchitecturalSmellsIn(version);
        smells.forEach(smell -> {
            var pageRank = Double.parseDouble(smell.accept(pr));
            assertTrue(pageRank > 0);
        });
    }
}
