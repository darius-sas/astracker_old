package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
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

    @Test
    void testCDParsing2(){
        var graph = TinkerGraph.open();
        graph.traversal().io("/home/fenn/Downloads/gitRepos/output/arcanOutput/CoffeeConTroll/graph-16-12_6_2016-620236ec438d3c92360146a41dca1f46f459a7b4.graphml").read().with(IO.reader, IO.graphml).iterate();
        g = graph.traversal();
        testParsing(ArchitecturalSmell.Type.CD, CDSmellCPP::new);
    }

}
