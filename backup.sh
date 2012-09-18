NOW=$(date +"%Y%m%d-%H%M%S")
git archive --format=tar HEAD | gzip >/home/itecgo/Backup/Archimulator/Archimulator_backup_$NOW.tar.gz
