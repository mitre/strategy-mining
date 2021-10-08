#!/bin/bash
#SBATCH --job-name=nms-ecj-test
#SBATCH --time=00:30:00
#SBATCH --output=/home/STRATEGY-MINING/veneman/output/nms-ecj_job_%j.out
#SBATCH --export=ALL
#SBATCH --mail-user=veneman@mitre.org
#SBATCH --mail-type=ALL
#SBATCH --account=strategy-mining
# --- Master resources ---
#SBATCH --nodes=1
#SBATCH --mem-per-cpu=1G
#SBATCH --cpus-per-task=1
#SBATCH --ntasks-per-node=1
# --- Worker resources ---
#SBATCH packjob
#SBATCH --mem-per-cpu=8G
#SBATCH --cpus-per-task=1
#SBATCH --ntasks-per-node=1
#SBATCH --nodes=1


NODES=$(scontrol show hostnames)

# start master
srun --pack-group 0 --output "/home/STRATEGY-MINING/veneman/output/nms1.txt" /home/STRATEGY-MINING/veneman/lib/strategy_mining-1.0-SNAPSHOT/bin/strategy_mining -f /home/STRATEGY-MINING/veneman/strategy-mining/input/nms/experiment_dist.nms.mason.params &

# start all the slaves
srun --pack-group 1 --output "/home/STRATEGY-MINING/veneman/output/nms2.txt" /home/STRATEGY-MINING/veneman/lib/strategy_mining-1.0-SNAPSHOT/bin/strategy_mining -d true -f /home/STRATEGY-MINING/veneman/strategy_mining/input/nms/experiment_slave.nms.mason.params -p eval.master.host=$NODES &


wait
