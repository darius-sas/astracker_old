#!/bin/bash

fix_lucene(){
    versions=(4.0.0 4.1.0 4.2.0 4.2.1 4.3.0)

    for version in ${versions[@]}
    do
        java -jar /home/p284098/git/trackas/data-analysis/../arcan/Arcan-1.4.0-SNAPSHOT.jar -p /home/p284098/git/trackas/data-analysis/../qualitas-corpus/input/lucene/lucene-${version} -folderOfJars -CD -HL -UD -CM -PM -out /home/p284098/git/trackas/data-analysis/../qualitas-corpus/output/arcanOutput/lucene/${version}/csv 
        
        mv ./ToySystem-graph.graphml /home/p284098/git/trackas/data-analysis/../qualitas-corpus/output/arcanOutput/lucene/lucene-$version.graphml
    done
}

run_failed(){
    source ./analyse-system.sh
    
    projects=(azureus freecol hibernate lucene)

    for project in ${projects[@]}
    do
        analyse_single -p ${project} -i ../qualitas-corpus/output/arcanOutput -o ../qualitas-corpus/output -rT 

        if [ $? -ne 0 ] ; then
            echo "Tracking failed for project ${project}."
            return $?
        fi
    done
}

run_failed
