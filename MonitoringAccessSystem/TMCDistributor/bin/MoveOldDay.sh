#!/bin/bash
#This batch moves the oldest monitor data day from the buffer to the historical area, using rsync.

#The lock file
lock_file="/opt/tmcs/TMCS/logs/MoveOldDay.lock"

date=$(date)

if [ -f $lock_file ]
then
  echo "Movement of files is locked ${date}" > /opt/tmcs/TMCS/logs/MoveOldDay.log 
else
  #Create the flag file
  touch ${lock_file}

  #If is necessary, it process performs append into existed files
  echo "Start move old day process ${date}" > /opt/tmcs/TMCS/logs/MoveOldDay.log

  path_buffer_area="/var/opt/alma/monitordata/current/"
  path_historical_area="/var/opt/alma/monitordata/"

  count_sources=$(/bin/find ${path_buffer_area} -mindepth 3 -maxdepth 3 -type d | /bin/sort -n | wc -l)

  while [ $count_sources -ge 20 ]; do
    path_source=$(/bin/find ${path_buffer_area} -mindepth 3 -maxdepth 3 -type d | /bin/sort -n | head -1)
    path_destination=${path_source/$path_buffer_area/$path_historical_area}
    basename_path_destination=$(/bin/basename ${path_destination})
    path_destination=${path_destination/$basename_path_destination/}

    echo "Moving data from ${path_source} to ${path_destination}" >> /opt/tmcs/TMCS/logs/MoveOldDay.log

    rsync -azvh ${path_source} ${path_destination}
    chown -R apache.apache ${path_destination}
    chmod -R 777 ${path_destination}
    /bin/rm -rf ${path_source}

    count_sources=$(/bin/find ${path_buffer_area} -mindepth 3 -maxdepth 3 -type d | /bin/sort -n | wc -l)
  done

  echo "The count of source's folders is ${count_sources}. This values is less than 20. Data will not moved" >> /opt/tmcs/TMCS/logs/MoveOldDay.log

  find ${path_buffer_area} -type d -empty -exec rmdir {} \;

  echo "End move old day process" >> /opt/tmcs/TMCS/logs/MoveOldDay.log

  #Remove flag file
  /bin/rm -rf ${lock_file}

  echo "The ${lock_file} was deleted" >> /opt/tmcs/TMCS/logs/MoveOldDay.log
  echo "==============================" >> /opt/tmcs/TMCS/logs/MoveOldDay.log
fi
