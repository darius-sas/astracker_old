package org.rug.simpletests.data.project;

import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rug.simpletests.TestData.antlr;
import static org.rug.simpletests.TestData.pure;

public class VersionTest {

    @Test
    void testRetrievedElementRemoval(){
        var version = antlr.getVersionWith(3);
        var nClassRetrieved = version.getGraph().traversal().V().has("ClassType", "RetrievedClass").count().tryNext().orElse(0L);
        assertEquals(0L, (long)nClassRetrieved);
        var nPackageRetrieved = version.getGraph().traversal().V().has("PackageType", "RetrievedPackage").count().tryNext().orElse(0L);
        assertEquals(0L, (long)nPackageRetrieved);

        version = pure.getVersionWith(2);
        var nRetrieved = version.getGraph().traversal().V().has("Type", TextP.containing("retrieved")).count().tryNext().orElse(0L);
        assertEquals(0, (long)nRetrieved);
    }

}
