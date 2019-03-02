#!/bin/bash
#SBATCH --job-name=track_as_qualitas_corpus
#SBATCH --mail-type=ALL
#SBATCH --mail-user=d.d.sas@rug.nl
#SBATCH --output=job-%j.log
#SBATCH --partition=regular
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=8
#SBATCH --mem=64000

module restore trackas

srun ./analyse-system.sh -m /data/p284098/qualitas-corpus/input -o /data/p284098/qualitas-corpus/output -rT -rA -pC -pS 
