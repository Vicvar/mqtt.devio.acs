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



import org.apache.log4j.Logger;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.InfluxDBFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;

/**
 * This class writes the data to the file
 *
 * @version 1.2
 * @author pmerino@alma.cl
 */
public class TMCInfluxDumper {

    /** The default influxdb url */
    private static final String INFLUX_URL_DEFAULT = "tcp://localhost:8086";

    /** The default retention policy name */
    private static final String A_RETENTION_POLICY_DEFAULT = "aRetentionPolicy";

    /** The default influxdb user */
    private static final String INFLUX_USER_DEFAULT = "root";

    /** The default influxdb password */
    private static final String INFLUX_PASSWORD_DEFAULT = "root";

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
    private static final Logger logger = Logger.getLogger(TMCInfluxDumper.class);

    /** The InfluxDB */ 
    private InfluxDB influxDB;

    /** The TMC properties */
    private TMCProperties tmcProperties;

    /** The writer stats */
    private TMCProducerStats writerStats;

    /** The disk write time */
    private TMCTimeStatistic diskWriteTime;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The influxdb url */
    private String influx_url = INFLUX_URL_DEFAULT;

    /** The retention policy name */
    private String aRetentionPolicy = A_RETENTION_POLICY_DEFAULT;

    /** The influxdb user */
    private String influx_user = INFLUX_USER_DEFAULT;

    /** The influxdb password */
    private String influx_password = INFLUX_PASSWORD_DEFAULT;

    /** The time limit */
    private long timeLimit = TIME_LIMIT_DEFAULT;

    /** The time delta default */
    private long timeDelta = TIME_DELTA_DEFAULT;

    /** Check the start time */
    private long checkStartTime = CHECK_START_TIME;

