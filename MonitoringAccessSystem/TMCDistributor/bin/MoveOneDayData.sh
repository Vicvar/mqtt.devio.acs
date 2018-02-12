#!/bin/bash
#This batch moves one day of monitor data day from the current to monitordata.

#The lock file
lock_file="/opt/tmcs/TMCS/logs/MoveOneDayData.lock"

date=$(date)

if [ -f $lock_file ]
then
  echo "Movement of one day is locked ${date}" > /opt/tmcs/TMCS/logs/MoveOneDayData.log 
else
  #Create the flag file
  touch ${lock_file}

  #If is necessary, it process performs append into existed files
  echo "Start process to move one day of data ${date}" > /opt/tmcs/TMCS/logs/MoveOneDayData.log

  #Find all directories on current
  directories=$(/bin/find /var/opt/alma/monitordata/current/2014/09/2014-09-17 -mindepth 1 -maxdepth 1 | /bin/sort -n)
  
  #See all directories into current.
  #Copy or add each file from current to parent folder.
  echo "Starting Copy or add each file from current to parent folder"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
  for dir in ${directories}
  do
    #Find all text files
    echo "Starting movement of files"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
    files=$(/bin/find ${dir} -name "*.txt")
    for filesource in ${files}
    do
      filetarget=${filesource/current\//}
      
      #Append of file
      if [ -f $filetarget ]
      then
        action="merging"
        /bin/cat ${filesource} >> ${filetarget}
      #Create the file
      else
        pathdevicetarget=${filetarget%/*}
        pathdatetarget=${pathdevicetarget%\/*}

        #If is necessary create the daily folder
        if [ ! -d $pathdatetarget ]
        then
          /bin/mkdir -p ${pathdatetarget}
          /bin/chmod -R 777 ${pathdatetarget}
          /bin/chown -R apache.apache ${pathdatetarget}
        fi
        
        #If is necessary create the device folder
        if [ ! -d $pathdevicetarget ]
        then
          /bin/mkdir -p ${pathdevicetarget}
          /bin/chmod -R 777 ${pathdevicetarget}
          /bin/chown -R apache.apache ${pathdevicetarget}
        fi
       
        #Copy the file source to file target
        action="copying"
        /bin/cp ${filesource} ${pathdevicetarget}
        /bin/chown apache.apache ${filetarget}
      fi
    
      #Remove the file source
      if [ $? -eq 0 ] ; then 
        /bin/rm -rf ${filesource}
      else 
        echo "Error detected while ${action} ${filesource} to ${filetarget} , the  ${filesource} won't be deleted." >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
      fi
    done
    echo "Movement of files is ended"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log

    #Clean empty device directories
    echo "Starting cleaning of empty device directories"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
    dirstoremove1=$(/bin/find /var/opt/alma/monitordata/current -type d -empty)
    for dir1 in ${dirstoremove1}
    do
      /bin/rm -rf ${dir1}
    done
    echo "Cleaning of empty device directories is ended"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
  
    #Clean empty daily directories
    echo "Starting cleaning of empty daily directories"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
    dirstoremove2=$(/bin/find /var/opt/alma/monitordata/current -type d -empty)
    for dir2 in ${dirstoremove2}
    do
      /bin/rm -rf ${dir2}
    done
    echo "Cleaning of empty daily directories is ended"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log

    #Clean empty monthly directories
    echo "Starting cleaning of empty monthly directories"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
    dirstoremove3=$(/bin/find /var/opt/alma/monitordata/current -type d -empty)
    for dir3 in ${dirstoremove3}
    do
      /bin/rm -rf ${dir3}
    done
      echo "Cleaning of empty monthly directories is ended"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log

    #If the yearly folder is empty, then remove the yearly folder
    echo "Starting cleaning of empty yearly directories"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
    filesintodir=$(/bin/find ${dir} -type f | wc -l)
    if [ $filesintodir -eq 0 ]
    then
      /bin/rm -rf ${dir}
    fi
    echo "Cleaning of empty yearly directories is ended"  >> /opt/tmcs/TMCS/logs/MoveOneDayData.log

    (( counter++ ))
  done

  echo "End move one day data process" >> /opt/tmcs/TMCS/logs/MoveOneDayData.log

  #Remove flag file
  /bin/rm -rf ${lock_file}
  
  echo "The ${lock_file} was deleted" >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
  echo "==============================" >> /opt/tmcs/TMCS/logs/MoveOneDayData.log
fi
