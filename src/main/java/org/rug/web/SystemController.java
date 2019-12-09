package org.rug.web;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Defines the controller that manages the requests to retrieve data about a system.
 */
@RestController
@Scope("session")
public class SystemController {

    private final Logger logger = LoggerFactory.getLogger(SystemController.class);

    private static final int MAX_CACHED_SYSTEMS = 5;
    private final Map<String, System> cachedSystems = new LinkedHashMap<>();

    /**
     * Return a map of versions for this system. The keys represent the index of the version and the values
     * the actual "name" of the version.
     * @param system the name of the system analysed.
     * @return a map with the versions of this system.
     */
    @RequestMapping(value = "/versions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Long, String> versions(@RequestParam(value="system", defaultValue="antlr") String system) {
        return getSystem(system).getVersions();
    }

    /**
     * Returns a list of components (classes, packages, headers, etc.) of this system.
     * A range of starting and ending version index can be provided to limit the results only to components
     * that were present in the version indexes included in the given range.
     * @param system the name of the system of interest.
     * @param fromVersionIndex the starting version index that defines the range of this query. If negative, or not provided,
     *                         the system will limit the results to few versions back in time.
     * @param toVersionIndex the ending version index that defines the range of this query. If not provided, the latest version index is used.
     * @return a list of components.
     */
    @RequestMapping(value = "/components", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Component> components(@RequestParam(value="system", defaultValue="antlr") String system,
                                      @RequestParam(value="lastVersion", defaultValue = "true") boolean lastVersion,
                                      @RequestParam(value="start", defaultValue = "-1", required = false) long fromVersionIndex,
                                      @RequestParam(value="end", defaultValue = "4294967296", required = false) long toVersionIndex){
        var sys = getSystem(system);
        if (lastVersion){
            fromVersionIndex = sys.getVersions().lastKey();
            toVersionIndex = sys.getVersions().lastKey();
        }else if (fromVersionIndex < 0) {
            fromVersionIndex = sys.getRecentStartingIndex();
        }
        logger.debug("Using fromVersionIndex={}", fromVersionIndex);
        return sys.getComponents(fromVersionIndex, toVersionIndex);
    }

    /**
     * Returns a list of smells detected in the given system.
     * A range of starting and ending version index can be provided to limit the results only to smells
     * that were detected in the version indexes included in the given range.
     * @param system the name of the system of interest.
     * @param fromVersionIndex the starting version index that defines the range of this query. If negative, or not provided,
     *                         the system will limit the results to few versions back in time.
     * @param toVersionIndex the ending version index that defines the range of this query. If not provided, the latest version index is used.
     * @return a list of smells.
     */
    @RequestMapping(value = "/smells", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Smell> smells(@RequestParam(value="system", defaultValue="antlr") String system,
                              @RequestParam(value="lastVersion", defaultValue = "true") boolean lastVersion,
                              @RequestParam(value="start", defaultValue = "-1", required = false) long fromVersionIndex,
                              @RequestParam(value="end", defaultValue = "4294967296", required = false) long toVersionIndex){
        var sys = getSystem(system);
        if (lastVersion){
            fromVersionIndex = sys.getVersions().lastKey();
            toVersionIndex = sys.getVersions().lastKey();
        }else if (fromVersionIndex < 0) {
            fromVersionIndex = sys.getRecentStartingIndex();
        }
        logger.debug("Using fromVersionIndex={}", fromVersionIndex);
        return sys.getSmells(fromVersionIndex, toVersionIndex);
    }

    /**
     * Returns the complete System object with all the smells and components belonging to the requested system.
     * @param system the system name.
     * @return a System object containing all versions of all smells and components.
     */
    @RequestMapping(value = "/system", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public System system(@RequestParam(value="system", defaultValue="antlr") String system){
        return getSystem(system);
    }

    /**
     * Get the system object with the given name. In case the system has not been cached already, load it before returning it to the caller.
     * @param systemName the name of the system to return.
     * @return a System object containing the data of the system.
     */
    private System getSystem(String systemName){
        if (cachedSystems.size() > MAX_CACHED_SYSTEMS){
            cachedSystems.remove(cachedSystems.keySet().iterator().next());
        }
        if (!cachedSystems.containsKey(systemName)){
            var graphFile = String.format("./test-data/output/trackASOutput/%s/condensed-graph-consecOnly.graphml", systemName);
            var graph = TinkerGraph.open();
            graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
            cachedSystems.put(systemName, new System(graph));
            logger.debug("Successfully loaded {}", systemName);
        }
        return cachedSystems.get(systemName);
    }
}
