#!/bin/bash

TRACKAS_JAR="/home/p284098/git/trackas/target/trackas/trackas-0.5.jar"
ARCAN_JAR="/home/p284098/git/trackas/arcan/Arcan-1.4.0-SNAPSHOT.jar"
ANALYSE_NOTEBOOK="/home/p284098/git/trackas/data-analysis/as-history-in-system.Rmd"

init_var(){
    MASTERDIR=""
    PROJECT=""
    MULTIPLE_PROJECTS=""
    RUN_ARCAN=""
    RUN_TRACKER=""
    RECOMPILE_TRACKER=""
    NON_CONSEC_VERS=""
    SIMIL_SCORES=""
    SMELL_CHARAC=""
    INPUTDIR=""
    OUTPUTDIR=""
    ERRORS=0
    PARALLELTASKS=4
}

init_var 

parse_args(){

    while [ "$1" != "" ]; do
    case $1 in
        -m | --master-dir )     shift
                                MASTERDIR=$1
                                ;;
        -o | --outdir )         shift
                                OUTPUTDIR=$1
                                ;;
        -i | --inputdir )       shift
                                INPUTDIR=$1
                                ;;
        -rT | --run-tracker )   RUN_TRACKER="-rT"
                                ;;
        -p | --project )        shift
                                PROJECT=$1
                                ;;
        -mP | --multiple-projects ) shift
                                MULTIPLE_PROJECTS=$1
                                ;;
        -rA | --run-arcan )     RUN_ARCAN="-rA"
                                ;;
        -tNC | --track-non-consec ) NON_CONSEC_VERS="-eNC"
                                ;;
        -c  | --recompile-tracker ) RECOMPILE_TRACKER="-c"
                                ;;
        -pC  | --print-characteristics ) SMELL_CHARAC="-pC"
                                ;;
        -pS  | --print-similarity-scores ) SIMIL_SCORES="-pS"
                                ;;
        -h | --help )           usage_single
                                usage_multiple
                                ;;
    esac
    shift
    done
}

usage_single() {
    echo "Usage:\n    analyse_single -p <project-name> -i <in-directory> -o <out-directory> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
}

usage_multiple(){
    echo "Usage:\n    analyse_multiple -m <master-directory> -o <out-directory> -mP <comma-separated-projects-name-list> [-rA|-run-Arcan][-rT|-run-Tracker][-c|--recompile-tracker][-tC|-track-consec-only][-h|--help]"
}

trackas(){
    java -Xmx20000m -jar $TRACKAS_JAR $@
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

    if [[ -z $PROJECT || -z $OUTPUTDIR ]] ; then
        echo "Please provide a project name and/or an output directory."
        usage_single
        return
    fi

    if [[ $RUN_TRACKER == "-rT" ]] ; then
        if [[ $RECOMPILE_TRACKER == "-c" ]] ; then
            recompile_tracker
        fi

        if [[ $RUN_ARCAN == "-rA" ]]; then
            trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR $SMELL_CHARAC $SIMIL_SCORES $NON_CONSEC_VERS $RUN_ARCAN "$ARCAN_JAR"
        else
            trackas -p $PROJECT -i $INPUTDIR -o $OUTPUTDIR $SMELL_CHARAC $SIMIL_SCORES $NON_CONSEC_VERS
        fi

        if [ $? -ne 0 ] ; then
            echo "Tracking failed for project $PROJECT."
            ERRORS=$(($ERRORS + 1))
            return $?
        fi
    fi

    OUTPUTDIR_PROJECT="$OUTPUTDIR/trackASOutput/$PROJECT"

    if [ -z $NON_CONSEC_VERS ] ; then
        SUFFIX="consecOnly"
    else
        SUFFIX="nonConsec"
    fi

    #SIMILARITY_SCORES_FILE=$OUTPUTDIR_PROJECT/similarity-scores-$SUFFIX.csv
    #SMELL_CHARACTERISTICS_FILE=$OUTPUTDIR_PROJECT/smell-characteristics-$SUFFIX.csv

    #similarity_score $SIMILARITY_SCORES_FILE $OUTPUTDIR_PROJECT/smell-similarity-matrices-$SUFFIX.pdf
    #notebook $PROJECT $SUFFIX $SIMILARITY_SCORES_FILE $SMELL_CHARACTERISTICS_FILE "$OUTPUTDIR_PROJECT/as-history-in-system-$SUFFIX.nb.html"
}

analyse_multiple(){

    parse_args $@

    if [[ -z $MASTERDIR || -z $OUTPUTDIR ]]
    then
        echo "Please provide a master and an output directory."
        usage_multiple
        return
    fi

    if [[ $RECOMPILE_TRACKER == "-c" ]] ; then
        recompile_tracker
    fi

    IFS="," read -ra projects <<< "${MULTIPLE_PROJECTS}"
    for project in "${projects[@]}"
    do
        project_dir=${MASTERDIR}/${project}
        projectLogfile=${OUTPUTDIR}/${project}-outputlog.log

        echo "Starting analysis in parallel on $project"
        analyse_single -p $project -i $MASTERDIR -o $OUTPUTDIR $RUN_TRACKER $RUN_ARCAN $NON_CONSEC_VERS $SMELL_CHARAC $SIMIL_SCORES $RECOMPILE_TRACKER 2>&1 > ${projectLogfile} &
    done
    wait
    echo "Finished projects: ${projects[@]}"
    echo "Completed with $ERRORS errors."
}

analyse_all_no_arcan(){
    projects_task1="freemind,jmeter,jung,freecol"
    projects_task2="jstock,junit,lucene,weka"
    projects_task3="ant,antlr,argouml,hibernate"
    projects_task4="jgraph,azureus"

    analyse_multiple -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task1} -pC -rT

    analyse_multiple -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task2} -pC -rT

    analyse_multiple -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task3} -pC -rT

    analyse_multiple -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task4} -pC -rT
}

analyse_all_sizes(){

    MASTERDIR=$1
    OUTDIR=$2

    for project in ${MASTERDIR}/*/
    do
        project=$(basename ${project%*/})
        project_dir=${MASTERDIR}/${project}

        java -jar target/trackas/trackas-0.5.jar -p ${project} -i ${MASTERDIR} -o ${OUTDIR} -dRT -rS

    done
}