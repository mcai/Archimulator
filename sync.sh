rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator_crosscompiler/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator_crosscompiler/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/benchmarks/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/benchmarks/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/git_repos/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/git_repos/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/.m2/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/.m2/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/idea-IC-122.264/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Tools/idea-IC-122.264/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Tools/apache-maven-3.0.4/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Tools/apache-maven-3.0.4/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/

rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/lib/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/target/lib/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/tools/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/tools/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/experiment_inputs/ root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/experiment_inputs/
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.jar root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/target/archimulator.jar
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" /home/itecgo/Archimulator/target/archimulator.war root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/target/archimulator.war

copy back experiment results:
rsync --recursive --progress -avz -e "sshpass -p 1026@ustc ssh -l root" root@[2607:F180:4102:1::32:14B5]:/home/itecgo/Archimulator/experiments/ /home/itecgo/Archimulator/experiments/

