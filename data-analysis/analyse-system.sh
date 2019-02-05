#!/bin/sh -x

TRACKAS_JAR="../target/trackas/trackas-0.1.jar"

usage() {
    echo "Usage:    analyse-system <project-name> [-rA|-run-Arcan][-rT|-run-Tracker][-tC|-track-consec-only][-h|--help]"
    exit
}

trackas(){
    java -jar $TRACKAS_JAR $@
}

PROJECT=""
RUN_ARCAN=""
RUN_TRACKER=false
NON_CONSEC_VERS=""

while [ "$1" != "" ]; do
  case $1 in
    -p | --project )        shift
                            PROJECT=$1
                            ;;
    -rT | --run-Tracker )   RUN_TRACKER=true
                            ;;
    -rA | --run-Arcan )     RUN_ARCAN="-rA"
                            ;;
    -tC | --track-consec-only )  NON_CONSEC_VERS="-dNC"
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