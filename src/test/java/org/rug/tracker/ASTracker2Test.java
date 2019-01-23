package org.rug.tracker;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.rug.data.ArcanDependencyGraphParser;
import org.rug.data.Triple;
import org.rug.data.smells.ArchitecturalSmell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;


class ASTracker2Test {

    private final static Logger logger = LoggerFactory.getLogger(ASTracker2Test.class);

    @Test
    void trackTest(){
        SortedMap<String, Graph> versionedSystem = ArcanDependencyGraphParser.parseGraphML("./arcanrunner/outputs/antlr");

        ISimilarityLinker scorer = new JaccardSimilarityLinker();
        ASTracker2 tracker = new ASTracker2(scorer, true);
        versionedSystem.forEach( (version, graph) -> {
            logger.info("Tracking version {}", version);
            tracker.track(graph, version);
            //Analysis.recordScorer(tracker); //TODO
        });
        logger.info("Tracking completed. Generating simplified graph...");
        tracker.writeSimplifiedGraph("src/test/graphimages/simplified-trackgraph.graphml");
        tracker.writeTrackGraph("src/test/graphimages/trackgraph.graphml");

    }

    public static Set<Integer> duplicate(List<Triple<ArchitecturalSmell, ArchitecturalSmell, Double>> bestMatch){
        Function<List<Integer>, Set<Integer>> findDuplicates = (List<Integer> listContainingDuplicates) ->
        {
            Set<Integer> setToReturn = new HashSet<>();
            Set<Integer> set1 = new HashSet<>();

            for (Integer yourInt : listContainingDuplicates)
            {
                if (!set1.add(yourInt))
                {
                    setToReturn.add(yourInt);
                }
            }
            return setToReturn;
        };

        return findDuplicates.apply(bestMatch.stream().mapToInt(t -> (int)t.getA().getId()).boxed().collect(Collectors.toList()));
    }

}