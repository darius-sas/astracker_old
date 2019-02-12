#!/bin/bash

TRACKAS_JAR="../target/trackas/trackas-0.1.jar"

usage_single() {
    echo "Usage:    analyse-single -p <project-name> -o <out-directory> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
    exit
}

usage_multiple(){
    echo "Usage:    analyse-multiple -m <master-directory> -o <out-directory> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
    exit
}

trackas(){
    java -jar $TRACKAS_JAR $@
}

analyse_single(){
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
        -o | --outdir )         shift
                                OUTPUTDIR=$1
                                ;;
        -rT | --run-tracker )   RUN_TRACKER=true
                                ;;
        -rA | --run-arcan )     RUN_ARCAN="-rA"
                                ;;
        -tC | --track-consec-only )  NON_CONSEC_VERS="-dNC"
                                ;;
        -c  | --recompile-tracker ) RECOMPILE_TRACKER=true
                                ;;
        -h | --help )           usage_single
                                ;;
        * )                     usage_single
    esac
    shift
    done

    if [ -z $PROJECT ] ; then
        echo "Please provide a project name."
        usage_single
    fi

    if [ -z $OUTPUTDIR ] ; then
        echo "Please provide an output directory."
        usage_single
    fi

    INPUTDIR="$OUTPUTDIR/arcanOutput/$PROJECT/"

    if [ $RUN_TRACKER = true ] ; then
        if [ $RECOMPILE_TRACKER = true ] ; then 
            oldir=$(pwd)
            cd ..
            echo "Compiling tracker..."
            mvn package > /dev/null
            if [ $? -eq 0 ] ; then
                echo "Build successful."
            else
                echo "Build failed. Exiting..."
                exit
            fi
            cd $oldir
        fi
        trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR -pC -pS $RUN_ARCAN $NON_CONSEC_VERS
        if [ $? -ne 0 ] ; then
            echo "Tracking failed. Exiting..."
            exit
        fi
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

    R -e "e<-new.env();e[['project']]<-'$PROJECT';e[['type']]<-'$SUFFIX';rmarkdown::render('as-history-in-system.Rmd', output_file='$OUTPUTDIR/as-history-in-system-$SUFFIX.nb.html',envir=e)"
}

analyse_multiple(){

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
        -rA | --run-arcan )     RUN_ARCAN="-rA"
                                ;;
        -tC | --track-consec-only )  CONSECONLY="-tC"
                                ;;
        -c  | --recompile-tracker ) RECOMPILE_TRACKER="-c"
                                ;;
        -h | --help )           usage_multiple
                                exit
                                ;;
        * )                     usage_multiple
                                exit 1
    esac
    shift
    done

    for project_dir in $MASTERDIR/*/
    do
        project_dir=${project_dir%*/} 
        PROJECT=$(basename $project_dir)
        echo "analyse_single -p $PROJECT -o $OUTPUTDIR $RUN_TRACKER $RUN_ARCAN $NON_CONSEC_VERS $RECOMPILE_TRACKER"
        analyse_single -p $PROJECT -o $OUTPUTDIR $RUN_TRACKER $RUN_ARCAN $CONSECONLY $RECOMPILE_TRACKER
    done
}