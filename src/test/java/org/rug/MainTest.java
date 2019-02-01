package org.rug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void executeMainArcan(){
        Main.main(new String[]{"-i", "./arcanrunner/inputs/antlr/", "-o", "test-antlr", "-rA"});
    }

    @Test
    void executeMain(){
        Main.main(new String[]{"-i", "./test-antlr/arcanOutput/antlr", "-o", "test-antlr"});
    }
}