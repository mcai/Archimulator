rsync -avz -e "sshpass -p 1026@ustc ssh -l itecgo" ~/Archimulator/target/ itecgo@204.152.205.131:~/Archimulator/target/
rsync -avz -e "sshpass -p xmttb123456 ssh -l xmttb" ~/Archimulator/target/ xmttb@110.80.31.30:~/Archimulator/target/

sshpass -p "1026@ustc" ssh root@204.152.205.131 'nohup `/home/itecgo/Archimulator/run_server.sh` &'

sshpass -p "1026@ustc" ssh root@204.152.205.131 'killall java'
sshpass -p "1026@ustc" ssh itecgo@204.152.205.131 'nohup `/home/itecgo/Archimulator/run.sh` &'

sshpass -p "xmttb123456" ssh xmttb@110.80.31.30 'killall java'
sshpass -p "xmttb123456" ssh xmttb@110.80.31.30 'nohup `/home/itecgo/Archimulator/run.sh` &'
