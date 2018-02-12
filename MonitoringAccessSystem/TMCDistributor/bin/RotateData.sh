#!/bin/bash
DIRECTORIES=$(/bin/find /var/opt/alma/monitordata/current -mindepth 1 -maxdepth 1 -cmin +100)
TARGET=/var/opt/alma/monitordata

date=$(date)
echo "Star rotate data process ${date}" > $HOME/TMCS/logs/RotateData.log
for dir in ${DIRECTORIES}
do
  count=$(/bin/ls -l ${dir} | wc -l)
  size=$(/usr/bin/du -sh ${dir})
  echo "Count: ${count} Size: ${size}" >> $HOME/TMCS/logs/RotateData.log
  
  files_sources=$(/bin/find ${dir} -name "*.txt")

  for file_source in ${files_sources}
  do
    echo "File source: ${file_source}" >> $HOME/TMCS/logs/RotateData.log
    
    echo "File target: ${file_source/current\//}" >> $HOME/TMCS/logs/RotateData.log

    /bin/cat ${file_source} >> ${file_source/current\//}
  done
  
  /bin/rm -rf ${dir}
done
echo "End rotate data process" >> $HOME/TMCS/logs/RotateData.log
echo "=======================" >> $HOME/TMCS/logs/RotateData.log
