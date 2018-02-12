#!/bin/bash
#Launchs the text archiver process
ulimit -u 4096
export INTROOT=$HOME/TMCS
/usr/bin/nohup nice -n -20 ionice -c2 -n0 $HOME/TMCS/bin/TMCTTArchiver > /opt/tmcs/TMCS/logs/TMCTTArchiver.log &
