#!/bin/bash
#Launchs the compress and move bash process
/usr/bin/nohup /bin/nice -n 10 $HOME/TMCS/bin/CompressMoveLastOldData.sh > $HOME/TMCS/logs/CompressMoveDaily.log &
