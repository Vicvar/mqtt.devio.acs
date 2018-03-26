/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package archive.tmcdb.monitoring.TMCOffline;

import java.util.List;
import java.util.Date;

import java.io.File;
import java.io.BufferedWriter;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;

/**
 * This class writes the data to the file
 *
 * @version 1.2
 * @author pmerino@alma.cl
 */
public class TMCDumper {
    /** The default path dumper */
    private static final String PATH_DUMPER_DEFAULT = "/var/opt/alma/monitordata/";

    /** The default extension file */
    private static final String EXTENSION_FILE_DEFAULT = "txt";

    /** The default time limit */
    private static final long TIME_LIMIT_DEFAULT = 135762911990000000L;

    /** The time delta default */
    private static final long TIME_DELTA_DEFAULT = 100000000000000000L;

    /** The start time check */
    private static final long CHECK_START_TIME = 10;

    /** The channel name */
    private String channelName;

    /** The data list */
    private List<String> dataList;

    /** The logger */
    private static final Logger logger = Logger.getLogger(TMCDumper.class);

    /** The TMC properties */
    private TMCProperties tmcProperties;

    /** The writer stats */
    private TMCProducerStats writerStats;

    /** The disk write time */
    private TMCTimeStatistic diskWriteTime;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The path of the dumper */
    private String pathDumper = PATH_DUMPER_DEFAULT;

    /** The extension file */
    private  String extensionFile = EXTENSION_FILE_DEFAULT;

    /** The time limit */
    private long timeLimit = TIME_LIMIT_DEFAULT;

    /** The time delta default */
    private long timeDelta = TIME_DELTA_DEFAULT;

    /** Check the start time */
    private long checkStartTime = CHECK_START_TIME;

    /**
     * Constructor
     */
    public TMCDumper() {
        this(null, null, null, null, null, null);
    }

    /**
     * Constructor
     *
     * @param channelName The channel name
     * @param dataList The data list
     * @param tmcProperties The TMC properties
     * @param writerStats The writer stats
     * @param diskWriteTime The disk write time
     * @param startupDate The startup date
     */
    public TMCDumper(String channelName, List<String> dataList, TMCProperties tmcProperties, TMCProducerStats writerStats, TMCTimeStatistic diskWriteTime, Date startupDate) {
        setChannelName(channelName);
        setDataList(dataList);
        setTmcProperties(tmcProperties);
        setWriterStats(writerStats);
        setDiskWriteTime(diskWriteTime);

        this.pathDumper = TMCProperties.getProperties().getProperty("path_dumper");
        this.extensionFile = TMCProperties.getProperties().getProperty("extension_file");
        try
        {
          this.timeLimit = new Long(TMCProperties.getProperties().getProperty("time_limit")).longValue();
        }
        catch (NumberFormatException nfe) {
          logger.error("Error when is trying to format timeLimit");
          logger.error(nfe);
          nfe.printStackTrace();
        }
        try
        {
          this.timeDelta = new Long(TMCProperties.getProperties().getProperty("time_delta")).longValue();
        }
        catch (NumberFormatException nfe) {
          logger.error("Error when is trying to format timeDelta");
          logger.error(nfe);
          nfe.printStackTrace();
        }
        try
        {
          this.checkStartTime = new Long(TMCProperties.getProperties().getProperty("check_start_time")).longValue();
        }
        catch (NumberFormatException nfe) {
          logger.error("Error when is trying to format checkStartTime");
          logger.error(nfe);
          nfe.printStackTrace();
        }

        if (logger.isDebugEnabled()) {
          logger.debug("pathDumper=" + this.pathDumper);
          logger.debug("extensionFile=" + this.extensionFile);
          logger.debug("timeLimit=" + this.timeLimit);
          logger.debug("timeDelta=" + this.timeDelta);
          logger.debug("checkStartTime=" + this.checkStartTime);
        }
    }

    /**
     * Gets the TMC properties
     *
     * @return The TMC properties
     */
    public TMCProperties getTmcProperties() {
        return tmcProperties;
    }

