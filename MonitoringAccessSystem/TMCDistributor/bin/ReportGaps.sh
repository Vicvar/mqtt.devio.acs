#!/bin/bash
#The lock file

lock_file="/opt/tmcs/TMCS/logs/ReportGaps.lock"

date=$(date)

if [ -f $lock_file ]
then
  echo "Report of gaps of Monitor Data Service is locked ${date}" > /opt/tmcs/TMCS/logs/ReportGaps.log
else
  #Create the flag file
  touch ${lock_file}

  yesterday=$(date --date="yesterday" +"%Y-%m-%d")
  today=`date +%Y-%m-%d`

  yesterday_file_name="check_ACS-${yesterday}.txt"

  sshpass -p '@xxREMOVEDxx' scp almamgr@aos-gns.osf.alma.cl:/users/almamgr/CheckControl/${yesterday_file_name} /opt/tmcs/TMCS/logs/

  yesterday_file=/opt/tmcs/TMCS/logs/${yesterday_file_name}

  echo "Scanning ${yesterday_file}" > /opt/tmcs/TMCS/logs/ReportGaps.log
  if [ -f ${yesterday_file} ]; then
    total_yesterday=$(cat ${yesterday_file} | wc -l)
    count_operational_yesterday=0
    for A in `seq 1 $total_yesterday` ; do
      line_yesterday=$(head -$A ${yesterday_file} | tail -1)
      last_character_yesterday=${line_yesterday: -1}
      if [[ $last_character_yesterday == 1* ]]; then
        (( count_operational_yesterday++ ))
      fi
    done
    percentage_yesterday=$(( 100 * ${count_operational_yesterday}/1440 ))
    rm -rf ${yesterday_file}
    echo "count_operational_yesterday=${count_operational_yesterday}" >> /opt/tmcs/TMCS/logs/ReportGaps.log
  else
    percentage_yesterday="100"
  fi
  echo "percentage_yesterday=${percentage_yesterday}" >> /opt/tmcs/TMCS/logs/ReportGaps.log

  echo "" >> /opt/tmcs/TMCS/logs/ReportGaps.log
  sudo /bin/nice -n -17 ionice -c2 -n3 /opt/tmcs/TMCS/bin/./CheckForMonitorTimeGaps.py -f ${yesterday}"T00:00:00" -t ${today}"T00:00:00" -o ${percentage_yesterday} >> /opt/tmcs/TMCS/logs/ReportGaps.log 2>&1

  #Remove flag file
  /bin/rm -rf ${lock_file}
fi
