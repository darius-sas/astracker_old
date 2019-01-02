package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.VertexLabel;
import org.rug.data.smells.SmellType;

import java.util.List;

public abstract class AbstractSmellCharacteristic<R> implements ISmellCharacteristic<R>{
    private SmellType targetType;
    private String name;


    protected AbstractSmellCharacteristic(SmellType targetType, String name){
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
