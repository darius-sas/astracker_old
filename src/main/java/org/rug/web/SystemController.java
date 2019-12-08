package org.rug.web;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@Scope("session")
public class SystemController {
    private static final int MAX_CACHED_SYSTEMS = 5;
    private final Map<String, System> cachedSystems = new LinkedHashMap<>();

    @RequestMapping(value = "/versions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Long, String> versions(@RequestParam(value="system", defaultValue="antlr") String system) {
        return getSystem(system).getVersions();
    }

    @RequestMapping(value = "/components", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Component> components(@RequestParam(value="system", defaultValue="antlr") String system,
                                      @RequestParam(value="start", defaultValue = "1") long fromVersionIndex,
                                      @RequestParam(value="end", defaultValue = "9223372036854775806") long toVersionIndex){
        return getSystem(system).getComponents(fromVersionIndex, toVersionIndex);
    }

    @RequestMapping(value = "/smells", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Smell> smells(@RequestParam(value="system", defaultValue="antlr") String system,
                              @RequestParam(value="start", defaultValue = "1") long fromVersionIndex,
                              @RequestParam(value="end", defaultValue = "9223372036854775806") long toVersionIndex){
        return getSystem(system).getSmells(fromVersionIndex, toVersionIndex);
    }

    private System getSystem(String systemName){
        if (cachedSystems.size() > MAX_CACHED_SYSTEMS){
            cachedSystems.remove(cachedSystems.keySet().iterator().next());
        }
        if (!cachedSystems.containsKey(systemName)){
            var graphFile = String.format("./test-data/output/trackASOutput/%s/condensed-graph-consecOnly.graphml", systemName);
            var graph = TinkerGraph.open();
            graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
            cachedSystems.put(systemName, new System(graph));
        }
        return cachedSystems.get(systemName);
    }
}
