dev:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/git_repos/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/git_repos/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/.m2/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/.m2/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/idea-IC-122.264/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Tools/idea-IC-122.264/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/apache-maven-3.0.4/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Tools/apache-maven-3.0.4/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/


local to linode:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator_crosscompiler/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/benchmarks/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/benchmarks/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/run_web_server.sh root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/run_web_server.sh
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/lib/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/tools/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/tools/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/experiment_inputs/ root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.jar root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/target/archimulator.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.war root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/target/archimulator.war


linode to server:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator_crosscompiler/ root@23.29.77.186:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/benchmarks/ root@23.29.77.186:/home/itecgo/Archimulator/benchmarks/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/run_web_server.sh root@23.29.77.186:/home/itecgo/Archimulator/run_web_server.sh
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/lib/ root@23.29.77.186:/home/itecgo/Archimulator/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/tools/ root@23.29.77.186:/home/itecgo/Archimulator/tools/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/experiment_inputs/ root@23.29.77.186:/home/itecgo/Archimulator/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.jar root@23.29.77.186:/home/itecgo/Archimulator/target/archimulator.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.war root@23.29.77.186:/home/itecgo/Archimulator/target/archimulator.war

copy back experiment results:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" root@[2400:8900::f03c:91ff:feae:649f]:/home/itecgo/Archimulator/experiments/ /home/itecgo/Archimulator/experiments/

