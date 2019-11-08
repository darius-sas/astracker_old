#!/bin/bash

ARCAN_DIR="/home/fenn/git/arcan"
ASTRACKER_DIR="/home/fenn/git/astracker"
ARCAN_JAR="${ASTRACKER_DIR}/arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar"
ASTRACKER_JAR="${ASTRACKER_DIR}/target/astracker-0.9.0-jar-with-dependencies.jar"
WEEKS_INTERVAL_GIT_COMMITS=2

astracker(){
    java -jar ${ASTRACKER_JAR} $@
}

arcan(){
    java -jar ${ARCAN_JAR} $@
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
    echo " -p  | --projectName    The name of the project to analyse."
    echo " -i  | --inputDir       The directory of the project (git repository)."
    echo "                        Use this as input parameter to analyse one project only."
    echo " -f  | --projectsFolder The directory containing multiple projects."
    echo "                        Use this as input parameter (ignore -i) if you want to analyse" 
    echo "                        multiple projects at once."
    echo " -o  | --outputDir      The directory to use to write the output."
    echo " -c  | --compile        Whether to compile Arcan and ASTracker."
    echo " -rT | --runTracker     Run AStracker. By default ASTracker is not executed."
    echo " -rA | --runArcan       Run Arcan. By default Arcan is not executed."
	echo ""
	echo "Please ensure you set up the variables for Arcan and AStracker directories"
	echo "at the beginning of this script file."
}


run(){
    parse_args $@
    run_no_parse
}

run_no_parse(){
    OUTDIR_ARCAN=$OUTPUTDIR/arcanOutput/$PROJECT
    mkdir -p $OUTDIR_ARCAN
    
    if [[ ! -z $RUNARCAN ]]; then
        arcan -p $INPUTDIR -out $OUTPUTDIR/ -git -nWeeks $WEEKS_INTERVAL_GIT_COMMITS || { echo "Failed Arcan analysis."; exit; }
    else
        OUTDIR_ARCAN=$INPUTDIR
    fi

    if [[ ! -z $RUNTRACKER ]]; then
        astracker -p $PROJECT -i $OUTDIR_ARCAN -o $OUTPUTDIR -pC -rS -pCC -gitRepo $INPUTDIR || { echo "Failed ASTracker analysis."; exit; }
    fi
}

run_multiple(){
    for repo in $( ls $REPOS_HOME ); do
	    run -p $repo -i $REPOS_HOME/$repo -o $DIR_OUT
    done 
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

            -rA | --runArcan)
            RUNARCAN=true
            shift
            ;;

            -rT | --runTracker)
            RUNTRACKER=true
            shift
            ;;

            -c | --compile)
            COMPILE=true
            shift
            ;;

            -f | --projectsFolder)
            REPO_HOME=$2
            shift
            shift
            ;;

            *)
            usage
            exit
            ;;

        esac

    done
}

check_args(){
    if { [ -z $INPUTDIR ] && [ -z $REPO_HOME ]; }; then
        echo "Input directory is missing."
        print_usage=true
    fi

    if [[ -z $OUTPUTDIR ]]; then
        echo "Output directory is missing."
        print_usage=true
    fi

    if { [ ! -z $RUNARCAN ] && [ ! -f $ARCAN_JAR ]; }; then
        echo "Arcan JAR file does not exist."
        print_usage=true
    fi

    if { [ ! -z $RUNTRACKER ] && [ ! -f $ASTRACKER_JAR ]; }; then
        echo "ASTracker JAR file does not exist."
        print_usage=true
    fi

    if { [ -z $RUNTRACKER ] && [ -z $RUNARCAN ];}; then
        echo "ASTracker or/and Arcan have to be executed."
        print_usage=true
    fi

    if [[ ! -z $print_usage ]]; then
        usage
        exit
    fi
}

parse_args $@
check_args

if [[ ! -z $COMPILE ]]; then
    echo compile 
fi

if [[ ! -z $REPO_HOME ]]; then
    echo run_multiple
else
    echo run_no_parse
fi
echo $@