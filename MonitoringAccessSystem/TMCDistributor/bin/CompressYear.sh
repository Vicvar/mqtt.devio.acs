#!/bin/bash
#This batch compress a year folder from the monitordata

compress() {
  echo "Start compression of: /var/opt/alma/monitordata/$1/$2" >> $HOME/TMCS/logs/CompressYear.log

  count_files=0
  files=$(/bin/find /var/opt/alma/monitordata/$1/$2 -name "*.txt")
  for file in ${files}
  do
    file_bz2="${file}.bz2"
    if [ -f $file_bz2 ]
    then
      file_tmp="${file}.tmp"
      file_new="${file}.new"
      /usr/bin/bzcat ${file_bz2} > ${file_tmp}
      /bin/cat ${file_tmp} ${file} > ${file_new}
      /bin/rm -rf ${file_tmp} ${file_bz2} ${file}
      /bin/mv ${file_new} ${file}
      /bin/chown apache.apache ${file}
      /bin/chmod 777 ${file}
    fi
    /usr/bin/bzip2 ${file} >> $HOME/TMCS/logs/CompressYear.log
    (( count_files++ ))
  done

  echo "Compressed files: ${count_files}" >> $HOME/TMCS/logs/CompressYear.log
  echo "Compression of all *.txt files of ${dir} were done" >> $HOME/TMCS/logs/CompressYear.log
  echo "------------------------------" >> $HOME/TMCS/logs/CompressYear.log
}

#The lock file
lock_file_compress_year="$HOME/TMCS/logs/CompressYear.lock"
lock_file_compress_last_old_data="$HOME/TMCS/logs/CompressLastOldData.lock"
lock_file_compress_one_folder="$HOME/TMCS/logs/CompressOneFolder.lock"

if [ -f $lock_file_compress_year ] || [ -f $lock_file_compress_last_old_data ] || [ -f $lock_file_compress_one_folder ]
then
  #echo "Compression of year is locked at ${date}. Another compression process is already running." > $HOME/TMCS/logs/CompressYear.log
  exit
else
  #Create the flag file
  touch ${lock_file_compress_year}

  date=$(date)
  echo "Star compress $1 data process ${date}" > $HOME/TMCS/logs/CompressYear.log

  for month in 01 02 03 04 05 06 07 08 09 10 11 12
  do
    compress $1 $month
  done

  #Remove flag file
  /bin/rm -rf ${lock_file_compress_year}

  echo "The ${lock_file_compress_year} was deleted" >> $HOME/TMCS/logs/CompressYear.log
  echo "End compress $1 data process" >> $HOME/TMCS/logs/CompressYear.log
  echo "============================" >> $HOME/TMCS/logs/CompressYear.log
fi
