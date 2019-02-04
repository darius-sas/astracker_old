#!/usr/bin/env bash

TRACKAS_JAR="../target/trackas/trackas-0.1.jar"

usage() {
    echo "Usage:    analyse-system <project-name> [-rA]"
    exit
}

trackas(){
    java -jar $TRACKAS_JAR $@
}

PROJECT=$1
RUN_ARCAN=
RUN_TRACKER=false
NON_CONSEC_VERS=true

while [ "$1" != "" ]; do
  case $1 in
    -p | --project )        shift
                            PROJECT=$1
                            ;;
    -rT | --run-Tracker )   shift
                            RUN_TRACKER=true
                            ;;
    -rA | --run-Arcan )     shift
                            RUN_ARCAN=$1
                            ;;
    -tC | --track-consec-only )     shift
                                    NON_CONSEC_VERS=false
                            ;;
    -h | --help )           usage
                            exit
                            ;;
    * )                     usage
                            exit 1
  esac
  shift
done

INPUTDIR="../test-data/input/$PROJECT/"
OUTPUTDIR="../test-data/output"

if [ $RUN_TRACKER = true ] ; then
    ./trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR -pC -pS $RUN_ARCAN -trackNonConsec $NON_CONSEC_VERS
fi

OUTPUTDIR=$OUTPUTDIR/trackASOutput/$PROJECT

# here R scripts to run, make analyses, or combine files
if [ $NON_CONSEC_VERS = true ] ; then
    SUFFIX="nonConsec"
else
    SUFFIX="consecOnly"
fi

SIMILARITY_SCORES_FILE=$OUTPUTDIR/similarity-scores-$SUFFIX.csv
SMELL_CHARACTERISTICS_FILE=$OUTPUTDIR/smell-characteristics-$SUFFIX.csv

Rscript jaccard-linking.r $SIMILARITY_SCORES_FILE $OUTPUTDIR/smell-similarity-matrices-$SUFFIX.pdf