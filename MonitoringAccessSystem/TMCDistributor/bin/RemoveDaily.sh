#!/bin/bash
#Launchs the move bash process
nice -n 10 /opt/tmcs/TMCS/bin/RemoveLastOldData.sh > /opt/tmcs/TMCS/logs/RemoveDaily.log 2>&1