    /**
     * Sets the TMC properties
     *
     * @param tmcProperties The TMC properties
     */
    public void setTmcProperties(TMCProperties tmcProperties) {
        this.tmcProperties = tmcProperties;
    }

    /**
     * Sets the channel name
     *
     * @param channelName The channel name
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Sets the data list
     *
     * @param dataList The dataList
     */
    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }

    /**
     * Sets the writer stats
     *
     * @param writerStats The writer stats
     */
    public void setWriterStats(TMCProducerStats writerStats) {
        this.writerStats = writerStats;
    }

    /**
     * Sets the disk write time
     *
     * @param diskWriteTime The disk write time
     */
    public void setDiskWriteTime(TMCTimeStatistic diskWriteTime) {
        this.diskWriteTime = diskWriteTime;
    }

    /**
     * Sets the startupDate
     *
     * @param The startupDate
     */
    public void setStartupDate(String startupDate) {
        this.startupDate = TMCTimeConverter.toDate(startupDate, TMCConstants.YYYY_MM_DD);
    }

    /**
     * Write the data list to text file
     */
    public void writeData() {
        long startTime = System.nanoTime();

        if (dataList != null && !dataList.isEmpty()) {
            if (logger.isDebugEnabled())
              logger.debug("Thread name: " + Thread.currentThread().getName());

            int dataListSize = dataList.size();
            long timesToCheck = dataListSize/checkStartTime;

            if (logger.isInfoEnabled()) {
                logger.info("dataListSize: " + dataListSize);
                logger.info("checkStartTime: " + checkStartTime);
                logger.info("timesToCheck: " + timesToCheck);
            }

            int startIndex = 0;
            int endIndex = 0;

            if (timesToCheck == 0) {
              endIndex = dataListSize;
              dumpToDisk(startIndex, endIndex);
            }
            else {
              long mod = dataListSize%checkStartTime;
              if (logger.isInfoEnabled())
                  logger.info("mod: " + mod);
              if (mod == 0) {
                for (startIndex=0; endIndex<dataListSize; startIndex+=checkStartTime) {
                  endIndex = (int) (startIndex + checkStartTime);
                  dumpToDisk(startIndex, endIndex);
                }
              }
              else {
                for (startIndex=0; endIndex<dataListSize; startIndex+=checkStartTime) {
                  if ((startIndex + checkStartTime) <= dataListSize)
                    endIndex = (int) (startIndex + checkStartTime);
                  else
                    endIndex = dataListSize;
                  dumpToDisk(startIndex, endIndex);
                }
              }
            }
        }
        else {
          logger.error("From writeData: dataList is empty. This is strange.");
        }

        long endTime = System.nanoTime();
        if (diskWriteTime != null)
           diskWriteTime.addTime((endTime - startTime) / 1000L);
    }

    /**
     * Dumps the data to disk
     * 
     * @param start The start time
     * @param end The end time
     */
    private void dumpToDisk(int start, int end) {
        String fileName = getFileName();
        String currentStartTime = getStartTime((String) dataList.get(start));
        String stringFolder = getFolderName(currentStartTime);
        String fullFileName = getFullFileName(stringFolder, fileName);
        String bufferString = getDataListAsString(start, end, fullFileName);
        writeFile(stringFolder, fullFileName, bufferString);

        if (logger.isInfoEnabled()) {
          logger.info("Dumping to disk start: " + start + " end: " + end);
          logger.info("fileName: " + fileName);
          logger.info("currentStartTime: " + currentStartTime);
          logger.info("stringFolder: " + stringFolder);
          logger.info("fullFileName: " + fullFileName);
          logger.info("bufferString: ");
          logger.info(bufferString);
        }
    }

    /**
     * Returns the file name, based in the channel name. For example:
     * when the channel is "TMCS:TOPIC:CONTROL/DV01/IFProc0:ARP_FULL", it returns ARP_FULL.txt 
     *
     * @return The file name
     */
    public String getFileName() {
        if (channelName != null && !"".equals(channelName.trim())) {
            String[] channelNameArray = channelName.split("\\:");
            String monitorPointName = "";

            if (channelNameArray != null && channelNameArray.length == 4)
              monitorPointName = channelNameArray[3];
            else if (channelNameArray == null)
              logger.error("From the getFileName, channelNameArray is null. That is strange");
            else
              logger.error("From the getFileName, channelNameArray.length(" + channelNameArray.length + ") is not 4. That is strange");

            return monitorPointName + "." + extensionFile;
        }
        else {
            logger.error("channelName is empty. This is strange.");
        }

        return null;
    }

    /**
     * Gets the startime. Searchs the startime from first element of the list
     *
     * @param data The data or item of the list. For example
     *             "2014-09-08T20:22:05.512|136295005255105360;136295005455103040;1410207735188;136295005276887140|0|136295005326887140|0|136295005376887140|0|136295005426887140|0\n;0.0"
     * @return The starttime like as string in YYYY-MM-DD format. For example: 2014-09-08
     */
    public String getStartTime(String data) {
        if (data != null && !"".equals(data.trim())) {
          String[] fields = data.split("\\;");

          if (fields != null && fields.length == 5) {
            String[] startFields = fields[0].split("\\|");
            if (startFields != null && startFields.length > 0) {
              String startTime = startFields[1];
              return TMCTimeConverter.toYYYYMMDD(new Long(startTime).longValue());
            }

            if (startFields == null)
              logger.error("From the getStartTime, startFields is null. That is strange");
            else
              logger.error("From the getStartTime, startFields.length is less than zero. That is strange");
          }
          else if (fields == null)
            logger.error("From the getStartTime, fields is null. That is strange");
          else
            logger.error("From the getStartTime, fields.length(" + fields.length + ") is not " + 5 + ". That is strange");
        }
        else {
          logger.error("data is empty. This is strange.");
        }

        return null;
    }

    /**
     * Returns the folder name
     *
     * @param yearMonthDay Date in YYYYMMDD format
     * @return The folder name in format <PATH_DUMPER>/<YYYY>/<MM>/<YYYY-MM-DD>/<TOPIC>/<COMPONENT_NAME>
     *         For example: /var/opt/alma/monitordata/current/2014/08/2014-09-08/CONTROL_DV01_IFProc0
     */
    public String getFolderName(String yearMonthDay) {
        if (yearMonthDay != null && !"".equals(yearMonthDay.trim()) && channelName != null && !"".equals(channelName.trim())) {
            String yyyy = yearMonthDay.substring(0, 4);
            String mm = yearMonthDay.substring(5, 7);
            String[] channelNameArray = channelName.split("\\:");
            String componentName = "";
	    String topicName ="";

            if (channelNameArray != null && channelNameArray.length == 4){
              componentName = channelNameArray[2];
	      topicName = channelNameArray[1].replaceAll("/", "_");
            }else if (channelNameArray == null)
              logger.error("From the getFolderName, channelNameArray is null. That is strange");
            else
              logger.error("From the getFolderName, channelNameArray.length(" + channelNameArray.length + ") is not 4. That is strange");
            return pathDumper + "/" + topicName + "/" + yyyy + "/" + mm + "/" + yearMonthDay + "/" + componentName;
        }
        else {
            logger.error("yearMonthDay or channelName is empty. This is strange.");
        }

        return null;
    }

    /**
     * Gets the full file name
     *
     * @param stringFolder The string folder
     * @param fileName The file name
     * @return The full file name. For example:
     *         "/var/opt/alma/monitordata/current/2014/08/2014-09-08/CONTROL_DV01_IFProc0/VF_CTL_MON.txt"
     */
    public String getFullFileName(String stringFolder, String fileName) {
        return stringFolder + "/" + fileName;
    }

    /**
     * Gets the data list as a string.
     * For each data in the list, put the element in the buffer and returns it
     * 
     * @param start The start index of the data list
     * @param end The end index of the data list
     * @param fullFileName The full file name
     * @return The data list as a string
     */
    public String getDataListAsString(int start, int end, String fullFileName) {
        StringBuffer buffer = null;

        if (dataList != null && !dataList.isEmpty()) {
          List<String> dataList = this.dataList.subList(start, end);
          buffer = new StringBuffer();
          for (String data : dataList) {
            String[] fields = data.split("\\;");

            if (fields != null && fields.length == 5) {
              String clob = fields[3];
              String[] clobArray = clob.replaceAll("\n", "|").split("\\|");

              if (clobArray != null && clobArray.length > 1) {
                String previousTime = null;
                long previousTimeLong = -1;
                for (int i=0; i<clobArray.length-1; i+=2) {
                  try {
                    long currentTimeStamp = new Long(clobArray[i]).longValue();

                    if (TMCTimeConverter.isTimeVeryOld(currentTimeStamp, timeLimit)) {
                      long transformedTimeStamp = currentTimeStamp + timeDelta;
                      String reason = "The clob time is very old. Bad data, transforming it. currentTimeStamp=" + currentTimeStamp + ";" + "transformedTimeStamp=" + transformedTimeStamp + ";" + "channelName=" + channelName;
                      if (logger.isInfoEnabled())
                        logger.info(reason);
                      currentTimeStamp = transformedTimeStamp;
                    }
                    else {
                      if (logger.isDebugEnabled())
                        logger.debug("The clob time is normal");
                    }

                    String currentTime = TMCTimeConverter.toDateReadableFormatMillisecondShort(currentTimeStamp);
                    if (currentTime != null && currentTime.length() == 23) {
                      try {
                        String row = currentTime;
                        String values = clobArray[(i + 1)];
                        String[] valueArray = values.split(" ");
                        if (valueArray != null && valueArray.length > 0) {
                          for (String value : valueArray) {
                            String lastCharacter = value.substring(value.length() - 1);
                            if ("-".equals(lastCharacter))
                              value += "E";
                            Double valueDouble = Double.parseDouble(value);
                            if (valueDouble < Long.MIN_VALUE || valueDouble > Long.MAX_VALUE)
                              value = "" + valueDouble.doubleValue();
                            row += " " + value;
                          }
                        }
                        else {
                          String lastCharacter = values.substring(values.length() - 1);
                          String value = null;
                          if ("-".equals(lastCharacter))
                            value += "E";
                          Double valueDouble = Double.parseDouble(value);
                          if (valueDouble < Long.MIN_VALUE || valueDouble > Long.MAX_VALUE)
                            value = "" + valueDouble.doubleValue();
                          row += " " + value;
                        }
                        row += "\n";
                        buffer.append(row);
                      }
                      catch (NumberFormatException nfe) {
                        String msg = "Problems trying to convert clobArray[" + (i + 1) + "] to a valid number. " +
                                     "The element " + clobArray[(i + 1)] + " will be discarded by wrong-formed value. channelName: " + channelName + " fullFileName:" + fullFileName;
                        logger.error(msg);
                        logger.error(nfe);
                      }
                    }
                    else {
                      String msg = "Problems trying to convert currentTime[" + currentTime + "] to a valid timestamp. " +
                                   "The element " + currentTime + " will be discarded by wrong-formed time. channelName: " + channelName + " fullFileName:" + fullFileName;
                      logger.error(msg);
                    }

                    // currentTimeStamp < previousTimeLong
                    if (previousTimeLong != -1 && currentTimeStamp != -1 && currentTimeStamp < previousTimeLong) {
                      String msg = "Out of sequence. previousTime[" + previousTimeLong + " " + previousTime + "] is more than currentTime[" + currentTimeStamp + " " + currentTime + "]. " +
                                   "However, the data will be stored. channelName: " + channelName + " fullFileName:" + fullFileName;
                      logger.error(msg);
                    }
                    // currentTimeStamp == previousTimeLong
                    else if (previousTimeLong != -1 && currentTimeStamp != -1 && currentTimeStamp == previousTimeLong) {
                      String msg = "Repeated sequence. previousTime[" + previousTimeLong + " " + previousTime + "] is equal to currentTime[" + currentTimeStamp + " " + currentTime + "]. " +
                                   "However, the data will be stored. channelName: " + channelName + " fullFileName:" + fullFileName;
                      logger.error(msg);
                    }

                    previousTime = currentTime;
                    previousTimeLong = currentTimeStamp;
                  }
                  catch (ArrayIndexOutOfBoundsException aiobe) {
                    logger.error("Problems to get clobArray[" + i + "] or clobArray[" + (i + 1) + "] when clobArray.length=" + clobArray.length);
                    logger.error(aiobe);
                    aiobe.printStackTrace();
                  }
                  catch (NumberFormatException numberFormatException) {
                    logger.error("Problems to cast to Long the clobArray[" + i + "] when clobArray[" + i + "]=" + clobArray[i]);
                    logger.error(numberFormatException);
                    numberFormatException.printStackTrace();
                  }
                }
              }
              else if (clobArray == null) {
                logger.error("From the getDataListAsString, clobArray is null. That is strange");
              }
              else {
                String msg = "From the getDataListAsString, clobArray.length(" + clobArray.length + ") is less than zero." + "That is strange. channelName=" + channelName + " fullFileName=" + fullFileName + " clob=" + clob;
                logger.error(msg);
              }
            }
            else if (fields == null) {
              logger.error("From the getDataListAsString, fields is null. That is strange");
            }
            else {
              logger.error("From the getDataListAsString, fields.length(" + fields.length + ") is not 5. That is strange");
            }
          }
        }
        else {
          logger.error("From getDataListAsString: dataList is empty. This is strange.");
        }

        return buffer == null ? null : buffer.toString();
    }

    /**
     * Writes the file to disk
     * 
     * @param stringFolder The folder of the path
     * @param fullFileName The full file name
     * @param bufferString The buffer string
     */
    private void writeFile(String stringFolder, String fullFileName, String bufferString) {
        try {
          File outputFolder = new File(stringFolder);
          if (!outputFolder.exists()) {
            outputFolder.mkdirs();
            if (logger.isDebugEnabled())
              logger.debug("Output directory was created");
          }

          PrintWriter outWriter = null;
          FileWriter outFile = null;
          if (bufferString != null && !"".equals(bufferString.trim())) {
            File f = new File(fullFileName);
            if (f.exists()) {
              outFile = new FileWriter(fullFileName, true);
              if (logger.isDebugEnabled())
                logger.debug("File writer was appended");
            }
            else {
              File parentDirectory = f.getParentFile();
              if (!parentDirectory.exists()) {
                  parentDirectory.mkdirs();
                  if (logger.isDebugEnabled())
                    logger.debug("Parent directory was created");
              }
              outFile = new FileWriter(fullFileName);
              if (logger.isDebugEnabled())
                logger.debug("File writer was created");
            }

            outWriter = new PrintWriter(outFile, true);
            outWriter.print(bufferString);
            if (logger.isDebugEnabled()) {
              logger.debug("Out writer was created");
            }
          }
          else {
            logger.error("From writeFile: dataList is empty. This is strange.");
          }

          if (outWriter != null)
            outWriter.close();

          if (outFile != null)
            outFile.close();

          Date currentDate = new Date();
          if (TMCTimeConverter.isOtherDay(currentDate, startupDate)) {
            if (logger.isInfoEnabled())
              logger.info("Is other day. The writerStats will be reseted. startupDate=" + startupDate + " currentDate=" + currentDate);
            startupDate = new Date(currentDate.getTime());
            if (writerStats != null)
              writerStats.reset();
          }

          if (writerStats != null)
            writerStats.addCount();
        }
        catch (IOException ioe) {
          logger.error("Failed to close the file...Is the filesystem full?");
          logger.error(ioe);
          ioe.printStackTrace();
        }
    }
}
