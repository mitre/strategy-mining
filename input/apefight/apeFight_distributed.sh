#!/bin/bash
#SBATCH --job-name ecj-test
#SBATCH --time=00:10:00
#SBATCH --output=/home/STRATEGY-MINING/output/slurm/ECJ_job_%j.out
#SBATCH --export=ALL
#SBATCH --mail-type=ALL
# --- Master resources ---
#SBATCH --nodes=1
#SBATCH --mem-per-cpu=1G
#SBATCH --cpus-per-task=1
#SBATCH --ntasks-per-node=1
# --- Worker resources ---
#SBATCH packjob
#SBATCH --mem-per-cpu=1G
#SBATCH --cpus-per-task=4
#SBATCH --ntasks-per-node=1
#SBATCH --nodes=1


NODES=$(scontrol show hostnames)

# start master
srun --pack-group 0 --output "/home/STRATEGY-MINING/output/slurm/tmp2.txt" /home/STRATEGY-MINING/lib/strategy_mining-1.0-SNAPSHOT/bin/strategy_mining -f /home/STRATEGY-MINING/input/apefight/hpcTestParams.apeFight_02.nlogo.params &

# start all the slaves
srun --pack-group 1 --output "/home/STRATEGY-MINING/output/slurm/tmp3.txt" /home/STRATEGY-MINING/lib/strategy_mining-1.0-SNAPSHOT/bin/strategy_mining -d true -f /home/STRATEGY-MINING/input/apefight/hpcTestParams.apeFight_02.nlogo.slave.params -p eval.master.host=$NODES &


wait
