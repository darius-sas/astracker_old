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

while [ "$1" != "" ]; do
  case $1 in
    -p | --project )        shift
                            PROJECT=$1
                            ;;
    -rA | --run-Arcan )     shift
                            RUN_ARCAN=$1
                            ;;
    -h | --help )           usage
                            exit
                            ;;
    * )                     usage
                            exit 1
  esac
  shift
done

./trackas -p $PROJECT -i ../test-data/input/$PROJECT/ -o ../test-data/output/ -pC -pS $RUN_ARCAN

# here R scripts to run, make analyses, or combine files