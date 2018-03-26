#!/bin/bash
#This batch remove the ten folders from the monitordata more old
date=$(date)
echo "Star remove last old data process ${date}" > /opt/tmcs/TMCS/logs/RemoveLastOldData.log

counter=0
directories=$(find /data1/monitordata/ -mindepth 3 -maxdepth 3 -not -name "*current*" | sort -rn)

for dir in ${directories}
do
  echo "counter: ${counter}" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
  echo "dir: ${dir}" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log

  if [ $counter -gt 7 ]
  then
    count1=$(ls -l ${dir} | wc -l)
    size1=$(du -sh ${dir})
    echo "Before of remove" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
    echo "Count1: ${count1} Size1: ${size1}" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
 
    rm -rf ${dir}

    count2=$(ls -l ${dir} | wc -l)
    size2=$(du -sh ${dir})
    echo "After of remove" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
    echo "Count2: ${count2} Size2: ${size2}" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
  fi
  (( counter++ ))
done

directories=$(find /data1/monitordata/ -mindepth 2 -maxdepth 2 -not -name "*current*" -empty)
for dir in ${directories}
do
  rm -rf ${dir}
done

directories=$(find /data1/monitordata/ -mindepth 1 -maxdepth 1 -not -name "*current*" -empty)
for dir in ${directories}
do
  rm -rf ${dir}
done

echo "End remove last old data process" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
echo "================================" >> /opt/tmcs/TMCS/logs/RemoveLastOldData.log
