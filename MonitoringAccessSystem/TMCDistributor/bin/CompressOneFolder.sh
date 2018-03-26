#!/bin/bash
#This batch compress one folder specified by parameter (Entire month or entire day or specific device)

#The lock file
lock_file_compress_all_year="$HOME/TMCS/logs/CompressAllYear.lock"
lock_file_compress_last_old_data="$HOME/TMCS/logs/CompressLastOldData.lock"
lock_file_compress_one_folder="$HOME/TMCS/logs/CompressOneFolder.lock"
lock_file_compress_year="$HOME/TMCS/logs/CompressYear.lock"

if [ -f $lock_file_compress_all_year ] || [ -f $lock_file_compress_one_folder ] || [ -f $lock_file_compress_year ]
then
  #echo "Compression of one folder is locked at ${date}. Another compression process is already running." > $HOME/TMCS/logs/CompressOneFolder.log
  exit
else
  #Create the flag file
  touch ${lock_file_compress_one_folder}

  count1=$(/bin/ls -l $1 | wc -l)
  size1=$(/usr/bin/du -sh $1)
  echo "Before of the compression" > $HOME/TMCS/logs/CompressOneFolder.log
  echo "Count1: ${count1} Size1: ${size1}" >> $HOME/TMCS/logs/CompressOneFolder.log
  
  files=$(/bin/find $1 -name "*.txt")

  for file in ${files}
    do
      /usr/bin/bzip2 ${file}
      echo "Compression of ${file} ready"
    done
  
  count2=$(/bin/ls -l $1 | wc -l)
  size2=$(/usr/bin/du -sh $1)
  echo "After of the compression" >> $HOME/TMCS/logs/CompressOneFolder.log
  echo "Count2: ${count2} Size2: ${size2}" >> $HOME/TMCS/logs/CompressOneFolder.log

  #Remove flag file
  /bin/rm -rf ${lock_file_compress_one_folder}

  echo "The ${lock_file_compress_one_folder} was deleted" >> $HOME/TMCS/logs/CompressOneFolder.log
  echo "End compress one folder process" >> $HOME/TMCS/logs/CompressOneFolder.log
  echo "=============================" >> $HOME/TMCS/logs/CompressOneFolder.log
fi
