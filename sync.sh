rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator_crosscompiler/ ubuntu@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Tools/apache-maven-3.0.3/ ubuntu@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Tools/apache-maven-3.0.3/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/.m2/repository/ ubuntu@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/.m2/repository/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/ ubuntu@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Archimulator/



rsync --recursive --progress --delete -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/.m2/repository/ root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/.m2/repository/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/.m2/repository/ ubuntu@61.129.33.163:/home/itecgo/.m2/repository/

rsync --recursive --progress --delete -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/ root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/Archimulator/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/

if ipv6 unavailable:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/


copy binary only:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator_crosscompiler/ ubuntu@61.129.33.163:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/benchmarks/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/benchmarks/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/target/lib/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/target/lib/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/experiment_inputs/ root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/Archimulator/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/experiment_inputs/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/experiment_inputs/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.jar root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/Archimulator/target/archimulator.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/target/archimulator.jar ubuntu@61.129.33.163:/home/itecgo/Archimulator/target/archimulator.jar


copy back experiment results:
rsync --recursive --progress --delete -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" ubuntu@61.129.33.163:/home/itecgo/Archimulator/experiments/ /home/itecgo/Archimulator/experiments/
rsync --recursive --progress --delete -avz -e "sshpass -p 1026@ustc ssh -l root" root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/Archimulator/experiments/ /home/itecgo/Archimulator/experiments/


new
--------------------------------------------------------------------------------------
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/target/lib/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/tools/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/tools/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/experiment_inputs/ ubuntu@61.129.33.163:/home/itecgo/Archimulator/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l ubuntu" /home/itecgo/Archimulator/target/archimulator.jar ubuntu@61.129.33.163:/home/itecgo/Archimulator/target/archimulator.jar

