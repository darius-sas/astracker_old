package org.rug.simpletests.data.smells;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.comps.JavaSourceCodeRetriever;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTests")
public class JavaSourceCodeRetrieverTest {

    @Test
    void testGetClassName(){
        var retriever = new JavaSourceCodeRetriever(Paths.get("src", "main", "java"));
        var source = retriever.getClassSource("org.rug.data.characteristics.comps.JavaSourceCodeRetriever");
        assertNotNull(source);
        assertNotEquals("", source);
        System.out.println(source);
    }

}
