#!/bin/bash
#SBATCH --job-name ecj-het
#SBATCH --time=00:10:00
#SBATCH --output=testFolder/slurm/ECJjob%j.out
#SBATCH --export=ALL
#SBATCH --mem-per-cpu=1G
#SBATCH --cpus-per-task=10
#SBATCH --ntasks-per-node=1
#SBATCH --nodes=1


# run from /home/STRATEGY-MINING/dslater/strategy-mining/mariah_cleaning/src
# :/home/STRATEGY-MINING/NetLogo\ 6.1.1/app/*:/home/STRATEGY-MINING/NetLogo\ 6.1.1/app/extensions/.bundled/*
# compile java code, process the params file, compile the new rules (java classes)
javac -cp .:../lib/*:/home/STRATEGY-MINING/NetLogo\ 6.1.1/app/* testFolder/*.java
java -cp .:../lib/* testFolder.EvolutionaryModelDiscovery testFolder/testParams_copy.params
javac -cp .:../lib/* testFolder/rules/*.java

# run ECJ
java -cp .:../lib/*:/home/STRATEGY-MINING/NetLogo\ 6.1.1/app/* ec.Evolve -file testFolder/testParams_copy.apeFight_02.nlogo.params -p outputPath=data/output2.csv -p rowNumber=7
