#!/bin/bash
#This batch compress the first folder from the monitordata

#The lock file
lock_file_compress_last_old_data="$HOME/TMCS/logs/CompressLastOldData.lock"
lock_file_compress_all_year="$HOME/TMCS/logs/CompressAllYear.lock"
lock_file_compress_one_folder="$HOME/TMCS/logs/CompressOneFolder.lock"
lock_file_compress_year="$HOME/TMCS/logs/CompressYear.lock"

date=$(date)
count_process=$(ps -f -u tmcs | pgrep "/opt/tmcs/TMCS/bin/CompressLastOldData.sh" | wc -l)

if [[ $count_process == 0 ]]
then
  #Remove flag file
  /bin/rm -rf ${lock_file_compress_last_old_data}
fi

if [ -f $lock_file_compress_last_old_data ] || [ -f $lock_file_compress_all_year ] || [ -f $lock_file_compress_one_folder ] || [ -f $lock_file_compress_year ]
then
  #echo "Compression of last old data is locked at ${date}. Another compression process is already running." > $HOME/TMCS/logs/CompressLastOldData.log
  exit
else
  total=$(/bin/ls -l /var/opt/alma/monitordata | wc -l)
  (( total-- ))
  echo "number of folders: ${total}" >> $HOME/TMCS/logs/CompressLastOldData.log

  if [[ $total > 1 ]]
  then
    #Create the flag file
    touch ${lock_file_compress_last_old_data}

    echo "Star compress last old data process ${date}" > $HOME/TMCS/logs/CompressLastOldData.log

    counter_directories=1
    directories=$(/bin/find /var/opt/alma/monitordata -mindepth 3 -maxdepth 3 -not -name "*current*" | /bin/sort -rn | head -61)

    for dir in ${directories}
    do
      if [[ $dir != *current* ]]
      then
        echo "counter_directories: ${counter_directories}" >> $HOME/TMCS/logs/CompressLastOldData.log

        if [ $counter_directories -gt 30 ]
        then
          counter_files=0
          files=$(/bin/find ${dir} -name "*.txt" | head -8000)
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
            /usr/bin/bzip2 ${file} >> $HOME/TMCS/logs/CompressMonthly.log
            (( counter_files++ ))
          done

          echo "counter_files: ${counter_files}" >> $HOME/TMCS/logs/CompressLastOldData.log

          if [ $counter_files -gt 0 ]
          then
            echo "Compression of all *.txt files of ${dir} were done" >> $HOME/TMCS/logs/CompressLastOldData.log
          else
            echo "The ${dir} does not have *.txt files" >> $HOME/TMCS/logs/CompressLastOldData.log
          fi
        else
          echo "Not considered. dir: ${dir} is less than 30 days ago" >> $HOME/TMCS/logs/CompressLastOldData.log
        fi

        (( counter_directories++ ))
      else
        echo "Not considered. dir: ${dir} has current" >> $HOME/TMCS/logs/CompressLastOldData.log
      fi

      echo "----------------------------------" >> $HOME/TMCS/logs/CompressLastOldData.log
    done

    #Remove flag file
    /bin/rm -rf ${lock_file_compress_last_old_data}

    echo "The ${lock_file_compress_last_old_data} was deleted" >> $HOME/TMCS/logs/CompressLastOldData.log
    echo "End compress last old data process" >> $HOME/TMCS/logs/CompressLastOldData.log
    echo "==================================" >> $HOME/TMCS/logs/CompressLastOldData.log
  fi
fi
