package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;

import java.util.List;

public abstract class AbstractSmellCharacteristic<R> implements ISmellCharacteristic<R>{
    private ArchitecturalSmell.Type targetType;
    private String name;


    protected AbstractSmellCharacteristic(ArchitecturalSmell.Type targetType, String name){
        this.targetType = targetType;
        this.name = name;
    }

    protected List<Vertex> getListOfSmells(Graph graph){
        return graph.traversal().V()
                .hasLabel(VertexLabel.SMELL.toString())
                .has("smellType", targetType.toString())
                .toList();
    }

}
