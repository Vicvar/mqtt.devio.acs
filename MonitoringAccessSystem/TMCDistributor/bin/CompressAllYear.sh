#!/bin/bash
#Launchs the compress bash process

#The lock file
lock_file_compress_all_year="$HOME/TMCS/logs/CompressAllYear.lock"
lock_file_compress_last_old_data="$HOME/TMCS/logs/CompressLastOldData.lock"
lock_file_compress_one_folder="$HOME/TMCS/logs/CompressOneFolder.lock"
lock_file_compress_year="$HOME/TMCS/logs/CompressYear.lock"

if [ -f $lock_file_compress_all_year ] || [ -f $lock_file_compress_last_old_data ] || [ -f $lock_file_compress_one_folder ] || [ -f $lock_file_compress_year ]
then
  #echo "Compression of all year is locked at ${date}. Another compression process is already running." > $HOME/TMCS/logs/CompressAllYear.log
  exit
else
  #Create the flag file
  touch ${lock_file_compress_all_year}

  year=2010
  current_year=$(date +"%Y")
  while [[ $year -le $current_year ]]
  do
    sudo /bin/nice -n -18 ionice -c2 -n2 su - tmcs $HOME/TMCS/bin/CompressYear.sh ${year} >> $HOME/TMCS/logs/CompressAllYear.log 2>&1
    (( year++ ))
  done

  #Remove flag file
  /bin/rm -rf ${lock_file_compress_all_year}

  echo "The ${lock_file_compress_all_year} was deleted" >> $HOME/TMCS/logs/CompressAllYear.log
  echo "End compress all year process" >> $HOME/TMCS/logs/CompressAllYear.log
  echo "=================================" >> $HOME/TMCS/logs/CompressAllYear.log
fi
