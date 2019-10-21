#!/bin/bash

ARCAN_DIR="/home/fenn/git/arcan"
ASTRACKER_DIR="/home/fenn/git/astracker"
WEEKS_INTERVAL_GIT_COMMITS=2

astracker(){
    java "${ASTRACKER_DIR}/target/astracker-0.8-jar-with-dependencies.jar" $@
}

arcan(){
    java -jar "${ASTRACKER_DIR}/arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar" $@
}

compile_arcan(){
    wd=$(pwd)
    cd $ARCAN_DIR
    mvn clean compile package -DskipTests
    unzip $ARCAN_DIR/target/Arcan-1.4.0-SNAPSHOT.zip -d $ASTRACKER_DIR/arcan
    cd $wd
}

compile_astracker(){
    wd=$(pwd)
    cd $ASTRACKER_DIR
    mvn clean compile assembly:single -DskipTests
    cd $wd
}

compile(){
    compile_arcan || { echo "Failed to compile Arcan."; exit; }
    compile_astracker || { echo "Failed to compile ASTracker."; exit; }
}

usage(){
    echo "atdan [args]"
    echo "Arguments:"
    echo " -p | --projectName   The name of the project to analyse."
    echo " -i | --inputDir      The directory of the project (git repository)."
    echo " -o | --outputDir     The directory to use to write the output."
    echo " -c | --compile       Whether to compile Arcan and ASTracker."
}

run(){
    parse_args $@
    if [[ -z $COMPILE ]]; then
        compile 
    fi

    OUTDIR_ARCAN=$OUTPUTDIR/arcanOutput/$PROJECT
    mkdir -p $OUTDIR_ARCAN
    
    arcan -p $INPUTDIR -out $OUTPUTDIR/ -git -nWeeks $WEEKS_INTERVAL_GIT_COMMITS || { echo "Failed Arcan analysis."; exit; }
    astracker -p $PROJECT -i $OUTDIR_ARCAN -o $OUTPUTDIR -pC -rS -gitRepo $INPUTDIR || { echo "Failed ASTracker analysis."; exit; }
}
parse_args(){
    while [[ $# -gt 0 ]]; do

        arg=$1
        case $arg in 
            -p | --projectName)
            PROJECT=$2
            shift
            shift
            ;;

            -i | --inputDir)
            INPUTDIR=$2
            shift
            shift
            ;;

            -o | --outputDir)
            OUTPUTDIR=$2
            shift
            shift
            ;;

            -c | --compile)
            COMPILE=true
            shift
            ;;

            *)
            usage
            exit
            ;;

        esac

    done
}

run
