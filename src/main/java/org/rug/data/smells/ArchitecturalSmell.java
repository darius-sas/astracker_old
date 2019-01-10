package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Abstraction of an AS. A smell is composed by the nodes that represent the smell (label
 * <code>VertexLabel.SMELL</code>), and the nodes that are affected by the smell (label
 * <code>VertexLabel.PACKAGE || VertexLabel.CLASS</code>.
 */
public abstract class ArchitecturalSmell {

    private final static Logger logger = LoggerFactory.getLogger(ArchitecturalSmell.class);

    private long id;
    private Set<Vertex> smellNodes;
    private Set<Vertex> affectedElements;

    private Type type;
    private Level level;


    /**
     * Initializes this smell instance starting from the smell node
     * @param smell the smell that characterizes this instance.
     */
    protected ArchitecturalSmell(Vertex smell, Type type){
        assert smell.label().equals(VertexLabel.SMELL.toString());
        this.id = Long.parseLong(smell.id().toString());
        this.type = type;
        setLevel(smell);
        setAffectedElements(smell);
        setSmellNodes(smell);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Vertex> getSmellNodes() {
        return smellNodes;
    }

    public void setSmellNodes(Set<Vertex> smellNodes) {
        this.smellNodes = smellNodes;
    }

    public Set<Vertex> getAffectedElements() {
        return affectedElements;
    }

    public void setAffectedElements(Set<Vertex> affectedElements) {
        this.affectedElements = affectedElements;
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     * @param smell the starting node
     */
    public abstract void setAffectedElements(Vertex smell);

    /**
     * Sets the smell nodes that characterize this instance
     * @param smell the starting smell node. This will be mostly the only element in this set.
     */
    public abstract void setSmellNodes(Vertex smell);

    @Override
    public int hashCode() {
        return (int)id+smellNodes.hashCode()+ type.hashCode();
    }

    @SuppressWarnings("unchecked")
    public static List<ArchitecturalSmell> getArchitecturalSmellsIn(Graph graph){
        List<ArchitecturalSmell> architecturalSmells = new ArrayList<>();
        graph.traversal().V().hasLabel(VertexLabel.SMELL.toString()).toList()
        .forEach(smellVertex -> {
            String smellTypeProperty = smellVertex.value("smellType");
            if (smellTypeProperty != null) {
                Type smellType = Type.fromString(smellTypeProperty);
                ArchitecturalSmell as = smellType.getInstance(smellVertex);
                if (as != null)
                    architecturalSmells.add(as);
                else
                    logger.warn("AS type '{}' was ignored since no implementation exists for it.", smellVertex.value("smellType").toString());
            }else {
                logger.warn("No 'smellType' property found for smell vertex {}.", smellVertex);
            }
        });
        return architecturalSmells;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    protected void setLevel(Vertex smell){
        setLevel(Level.fromString(smell.value("vertexType")));
    }

    /**
     * Represents a type of AS
     */
    public enum Type {
        CD("cyclicDep", CDSmell::new),
        UD("unstableDep", UDSmell::new),
        HL("hubLikeDep", HLSmell::new),
        ICPD("ixpDep", vertex -> null),
        MAS("multipleAS", vertex -> null)
        ;

        private String value;
        private Function<Vertex, ArchitecturalSmell> smellInstantiator;

        Type(String value, Function<Vertex, ArchitecturalSmell> smellInstantiator){
            this.value = value;
            this.smellInstantiator = smellInstantiator;
        }

        public ArchitecturalSmell getInstance(Vertex vertex){
            return this.smellInstantiator.apply(vertex);
        }

        @Override
        public String toString() {
            return value;
        }

        public static Type fromString(String name){
            return lookup.get(name);
        }

        private static final Map<String, Type> lookup = new HashMap<>();

        static
        {
            for(Type type : Type.values())
            {
                lookup.put(type.value, type);
            }
        }

    }

    public enum Level {
        CLASS("class"),
        PACKAGE("package");

        private final String level;

        Level(String level){
            this.level = level;
        }

        public static Level fromString(String name){
            return lookup.get(name);
        }

        private static final Map<String, Level> lookup = new HashMap<>();

        static
        {
            for(Level type : Level.values())
            {
                lookup.put(type.level, type);
            }
        }

        @Override
        public String toString() {
            return level;
        }
    }
}
