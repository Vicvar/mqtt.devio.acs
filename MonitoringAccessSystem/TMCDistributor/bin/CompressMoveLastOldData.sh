#!/bin/bash
#This batch compress and move the first folder from the current to monitordata
date=$(date)
echo "Star compress and move last old data process ${date}" > $HOME/TMCS/logs/CompressMoveLastOldData.log

total=$(/bin/ls -l /var/opt/alma/monitordata/current | wc -l)
(( total-- ))
echo "total: ${total}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log

if [[ $total > 1 ]]
then
  counter=1
  directories=$(/bin/find /var/opt/alma/monitordata/current -mindepth 1 -maxdepth 1 | /bin/sort -n)

  for dir in ${directories}
  do
    echo "counter: ${counter}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
    echo "dir: ${dir}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log

    if [[ $counter < $total ]]
    then
      count1=$(/bin/ls -l ${dir} | wc -l)
      size1=$(/usr/bin/du -sh ${dir})
      echo "Before of move" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
      echo "Count1: ${count1} Size1: ${size1}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
    
      /bin/cp -rf ${dir} /var/opt/alma/monitordata
      /bin/rm -rf ${dir}
    
      targetdir=${dir/current\//}
      echo "Target dir: " ${targetdir} >> $HOME/TMCS/logs/CompressMoveLastOldData.log

      count2=$(/bin/ls -l ${targetdir} | wc -l)
      size2=$(/usr/bin/du -sh ${targetdir})
      echo "Before of compression" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
      echo "Count2: ${count2} Size2: ${size2}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
 
      files=$(/bin/find ${targetdir} -name "*.txt")
      for file in ${files}
      do
        /usr/bin/bzip2 ${file}
        echo "Compression of ${file} ready"
      done

      count3=$(/bin/ls -l ${targetdir} | wc -l)
      size3=$(/usr/bin/du -sh ${targetdir})
      echo "After of compression" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
      echo "Count3: ${count3} Size3: ${size3}" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
    fi
    (( counter++ ))
  done
fi
echo "End compress and move last old data process" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
echo "===========================================" >> $HOME/TMCS/logs/CompressMoveLastOldData.log
