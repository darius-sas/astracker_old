package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmellCPP;
import org.rug.data.smells.HLSmellCPP;
import org.rug.data.smells.UDSmellCPP;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.rug.simpletests.TestData.pure;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArchitecturalSmellCppParsingTest {

    GraphTraversalSource g;

    @BeforeAll
    void init(){
        g = pure.getVersion("1.0.0.0").getGraph().traversal();
    }

    @Test
    void testCDParsing(){
        testParsing(ArchitecturalSmell.Type.CD, CDSmellCPP::new);
    }

    @Test
    void testUDParsing(){
        testParsing(ArchitecturalSmell.Type.UD, UDSmellCPP::new);
    }

    @Test
    void testHLParsing(){
        testParsing(ArchitecturalSmell.Type.HL, HLSmellCPP::new);
    }

    private void testParsing(ArchitecturalSmell.Type type, Function<Vertex, ArchitecturalSmell> instantiator){
        var cds = g.V().hasLabel("smell").has("smellType", type.toString()).toSet();
        cds.forEach(smell -> {
            var s = instantiator.apply(smell);
            assertNotEquals(0, s.getAffectedElements().size());
            assertEquals(type, s.getType());
            switch (s.getLevel()){
                case CFILE:
                    assertEquals("CFile", smell.value("vertexType").toString());
                    break;
                case COMPONENT:
                    assertEquals("component", smell.value("vertexType"));
                    break;
                default:
                    fail("Level is incorrect. Test failed.");
            }
        });
    }

}
