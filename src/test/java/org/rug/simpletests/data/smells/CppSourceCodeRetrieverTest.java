package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.CppSourceCodeRetriever;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTests")
public class CppSourceCodeRetrieverTest {
    @Test
    void testCppRetriever(){
        var retriever = new CppSourceCodeRetriever(new File("./test-data/git-projects/pure/src/").toPath());
        var source = retriever.getSource("netbase", ".cpp");
        assertFalse(source.isEmpty());
        assertNotEquals("", source);

        var source2 = retriever.getSource("netbase", ".h");
        assertFalse(source2.isEmpty());
        assertNotEquals("", source2);

        assertTrue(source.length() > source2.length());

        var source3 = retriever.getSource("miner", ".cpp");
        assertFalse(source3.isEmpty());
        assertNotEquals("", source3);

        assertEquals("", retriever.getSource("nonExistingFile", ".h"));
    }
}
