#!/bin/bash
#Launchs the move bash process
sudo /bin/nice -n -20 ionice -c2 -n0 su - tmcs $HOME/TMCS/bin/MoveLastOldData.sh > $HOME/TMCS/logs/MoveDaily.log 2>&1
