REMOTE_SERVER_IP=50.115.38.134

#ssh root@$REMOTE_SERVER_IP 'useradd -m -p bywwnss itecgo'
#ssh root@$REMOTE_SERVER_IP 'passwd itecgo'

#------------------------------------------------------------------------

#ssh root@$REMOTE_SERVER_IP 'sudo apt-get install ant'
#ssh root@$REMOTE_SERVER_IP 'rm -rf /home/itecgo/Archimulator/'

#ssh root@$REMOTE_SERVER_IP 'mkdir -p /home/itecgo/Archimulator/benchmarks/; mkdir -p /home/itecgo/Archimulator/out/artifacts/Archimulator_jar/'

#rsync -avz /home/itecgo/Archimulator/benchmarks/ root@$REMOTE_SERVER_IP:/home/itecgo/Archimulator/benchmarks/
#rsync -avz /home/itecgo/Archimulator/out/artifacts/Archimulator_jar/ root@$REMOTE_SERVER_IP:/home/itecgo/Archimulator/out/artifacts/Archimulator_jar/

#------------------------------------------------------------------------

ssh root@$REMOTE_SERVER_IP 'cd /home/itecgo/Archimulator; nohup `java -cp "out/artifacts/Archimulator_jar/Archimulator.jar:out/artifacts/Archimulator_jar/jna.jar" archimulator.client.GuestStartup` &'
#ssh root@$REMOTE_SERVER_IP 'cd /home/itecgo/Archimulator; java -cp "out/artifacts/Archimulator_jar/Archimulator.jar:out/artifacts/Archimulator_jar/jna.jar" archimulator.client.GuestStartup'