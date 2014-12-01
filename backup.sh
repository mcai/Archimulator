#!/bin/sh
NOW=$(date +"%Y%m%d-%H%M%S")

cd /home/itecgo/Archimulator
git archive --format=tar HEAD | gzip >/home/itecgo/Backup/Archimulator/Archimulator_backup_$NOW.tar.gz