    /**
     * Constructor
     */
    public TMCInfluxDumper() {
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
    public TMCInfluxDumper(String channelName, List<String> dataList, TMCProperties tmcProperties, TMCProducerStats writerStats, TMCTimeStatistic diskWriteTime, Date startupDate) {
        setChannelName(channelName);
        setDataList(dataList);
        setTmcProperties(tmcProperties);
        setWriterStats(writerStats);
        setDiskWriteTime(diskWriteTime);

	this.influx_url = TMCProperties.getProperties().getProperty("influx_url");
	this.aRetentionPolicy = TMCProperties.getProperties().getProperty("aRetentionPolicy");
	this.influx_user = TMCProperties.getProperties().getProperty("influx_user");
	this.influx_password = TMCProperties.getProperties().getProperty("influx_password");	
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
	setInfluxDBConnection(this.influx_url, this.influx_user, this.influx_password);
	setInfluxDBDatabase(this.getDatabaseName());
	setInfluxDBRetentionPolicy(this.aRetentionPolicy);
	
        if (logger.isDebugEnabled()) {
          logger.debug("influx_url=" + this.influx_url);
          logger.debug("aRetentionPolicy=" + this.aRetentionPolicy);
          logger.debug("influx_user=" + this.influx_user);
          logger.debug("influx_password=" + this.influx_password);
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
     * Set InfluxDB connection
     *t
     */

    public void setInfluxDBConnection(String influx_url, String influx_user, String influx_password) {
        this.influxDB = InfluxDBFactory.connect(influx_url, influx_user, influx_password);	
	
    }

    /**
     * Set InfluxDB database
     *
     * If the database does not exist it is possible to create it, but there are problems with the retention policy
     */

    public void setInfluxDBDatabase(String influx_dbname){
	boolean checkDbExistence = influxDB.databaseExists(influx_dbname);
	if (!checkDbExistence)
		influxDB.createDatabase(influx_dbname);
	this.influxDB.setDatabase(influx_dbname);
    }

    /**
     * Set InfluxDB retention policy
     *
     */

    public void setInfluxDBRetentionPolicy(String aRetentionPolicy){
	this.influxDB.setRetentionPolicy(aRetentionPolicy);
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
     * Write the data in influxdb
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
              dumpToInflux(startIndex, endIndex);
            }
            else {
              long mod = dataListSize%checkStartTime;
              if (logger.isInfoEnabled())
                  logger.info("mod: " + mod);
              if (mod == 0) {
                for (startIndex=0; endIndex<dataListSize; startIndex+=checkStartTime) {
                  endIndex = (int) (startIndex + checkStartTime);
                  dumpToInflux(startIndex, endIndex);
                }
              }
              else {
                for (startIndex=0; endIndex<dataListSize; startIndex+=checkStartTime) {
                  if ((startIndex + checkStartTime) <= dataListSize)
                    endIndex = (int) (startIndex + checkStartTime);
                  else
                    endIndex = dataListSize;
                  dumpToInflux(startIndex, endIndex);
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
    private void dumpToInflux(int start, int end) {
        String currentStartTime = getStartTime((String) dataList.get(start));
        getDataListAsString(start, end);

        if (logger.isInfoEnabled()) {
          logger.info("Dumping to disk start: " + start + " end: " + end);
          logger.info("currentStartTime: " + currentStartTime);
        }
    }

    /**
     * Returns the file name, based in the channel name. For example:
     * when the channel is "TMCS:TOPIC:CONTROL/DV01/IFProc0:ARP_FULL", it returns ARP_FULL 
     *
     * @return The baci name
     */
    public String getMonitorPointName() {
        if (channelName != null && !"".equals(channelName.trim())) {
            String[] channelNameArray = channelName.split("\\:");
            String monitorPointName = "";
            if (channelNameArray != null && channelNameArray.length == 4)
              monitorPointName = channelNameArray[3];
            else if (channelNameArray == null)
              logger.error("From the getMonitorPointName, channelNameArray is null. That is strange");
            else
              logger.error("From the getMonitorPointName, channelNameArray.length(" + channelNameArray.length + ") is not 4. That is strange");
            return monitorPointName;
        }
        else {
            logger.error("channelName is empty. This is strange.");
        }

        return null;
    }

    /**
     * Returns the mqtt topic, based in the channel name. For example:
     * when the channel is "TMCS:TOPIC:CONTROL/DV01/IFProc0:ARP_FULL", it returns TOPIC 
     *
     * @return The database name
     */
    public String getDatabaseName() {
        if (channelName != null && !"".equals(channelName.trim())) {
            String[] channelNameArray = channelName.split("\\:");
            String databaseName = "";
            if (channelNameArray != null && channelNameArray.length == 4)
              databaseName = channelNameArray[1];
            else if (channelNameArray == null)
              logger.error("From the getMonitorPointName, channelNameArray is null. That is strange");
            else
              logger.error("From the getMonitorPointName, channelNameArray.length(" + channelNameArray.length + ") is not 4. That is strange");
            return databaseName;
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
     * Returns the component name, based in the channel name. For example:
     * when the channel is "TMCS:TOPIC:CONTROL/DV01/IFProc0:ARP_FULL", it returns CONTROL_DV01_IFProc0 
     *
     */
    public String getMeasurement() {
        if (channelName != null && !"".equals(channelName.trim())) {
            String[] channelNameArray = channelName.split("\\:");
            String measurement = "";
            if (channelNameArray != null && channelNameArray.length == 4){
              measurement = channelNameArray[2].replace("/","_");
            }else if (channelNameArray == null)
              logger.error("From the getFolderName, channelNameArray is null. That is strange");
            else
              logger.error("From the getFolderName, channelNameArray.length(" + channelNameArray.length + ") is not 4. That is strange");
            return measurement;
        }
        else {
            logger.error("yearMonthDay or channelName is empty. This is strange.");
        }

        return null;
    }

    /**
     * Get values from dataList
     * 
     * @param start The start index of the data list
     * @param end The end index of the data list
     * @return The data list as a string
     */
    public void getDataListAsString(int start, int end) {
	BatchPoints batchPoints = BatchPoints.database(this.getDatabaseName()).tag("async", "true").retentionPolicy(this.aRetentionPolicy).build();
        if (dataList != null && !dataList.isEmpty()) {
          List<String> dataList = this.dataList.subList(start, end);
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
			Double data_value = 0.0;
                        String values = clobArray[(i + 1)];
                        String[] valueArray = values.split(" ");
                        if (valueArray != null && valueArray.length > 0) {
                          for (String value : valueArray) {
                            String lastCharacter = value.substring(value.length() - 1);
                            if ("-".equals(lastCharacter))
                              value += "E";
                            Double valueDouble = Double.parseDouble(value);
                            data_value = valueDouble;
                          }
                        }
                        else {
                          String lastCharacter = values.substring(values.length() - 1);
                          String value = null;
                          if ("-".equals(lastCharacter))
                            value += "E";
                          Double valueDouble = Double.parseDouble(value);
                          data_value = valueDouble;
                        }
			 long time = (currentTimeStamp - 122192928000060000L)/10000;
			 Point point = Point.measurement(getMeasurement()).time(time , TimeUnit.MILLISECONDS).tag("baciName", getMonitorPointName()).addField("value", data_value).build();
	 		 batchPoints.point(point);			
			
                      }
                      catch (NumberFormatException nfe) {
                        String msg = "Problems trying to convert clobArray[" + (i + 1) + "] to a valid number. " +
                                     "The element " + clobArray[(i + 1)] + " will be discarded by wrong-formed value. channelName: " + channelName;
                        logger.error(msg);
                        logger.error(nfe);
                      }
                    }
                    else {
                      String msg = "Problems trying to convert currentTime[" + currentTimeStamp + "] to a valid timestamp. " +
                                   "The element " + currentTimeStamp + " will be discarded by wrong-formed time. channelName: " + channelName;
                      logger.error(msg);
                    }

                    if (previousTimeLong != -1 && currentTimeStamp != -1 && currentTimeStamp < previousTimeLong) {
                      String msg = "Out of sequence. previousTime[" + previousTimeLong + " " + previousTime + "] is more than currentTime[" + currentTimeStamp + " " + currentTime + "]. " +
                                   "However, the data will be stored. channelName: " + channelName;
                      logger.error(msg);
                    }
                    else if (previousTimeLong != -1 && currentTimeStamp != -1 && currentTimeStamp == previousTimeLong) {
                      String msg = "Repeated sequence. previousTime[" + previousTimeLong + " " + previousTime + "] is equal to currentTime[" + currentTimeStamp + " " + currentTime + "]. " +
                                   "However, the data will be stored. channelName: " + channelName;
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
                String msg = "From the getDataListAsString, clobArray.length(" + clobArray.length + ") is less than zero." + "That is strange. channelName=" + channelName + " clob=" + clob;
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
	this.influxDB.write(batchPoints);
    }

    /**
     * Publish in influxDB
     * 
     */

   /* private void publish(String measurement, long time, String property, Double value) {
    	
	   this.influxDB.write(Point.measurement(measurement)
	 				.time(time, TimeUnit.MILLISECONDS)
					.tag("baciName", property)
					.addField("value", value)
					.build());
	
	if (logger.isDebugEnabled()) {
	  logger.debug("send data to influx");
          logger.debug("measurement=" + measurement + "; time=" + time + "; property=" + property + "; value=" + value);
        }
    }*/
}
