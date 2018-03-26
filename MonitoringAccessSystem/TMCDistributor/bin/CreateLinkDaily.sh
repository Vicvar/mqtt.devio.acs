#!/bin/bash
#This batch creates the latest symbolic link of monitor data.

#The lock file
lock_file="/opt/tmcs/TMCS/logs/CreateLinkDaily.lock"

date=$(date)

if [ -f $lock_file ]
then
  echo "Create link process is locked ${date}" > /opt/tmcs/TMCS/logs/CreateLinkDaily.log
else
  #Create the flag file
  touch ${lock_file}

  echo "Start process to create link daily ${date}" > /opt/tmcs/TMCS/logs/CreateLinkDaily.log

  path_buffer_area="/var/opt/alma/monitordata/current/"
  path_historical_area="/var/opt/alma/monitordata/"

  current_day=$(date +"%d")
  current_month=$(date +"%m")
  current_year=$(date +"%Y")
  today_folder_name=${current_year}"-"${current_month}"-"${current_day}
  target_path=${path_buffer_area}${current_year}"/"${current_month}"/"${today_folder_name}

  #Create the link to the latest folder
  if [ -d "${target_path}" ]; then
    cd ${path_historical_area}
    if [ ! -d "${current_year}" ]; then
      mkdir ${current_year}
      chown -R apache.apache ${current_year}
    fi
    cd ${current_year}
    if [ ! -d "${current_month}" ]; then
      mkdir ${current_month}
      chown -R apache.apache ${current_month}
    fi
    cd ${current_month}
    symbolic_link=${current_year}"-"${current_month}"-"${current_day}
    if [[ -L "$symbolic_link" && -d "$symbolic_link" ]]; then
      echo "${symbolic_link} symbolic link already exists" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log
    else
      ln -s "${target_path}" ${symbolic_link}
      echo "${symbolic_link} symbolic link was created" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log
    fi
  else
    echo "The " ${target_path} " path does not exist" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log
  fi

  echo "End of create link daily process" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log

  #Remove flag file
  /bin/rm -rf ${lock_file}

  echo "The ${lock_file} was deleted" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log
  echo "==============================" >> /opt/tmcs/TMCS/logs/CreateLinkDaily.log
fi
