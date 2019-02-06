package org.rug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void executeMainArcan(){
        Main.main(new String[]{"-p", "antlr", "-i", "./arcanrunner/inputs/antlr/", "-o", "test-outputs", "-rA"});
    }

    @Test
    void executeMain(){
        Main.main(new String[]{"-p", "antlr", "-i", "./test-data/output/arcanOutput/antlr", "-o", "test-data/output", "-pC", "-pS", "-dNC"});
    }
}