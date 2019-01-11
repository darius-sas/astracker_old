package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.rug.data.smells.characteristics.*;
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

    private Map<String, Double> characteristicsMap;

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
        this.characteristicsMap = new HashMap<>();
        setLevel(smell);
        setAffectedElements(smell);
        setSmellNodes(smell);
    }

    /**
     * Returns the id of the node representing this smell.
     * @return The long value representing the id of this smell in the Graph of the system.
     */
    public long getId() {
        return id;
    }

    /**
     * Get the vertices that describe this smell.
     * @return An unmodifiable set of vertices.
     */
    public Set<Vertex> getSmellNodes() {
        return Collections.unmodifiableSet(smellNodes);
    }

    /**
     * Set the smell nodes that describe this smell.
     * @param smellNodes the vertices that describe the nodes
     */
    protected void setSmellNodes(Set<Vertex> smellNodes) {
        this.smellNodes = smellNodes;
    }

    /**
     * Get the vertices that are affected by this smell.
     * @return an unmodifiable set of the affected elements.
     */
    public Set<Vertex> getAffectedElements() {
        return Collections.unmodifiableSet(affectedElements);
    }

    /**
     * Sets the affected elements of this smell.
     * @param affectedElements the elements affected by this smell.
     */
    protected void setAffectedElements(Set<Vertex> affectedElements) {
        this.affectedElements = affectedElements;
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     * @param smell the starting node
     */
    protected abstract void setAffectedElements(Vertex smell);

    /**
     * Sets the smell nodes that characterize this instance
     * @param smell the starting smell node. This will be mostly the only element in this set.
     */
    protected abstract void setSmellNodes(Vertex smell);

    /**
     * Triggers the calculation of each characteristic using the correct implementation of ICharacteristicsSet for the
     * current smell type. The results of the calculation are saved internally in a map
     * retrievable using <code>getCharacteristicsMap()</code>.
     */
    public void calculateCharacteristics(){
        Set<ISmellCharacteristic> characteristics = getCharacteristicsSet();
        for (ISmellCharacteristic s : characteristics){
            s.calculate(this);
            this.characteristicsMap.put(s.getName(), s.getValue());
        }
    }

    /**
     * Get the map of the currently computed characteristics.
     * @return an unmodifiable map containing the results of the characteristics.
     * The keys of the map are the name of the characteristics whereas the values is the computed value for that key.
     */
    public Map<String, Double> getCharacteristicsMap() {
        return characteristicsMap;
    }

    /**
     * Gets the type of components that this smell affects (i.e. class or package).
     * @return The type of components affected by the smell.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Set the type of components this smell affects.
     * @param level the level.
     */
    protected void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Gets the type of this smell.
     * @return The type of this smell.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the smell
     * @param type the type of the smell.
     */
    protected void setType(Type type) {
        this.type = type;
    }

    /**
     * Sets the level of this smell starting from the vertex describing it.
     * @param smell the vertex that describes it.
     */
    protected void setLevel(Vertex smell){
        setLevel(Level.fromString(smell.value("vertexType")));
    }

    /**
     * Returns the characteristics that will be computed for this smell.
     * @return an unmodifiable set of characteristics.
     */
    @SuppressWarnings("unchecked")
    private Set<ISmellCharacteristic> getCharacteristicsSet(){ return Collections.unmodifiableSet(getType().getCharacteristicsSet().getCharacteristicSet());}

    /**
     * Given the graph of a system, this methods builds a list of Architectural Smells that affect this system.
     * @param graph the graph of the system.
     * @return a list containing the parsed smells.
     */
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

    /**
     * Represents a type of AS and maps them to their instantiation and characteristics set.
     */
    public enum Type {
        CD("cyclicDep", CDSmell::new, new CDCharacteristicsSet()),
        UD("unstableDep", UDSmell::new, new UDCharacteristicsSet()),
        HL("hubLikeDep", HLSmell::new, new HLCharacteristicsSet()),
        ICPD("ixpDep", vertex -> null, null),
        MAS("multipleAS", vertex -> null, null)
        ;

        private String value;
        private Function<Vertex, ArchitecturalSmell> smellInstantiator;
        private ICharacteristicsSet characteristicsSet;

        Type(String value, Function<Vertex, ArchitecturalSmell> smellInstantiator, ICharacteristicsSet characteristicsSet){
            this.value = value;
            this.smellInstantiator = smellInstantiator;
            this.characteristicsSet = characteristicsSet;
        }

        public ArchitecturalSmell getInstance(Vertex vertex){
            return this.smellInstantiator.apply(vertex);
        }

        /**
         * Returns the ICharacteristicsSet instance of the current type.
         * @return the correct instance of ICharacteristicsSet that can be used to compute a smell's characteristics.
         */
        public ICharacteristicsSet getCharacteristicsSet() {
            return characteristicsSet;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Retrieves the Type of starting from the given string. A lookup table is used to enhance performance.
         * @param name The type of the smell as a string.
         * @return The type of this smell or null if the string is not present in the lookup table and in this enum.
         */
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

    /**
     * Describes the level at which a smell is detected: class or package.
     */
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
