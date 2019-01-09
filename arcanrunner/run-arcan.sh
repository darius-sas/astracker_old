
function arcan(){
    java -jar ./arcan/Arcan-1.4.0-SNAPSHOT.jar $@
}

function download_antlr(){
    VERSIONS=$1
    while read version
    do
        if [ -z $version ]
        then
            exit
        fi
            echo "Downloading version $version"
            outputdoc=inputs/antlr/antlr-$version.jar
            wget --output-document=$outputdoc http://central.maven.org/maven2/org/antlr/antlr/$version/antlr-$version.jar
            if [ $? -ne 0 ]; then
                wget -q --output-document=$outputdoc http://central.maven.org/maven2/antlr/antlr/$version/antlr-$version.jar
            fi  
    done < $VERSIONS
    echo "Deleting zero-size files"
    find inputs/antlr/ -size 0 -print0 |xargs -0 rm --
}

function run_arcan(){
    for filename in $1/*.jar; do 
        
        file=$(basename $filename)
        project=$(echo $file | cut -d '-' -f 1)
        version=$(echo $file | cut -d '-' -f 2)
        version=${version%.jar*} 
        
        outpath=outputs/$project/$project-$version/
        mkdir -p outpath
        
        echo Running arcan on $project-$version
        arcan -p $filename -jar -all -out $outpath/csv/ -neo4j -d $outpath/$project-neo4jdb &>/dev/null
        mv ToySystem-graph.graphml $outpath/$project-$version.graphml
    done
}