#!/bin/bash
#SBATCH --job-name=track_as_qualitas_corpus
#SBATCH --mail-type=ALL
#SBATCH --mail-user=d.d.sas@rug.nl
#SBATCH --output=job-%j.log
#SBATCH --partition=short
#SBATCH --nodes=1
#SBATCH --ntasks=4
#SBATCH --cpus-per-task=2
#SBATCH --mem=16000

module restore trackas

source analyse_system.sh
srun analyse_multiple -m #MASTERDIR -o #OUTPUDIR