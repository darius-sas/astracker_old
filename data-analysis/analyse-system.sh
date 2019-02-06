#!/bin/sh -x

TRACKAS_JAR="../target/trackas/trackas-0.1.jar"

usage() {
    echo "Usage:    analyse-system <project-name> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
    exit
}

trackas(){
    java -jar $TRACKAS_JAR $@
}

PROJECT=""
RUN_ARCAN=""
RUN_TRACKER=false
RECOMPILE_TRACKER=false
NON_CONSEC_VERS=""


while [ "$1" != "" ]; do
  case $1 in
    -p | --project )        shift
                            PROJECT=$1
                            ;;
    -rT | --run-tracker )   RUN_TRACKER=true
                            ;;
    -rA | --run-arcan )     RUN_ARCAN="-rA"
                            ;;
    -tC | --track-consec-only )  NON_CONSEC_VERS="-dNC"
                            ;;
    -c  | --recompile-tracker ) RECOMPILE_TRACKER=true
                            ;;
    -h | --help )           usage
                            exit
                            ;;
    * )                     usage
                            exit 1
  esac
  shift
done

INPUTDIR="../test-data/output/arcanOutput/$PROJECT/"
OUTPUTDIR="../test-data/output"

if [ $RUN_TRACKER = true ] ; then
    if [ $RECOMPILE_TRACKER = true ] ; then 
        oldir=$(pwd)
        cd ..
        echo "Compiling tracker..."
        mvn package > /dev/null
        if [ $? != 0 ] ; then
            echo "Build failed. Exiting..."
            exit
        else
            echo "Build successful."
        fi
        cd $oldir
    fi
    trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR -pC -pS $RUN_ARCAN $NON_CONSEC_VERS
fi

OUTPUTDIR=$OUTPUTDIR/trackASOutput/$PROJECT

if [ -z $NON_CONSEC_VERS ] ; then
    SUFFIX="nonConsec"
else
    SUFFIX="consecOnly"
fi

SIMILARITY_SCORES_FILE=$OUTPUTDIR/similarity-scores-$SUFFIX.csv
SMELL_CHARACTERISTICS_FILE=$OUTPUTDIR/smell-characteristics-$SUFFIX.csv

Rscript jaccard-linking.r $SIMILARITY_SCORES_FILE $OUTPUTDIR/smell-similarity-matrices-$SUFFIX.pdf