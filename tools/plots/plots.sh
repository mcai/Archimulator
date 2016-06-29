#!/usr/bin/env bash
PYTHONPATH=$PYTHONPATH:.:`pwd`.
export PYTHONPATH

cd $(dirname "$0")

python2.7 ./plots.py $@