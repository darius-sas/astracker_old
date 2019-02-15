package org.rug;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void executeMainArcan(){
        Main.main(new String[]{"-p", "argouml", "-i", "./test-data/input/argouml/", "-o", "test-data/output/", "-rA", "-pC", "-pS", "-dNC"});
    }

    @Test
    void executeMain(){
        Main.main(new String[]{"-p", "argouml", "-i", "./test-data/output/arcanOutput/argouml", "-o", "test-data/output", "-pC", "-pS", "-dNC"});
    }
}