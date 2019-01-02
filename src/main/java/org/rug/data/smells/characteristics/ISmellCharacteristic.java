package org.rug.data.smells.characteristics;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface ISmellCharacteristic <R> {
    R calculate(Graph sysGraph);
}
