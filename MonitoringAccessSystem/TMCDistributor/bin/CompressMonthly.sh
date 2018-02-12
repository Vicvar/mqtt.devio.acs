#!/bin/bash
#Launchs the compress bash process
sudo /bin/nice -n -19 ionice -c2 -n1 su - tmcs $HOME/TMCS/bin/CompressLastOldData.sh > $HOME/TMCS/logs/CompressMonthly.log 2>&1
