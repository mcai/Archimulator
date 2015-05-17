dev:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/git_repos/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/git_repos/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/.m2/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/.m2/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/idea-IC-122.264/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Tools/idea-IC-122.264/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/apache-maven-3.0.4/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Tools/apache-maven-3.0.4/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/


local to linode:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/benchmarks/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/benchmarks/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/tools/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/tools/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/experiment_inputs/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/experiment_tasks/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/experiment_tasks/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/run.sh root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/run.sh

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/lib/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/archimulator.ext.jar root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/target/archimulator.ext.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/archimulator.ext.war root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/target/archimulator.ext.war


linode to server:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/benchmarks/ root@23.29.77.186:/home/itecgo/Archimulator/benchmarks/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/tools/ root@23.29.77.186:/home/itecgo/Archimulator/tools/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/experiment_inputs/ root@23.29.77.186:/home/itecgo/Archimulator.Ext/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/experiment_tasks/ root@23.29.77.186:/home/itecgo/Archimulator.Ext/experiment_tasks/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/run.sh root@23.29.77.186:/home/itecgo/Archimulator.Ext/run.sh

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/lib/ root@23.29.77.186:/home/itecgo/Archimulator.Ext/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/archimulator.ext.jar root@23.29.77.186:/home/itecgo/Archimulator.Ext/target/archimulator.ext.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator.Ext/target/archimulator.ext.war root@23.29.77.186:/home/itecgo/Archimulator.Ext/target/archimulator.ext.war

local to desktop:
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Tools/jdk1.6.0_33/ itecgo@10.26.27.48:/home/itecgo/Tools/jdk1.6.0_33/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Tools/apache-maven-3.0.4/ itecgo@10.26.27.48:/home/itecgo/Tools/apache-maven-3.0.4/

--------------------------
to 10.26.27.48

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator/benchmarks/ itecgo@10.26.27.48:/home/itecgo/Archimulator/benchmarks/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator/tools/ itecgo@10.26.27.48:/home/itecgo/Archimulator/tools/

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/experiment_inputs/ itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/experiment_tasks/ itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/experiment_tasks/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/run.sh itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/run.sh

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/lib/ itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/target/lib/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/archimulator.ext.jar itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/target/archimulator.ext.jar
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/archimulator.ext.war itecgo@10.26.27.48:/home/itecgo/Archimulator.Ext/target/archimulator.ext.war

--------------------------
to 10.26.27.29

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator/benchmarks/ itecgo@10.26.27.29:/home/itecgo/Archimulator/benchmarks/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator/tools/ itecgo@10.26.27.29:/home/itecgo/Archimulator/tools/

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/experiment_inputs/ itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/experiment_tasks/ itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/experiment_tasks/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/run.sh itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/run.sh

rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/lib/ itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/target/lib/
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/archimulator.ext.jar itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/target/archimulator.ext.jar
rsync --recursive --progress -avz -e "sshpass -p bywwnss ssh -l itecgo" /home/itecgo/Archimulator.Ext/target/archimulator.ext.war itecgo@10.26.27.29:/home/itecgo/Archimulator.Ext/target/archimulator.ext.war

copy back experiment results:
#TODO: to be updated!!!
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator.Ext/experiments/ /home/itecgo/Archimulator.Ext/experiments/
