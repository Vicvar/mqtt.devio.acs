#!/bin/bash

#The lock file
lock_file="$HOME/TMCS/logs/CompressAllOldData.lock"

date=$(date)

if [ -f $lock_file ]
then
  #echo "Compression of all old data is locked at ${date}. Another compression process is already running." > $HOME/TMCS/logs/CompressAllOldData.log
  exit
else
  #Create the flag file
  touch ${lock_file}

  #This batch compress all untouched folders of monitordata in 60 minutes (1 hr)
  DIRECTORIES=$(/bin/find /var/opt/alma/monitordata -mindepth 1 -maxdepth 1 -cmin +60 | /bin/sort -rn)

  echo "Star compress all old data process ${date}" >> $HOME/TMCS/logs/CompressAllOldData.log
  for dir in ${DIRECTORIES}
  do
    count1=$(/bin/ls -l ${dir} | wc -l)
    size1=$(/usr/bin/du -sh ${dir})
    echo "Before of the compression" >> $HOME/TMCS/logs/CompressAllOldData.log
    echo "Count1: ${count1} Size1: ${size1}" >> $HOME/TMCS/logs/CompressAllOldData.log
  
    files=$(/bin/find ${dir} -name "*.txt")
    for file in ${files}
    do
      /usr/bin/bzip2 ${file}
      echo "Compression of ${file} ready"
    done

    count2=$(/bin/ls -l ${dir} | wc -l)
    size2=$(/usr/bin/du -sh ${dir})
    echo "After of the compression" >> $HOME/TMCS/logs/CompressAllOldData.log
    echo "Count2: ${count2} Size2: ${size2}" >> $HOME/TMCS/logs/CompressAllOldData.log
  done
  echo "End compress all old data process" >> $HOME/TMCS/logs/CompressAllOldData.log

  #Remove flag file
  /bin/rm -rf ${lock_file}

  echo "The ${lock_file} was deleted" >> $HOME/TMCS/logs/CompressAllOldData.log
  echo "=================================" >> $HOME/TMCS/logs/CompressAllOldData.log
fi
