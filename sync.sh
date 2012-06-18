rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/Archimulator_crosscompiler/ customer@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/Tools/apache-maven-3.0.3/ customer@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Tools/apache-maven-3.0.3/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/.m2/repository/ customer@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/.m2/repository/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/Archimulator/ customer@[2001:0:53aa:64c:1050:25cc:522f:46c5]:/home/itecgo/Archimulator/


rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/ root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/Archimulator/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/Archimulator/ customer@173.208.185.58:/home/itecgo/Archimulator/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/.m2/repository/ root@[2400:8900::f03c:91ff:feae:ff27]:/home/itecgo/.m2/repository/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l customer" /home/itecgo/.m2/repository/ customer@173.208.185.58:/home/itecgo/.m2/repository/
