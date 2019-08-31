package org.rug.simpletests.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.Test;
import org.rug.data.characteristics.smells.PageRank;
import org.rug.data.project.ArcanDependencyGraphParser;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.ArchitecturalSmell.Type;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.CDSmellCPP;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ArcanDependencyGraphParserTest {

    @Test
    void parseGraphML() {
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./test-data/output/arcanOutput/antlr");

		List<String> versions = Arrays.asList("2.7.1", "2.7.2", "2.7.5", "2.7.6", "2.7.7", "3.0", "3.0.1", "3.1",
				"3.1.1", "3.1.2", "3.1.3", "3.2", "3.3", "3.4", "3.5");

		assertTrue(versionedSystem.keySet().containsAll(versions));

	}


	void parseGraphMLCpp() {
		Graph gr = TinkerGraph.open();
		gr.traversal().io(".\\arcanCppOutput\\pure\\pure-1.0.0.0.graphml").read().with(IO.reader, IO.graphml).iterate();

		List<ArchitecturalSmell> smells = ArcanDependencyGraphParser.getArchitecturalSmellsIn(gr);
		ArchitecturalSmell smell = smells.stream().filter(as -> as.getId() == 13363).findFirst().get();
		Graph smellGraph = smell.getAffectedGraph();
		
		PageRank pr = new PageRank();
		String pRank = pr.visit((CDSmell) smell);
		System.out.println(pRank);
		
    	assertEquals(smells.size(), 11);

    	ArchitecturalSmell s1 = smells.stream().filter(as -> as.getId() == 9409).findFirst().get();
    	assertNotEquals(s1, null);
    	assertEquals(s1.getType(), Type.UDCPP);

    	ArchitecturalSmell s2 = smells.stream().filter(as -> as.getId() == 9421).findFirst().get();
    	assertNotEquals(s2, null);
    	assertEquals(s2.getType(), Type.UDCPP);

    	ArchitecturalSmell s3 = smells.stream().filter(as -> as.getId() == 9311).findFirst().get();
    	assertNotEquals(s3, null);
    	assertEquals(s3.getType(), Type.CDCPP);
    	assertEquals(((CDSmellCPP)s3).getShape(), CDSmell.Shape.UNCLASSIFIED);

    	ArchitecturalSmell s4 = smells.stream().filter(as -> as.getId() == 9326).findFirst().get();
    	assertNotEquals(s4, null);
    	assertEquals(s4.getType(), Type.CDCPP);
    	assertEquals(((CDSmellCPP)s4).getShape(), CDSmell.Shape.UNCLASSIFIED);

    	assertNotEquals(s1, s2);
    	assertNotEquals(s1, s3);
	}
}