#!/bin/bash
#SBATCH --job-name=track_as_qualitas_corpus
#SBATCH --mail-type=ALL
#SBATCH --mail-user=d.d.sas@rug.nl
#SBATCH --output=job-%j.log
#SBATCH --partition=short
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=4
#SBATCH --mem-per-cpu=500

source analyse_system.sh
srun analyse_multiple -p #MASTERDIR -o #OUTPUDIR