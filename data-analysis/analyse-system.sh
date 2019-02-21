#!/bin/bash

TRACKAS_JAR="../target/trackas/trackas-0.5.jar"
ANALYSE_NOTEBOOK="./as-history-in-system.Rmd"

MASTERDIR=""
PROJECT=""
RUN_ARCAN=""
RUN_TRACKER=""
RECOMPILE_TRACKER=""
NON_CONSEC_VERS="-dNC"
OUTPUTDIR=""
ERRORS=0
PARALLELTASKS=4

parse_args(){

    while [ "$1" != "" ]; do
    case $1 in
        -m | --master-dir )     shift
                                MASTERDIR=$1
                                ;;
        -o | --outdir )         shift
                                OUTPUTDIR=$1
                                ;;
        -rT | --run-tracker )   RUN_TRACKER="-rT"
                                ;;
        -p | --project )        shift
                                PROJECT=$1
                                ;;
        -rT | --run-tracker )   RUN_TRACKER="-rT"
                                ;;
        -rA | --run-arcan )     RUN_ARCAN="-rA"
                                ;;
        -tNC | --track-non-consec ) NON_CONSEC_VERS=""
                                ;;
        -c  | --recompile-tracker ) RECOMPILE_TRACKER="-c"
                                ;;
        -h | --help )           usage_single
                                usage_multiple
                                ;;
    esac
    shift
    done
}

usage_single() {
    echo "Usage:\n    analyse-single -p <project-name> -o <out-directory> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
}

usage_multiple(){
    echo "Usage:\n    analyse-multiple -m <master-directory> -o <out-directory> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
}

trackas(){
    java -jar $TRACKAS_JAR $@
}

# Synopsis: similarity_score <input-scores-file> <output-pdf-file>
similarity_score(){
    Rscript jaccard-linking.r $1 $2
}

configure_R(){
    Rscript rconfig.r
}

# Synopsis: notebook <project-name> <type> <score-file> <characteristics-file> <output-file>
notebook(){
    R -e "e<-new.env();e[['project']]<-'$1';e[['type']]<-'$2';e[['similarity_scores_file']]<-'$3';e[['characteristics_file']]<-'$4';rmarkdown::render('$ANALYSE_NOTEBOOK', output_file='$5',envir=e)"
}

recompile_tracker(){
    oldir=$(pwd)
    cd ..
    echo "Compiling tracker..."
    mvn package -Dmaven.compile.target=1.8 > /dev/null
    if [ $? -eq 0 ] ; then
        echo "Build successful."
    else
        echo "Build failed. Exiting..."
        return -1
    fi
    cd $oldir
    RECOMPILE_TRACKER=""
    return 0
}

analyse_single(){

    parse_args $@

    if [ -z $PROJECT ] ; then
        echo "Please provide a project name."
        usage_single
        return
    fi

    if [ -z $OUTPUTDIR ] ; then
        echo "Please provide an output directory."
        usage_single
        return
    fi

    INPUTDIR="$OUTPUTDIR/arcanOutput/$PROJECT/"

    if [[ $RUN_TRACKER == "-rT" ]] ; then
        if [[ $RECOMPILE_TRACKER == "-c" ]] ; then
            recompile_tracker
        fi
        trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR -pC -pS $RUN_ARCAN $NON_CONSEC_VERS
        if [ $? -ne 0 ] ; then
            echo "Tracking failed for project $PROJECT."
            echo "$(date "+%F %T")\\t$PROJECT" >> $logfile
            ERRORS=$((ERRORS + 1))
            return -1
        fi
    fi

    OUTPUTDIR_PROJECT=$OUTPUTDIR/trackASOutput/$PROJECT

    if [ -z $NON_CONSEC_VERS ] ; then
        SUFFIX="nonConsec"
    else
        SUFFIX="consecOnly"
    fi

    SIMILARITY_SCORES_FILE=$OUTPUTDIR_PROJECT/similarity-scores-$SUFFIX.csv
    SMELL_CHARACTERISTICS_FILE=$OUTPUTDIR_PROJECT/smell-characteristics-$SUFFIX.csv

    similarity_score $SIMILARITY_SCORES_FILE $OUTPUTDIR_PROJECT/smell-similarity-matrices-$SUFFIX.pdf
    notebook $PROJECT $SUFFIX $SIMILARITY_SCORES_FILE $SMELL_CHARACTERISTICS_FILE "$OUTPUTDIR_PROJECT/as-history-in-system-$SUFFIX.nb.html"
}

analyse_multiple(){

    parse_args $@

    if [[ -z $MASTERDIR || -z $OUTPUTDIR ]]
    then
        echo "Please provide a master and an output directory."
        usage_multiple
        return
    fi

    logfile=$MASTERDIR/failed.log

    echo "Analysis started on $(date "+%F %T")" > $logfile

    configure_R

    countpar=0

    if [[ $RECOMPILE_TRACKER == "-c" ]] ; then
        recompile_tracker
    fi

    for project_dir in $MASTERDIR/*/
    do
        project_dir=${project_dir%*/} 
        PROJECT=$(basename $project_dir)

        projectLogfile=${MASTERDIR}/${PROJECT}-outputlog.log

        echo "Running analysis on $PROJECT"

        echo analyse_single -p $PROJECT -o $OUTPUTDIR $RUN_TRACKER $RUN_ARCAN $NON_CONSEC_VERS $RECOMPILE_TRACKER > ${projectLogfile}

        analyse_single -p $PROJECT -o $OUTPUTDIR $RUN_TRACKER $RUN_ARCAN $NON_CONSEC_VERS $RECOMPILE_TRACKER 2>&1 > ${projectLogfile} &

        if [[ $countpar == $PARALLELTASKS ]]; then
            wait
            countpar=0
        fi
        countpar=$((countpar + 1))
    done
    wait
    echo "Completed with $ERRORS errors."
}

