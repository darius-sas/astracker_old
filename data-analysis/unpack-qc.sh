#!/bin/bash

unpack_systems(){
    systems=$1
    echo "Installing systems: $systems"
    ./install.pl -r -s $systems 
    IFS=','
    read -ra SYS <<< "$systems"
    for name in "${SYS[@]}"; do
        echo "Removing misc files and unused sources of $name" 
        for folder in ../Systems/$name/*/
        do
            folder=${folder%*/} 
            rm -rf $folder/src/
        done
    done
}

groups=("ant,antlr,argouml,azureus", "eclipse_SDK,freecol,freemind", "hibernate,jgraph,jmeter,jstock", "jung,junit,lucene,weka")

for group in ${groups[@]}
do
    unpack_systems $group
done