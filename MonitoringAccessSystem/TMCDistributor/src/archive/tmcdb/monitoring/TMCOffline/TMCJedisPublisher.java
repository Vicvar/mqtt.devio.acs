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

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.text.Format;

import java.util.Date;
import java.util.List;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.MemoryUsage;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;

/**
 * Publisher for Redis using Jedis<br>
 *
 * Publish messages in the channel
 *
 * @version 1.1
 * @author pmerino@alma.cl
 */
public class TMCJedisPublisher {
    /** Redis max length list by default */
    private static final long MAX_LENGTH_LIST_DEFAULT = 100;

    /** Redis max length list by default */
    private static final long REMOVE_FROM_LIST_DEFAULT = 50;

    /** Max total threads by default */
    private static final int MAX_TOTAL_THREADS_DEFAULT = 5000;

    /** Thread sleep by default */
    private static final int THREAD_SLEEP_DEFAULT = 1000;

    /** The influxDB status by default */
    private static final String INFLUX_STATUS_DEFAULT = "true";

    /** The archiver status by default */
    private static final String ARCHIVER_STATUS_DEFAULT = "true";

    /** Redis max limit date (600000 milsec = 10 min) */
    private static final int LIMIT_DEFAULT = 600000;

    /** The initial date */
    private static final long INITIAL_DATE_DEFAULT = 134815967990000000L;

    /** The logger */
    private static final Logger logger = Logger.getLogger(TMCJedisPublisher.class);

    /** Jedis singleton connection */
    private final TMCJedisSingleton jedisSingleton = TMCJedisSingleton.getInstance();

    /** The tmc properties */
    private TMCProperties tmcProperties;

    /** The writer stats */
    private TMCProducerStats writerStats;

    /** The disk write time statistic */
    private TMCTimeStatistic diskWriteTime;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The max lenght list */
    private long maxLengthList = MAX_LENGTH_LIST_DEFAULT;

    /** The remove from list */
    private long removeFromList = REMOVE_FROM_LIST_DEFAULT;

    /** The max total threads */
    private int maxTotalThreads = MAX_TOTAL_THREADS_DEFAULT;

    /** The thread sleep */
    private int threadSleep = THREAD_SLEEP_DEFAULT;

    /** The limit */
    private int limit = LIMIT_DEFAULT;

    /** The initial date */
    private long initialDate = INITIAL_DATE_DEFAULT;

    /** The influxDB status */
    private String influxStatus = INFLUX_STATUS_DEFAULT;

    /** The archiver status*/
    private String archiverStatus = ARCHIVER_STATUS_DEFAULT;

    /**
     * Constructor
     */
    public TMCJedisPublisher(TMCProperties tmcProperties) {
        setTmcProperties(tmcProperties);
        if (tmcProperties != null) {
          try {
            this.maxLengthList = new Long(TMCProperties.getProperties().getProperty("redis_max_length_list")).longValue();
          }
          catch (NumberFormatException nfe) {
            logger.error("Error when is trying format maxLengthList");
            logger.error(nfe);
            nfe.printStackTrace();
          }
          try
          {
            this.removeFromList = new Long(TMCProperties.getProperties().getProperty("redis_remove_from_list")).longValue();
          }
          catch (NumberFormatException nfe) {
            logger.error("Error when is trying format removeFromList");
            logger.error(nfe);
            nfe.printStackTrace();
          }
          try
          {
            this.maxTotalThreads = new Integer(TMCProperties.getProperties().getProperty("max_total_threads")).intValue();
          }
          catch (NumberFormatException nfe) {
            logger.error("Error when is trying format maxTotalThreads");
            logger.error(nfe);
            nfe.printStackTrace();
          }
          try
          {
            this.threadSleep = new Integer(TMCProperties.getProperties().getProperty("thread_sleep")).intValue();
          }
          catch (NumberFormatException nfe) {
            logger.error("Error when is trying format ThreadSleep");
            logger.error(nfe);
            nfe.printStackTrace();
          }
          try
          {
            this.limit = new Integer(TMCProperties.getProperties().getProperty("redis_max_limit_date")).intValue();
          }
          catch (NumberFormatException nfe) {
            logger.error("Error when is trying format limit");
            logger.error(nfe);
            nfe.printStackTrace();
          }
          try {
            this.initialDate = new Long(TMCProperties.getProperties().getProperty("initial_date")).longValue();
          }
          catch (Exception e) {
              logger.error("Error reading initialDate");
              logger.error(e);
              e.printStackTrace();
          }
          try {
            this.influxStatus = TMCProperties.getProperties().getProperty("influx_status");
          }
          catch (Exception e) {
              logger.error("Error reading influxStatus");
              logger.error(e);
              e.printStackTrace();
          }
          try {
            this.archiverStatus = TMCProperties.getProperties().getProperty("archiver_status");
          }
          catch (Exception e) {
              logger.error("Error reading archiverStatus");
              logger.error(e);
              e.printStackTrace();
          }
        }

        if (logger.isDebugEnabled()) {
          logger.debug("maxLengthList=" + this.maxLengthList);
          logger.debug("removeFromList=" + this.removeFromList);
          logger.debug("maxTotalThreads=" + this.maxTotalThreads);
          logger.debug("threadSleep=" + this.threadSleep);
          logger.debug("limit=" + this.limit);
          logger.debug("initialDate=" + this.initialDate);
        }
    }

    /**
     * Gets the tmc properties
     *
     * @return The TMC properties
     */
    public TMCProperties getTmcProperties() {
        return tmcProperties;
    }

    /**
     * Sets the tmc properties
     *
     * @return The TMC properties
     */
    public void setTmcProperties(TMCProperties tmcProperties) {
        this.tmcProperties = tmcProperties;
    }

    /**
     * Gets the writer stats
     *
     * @return The writer stats
     */
    public TMCProducerStats getWriterStats() {
        return writerStats;
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
     * Publish a message in the channel
     *
     * @param startTime The start time in ACS long format
     * @param channel The channel
     * @param message The message
     */
    public void publish(long startTime, String channel, String message) {
        if (TMCJedisSingleton.getJedis() == null) {
            return;
        }

        if (!TMCJedisSingleton.getJedis().isConnected()) {
            TMCJedisSingleton.getJedis().disconnect();
            TMCJedisSingleton.getJedis().connect();
        }

        if ((channel != null) && (message != null)) {
            String previousElement = TMCJedisSingleton.getJedis().lindex(channel, -1L);

            TMCJedisSingleton.getJedis().publish(channel, message);

            if (startTime <= initialDate) {
                logger.error("Problem in the startTime. " + startTime + " is less or equals than " + initialDate);
                startTime = System.currentTimeMillis();
            }
            else {
              if (logger.isDebugEnabled())
                  logger.debug("startTime " + startTime + " is more than " + initialDate);
            }
            String currentDateString = TMCTimeConverter.toDateReadableFormatMillisecondShort(startTime);
            if (logger.isDebugEnabled())
                logger.debug("currentDateString " + currentDateString);
            message = currentDateString + "|" + message;

            if (logger.isDebugEnabled()) {
              logger.debug("channel: " + channel + " exists");
            }
            long lengthList = TMCJedisSingleton.getJedis().rpush(channel, new String[] { message }).longValue();

            if (logger.isDebugEnabled()) {
              logger.debug("jedis list length:" + lengthList);
            }

            if ((lengthList >= this.maxLengthList) || (isVeryOld(previousElement, message))) {
              long removeFromListSwap;
              if (lengthList >= this.maxLengthList) {
                removeFromListSwap = this.removeFromList;
                if (logger.isDebugEnabled())
                  logger.debug("removeFromListSwap by maxLength: " + removeFromListSwap);
              }
              else {
                removeFromListSwap = lengthList - 1L;
                if (logger.isDebugEnabled()) {
                  logger.debug("removeFromListSwap by very old: " + removeFromListSwap);
                }
              }
              try {
                ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                int totalThreads = threadBean.getThreadCount();
                int counter = 10;
                while (totalThreads >= this.maxTotalThreads) {
                  logger.error("Too many threads (" + totalThreads + ") have already been spawned, wait until some are freed");
                  try {
                    Thread.sleep(this.threadSleep);
                  }
                  catch (InterruptedException e) {
                    logger.error("Problem trying to dumper to disk");
                    logger.error(e);
                    e.printStackTrace();
                    return;
                  }
                  totalThreads = threadBean.getThreadCount();
                  counter--;
                  if (counter <= 0) {
                    logger.error("Gave up waiting, something must be very wrong, drop data.");
                    return;
                  }
                }

                if (logger.isDebugEnabled()) {
                  logger.debug("There are " + totalThreads + " alive.");
                }

                // TODO: Possible concurrency issue here with the "Monitor Points Force" application.
                List listToRemove = TMCJedisSingleton.getJedis().lrange(channel, 0L, removeFromListSwap - 1L);
                TMCJedisSingleton.getJedis().ltrim(channel, removeFromListSwap, -1L);
		/* here it is published in influx or to the file */
	        if (archiverStatus.equals("true")){
		  TMCAsyncDumper tmcAsyncDumper = new TMCAsyncDumper(channel, listToRemove, this.tmcProperties, this.writerStats, this.diskWriteTime, this.startupDate);
                  Thread newThread = new Thread(tmcAsyncDumper);
                  newThread.start();
                }
	        if (influxStatus.equals("true")){
		   TMCAsyncInfluxDumper tmcAsyncInfluxDumper = new TMCAsyncInfluxDumper(channel, listToRemove, this.tmcProperties, this.writerStats, this.diskWriteTime, this.startupDate);
                   Thread newInfluxThread = new Thread(tmcAsyncInfluxDumper);
                   newInfluxThread.start();
		}
              }
              catch (java.lang.OutOfMemoryError e) {
                  logger.error("Out of memory, cannot spawn a new tread ... ");
                  MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                  MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
                  long memoryUsage = heapUsage.getUsed() / 1048576;
                  long memoryMax = heapUsage.getMax() / 1048576;
                  logger.error("Memory usage: " + memoryUsage + " Mb");
                  logger.error("Memory max: " + memoryMax + " Mb");
                  ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                  int totalThreads = threadBean.getThreadCount();
                  logger.error("Total Threads: " + totalThreads);
                  logger.error("Max Total Threads: " + maxTotalThreads);
                  logger.error("Thread Sleep: " + threadSleep);
                  logger.error(e);
                  e.printStackTrace();
                  return;
              }

            }
            else if (logger.isDebugEnabled()) {
              logger.debug("Dont dump to disk");
              logger.debug("maxLengthList: " + this.maxLengthList);
              logger.debug("lengthList: " + lengthList);
            }

            if (logger.isDebugEnabled())
              logger.debug("{channel: " + channel + " message: " + message + "}");
        }
        else {
            logger.error("channel or message is null. That is strange");
            logger.error("channel: " + channel);
            logger.error("message: " + message);
        }
    }

    /**
     * Indicates if a element is very old or not
     *
	 * @param previousElement The previous element
     * @param newElement The new message
     * @return boolean True if the element is very old, false in other case
     */
    private boolean isVeryOld(String previousElement, String newElement) {
        if (previousElement != null) {
            try {
              Date limitDate = new Date(System.currentTimeMillis() - this.limit);

              String[] fields = previousElement.split("\\;");
              if ((fields != null) && (fields.length > 0)) {
                String[] times = fields[0].split("\\|");
                if ((times != null) && (times.length > 0)) {
                  String publishedTime = times[0];
                  Date publishedTimeDate = TMCTimeConverter.toDate(publishedTime, null);

                  if (logger.isDebugEnabled()) {
                    logger.debug("previousElement=" + previousElement);
                    logger.debug("publishedTimeDate=" + publishedTimeDate);
                    logger.debug("limitDate=" + limitDate);
                  }

                  if ((publishedTimeDate != null) && (publishedTimeDate.compareTo(limitDate) <= 0)) {
                    if (logger.isDebugEnabled())
                      logger.debug("Previous element is very old");
                    return true;
                  }

                  if (newElement != null) {
                    String[] fieldsNew = newElement.split("\\;");
                    if ((fieldsNew != null) && (fieldsNew.length > 0)) {
                      String[] timesNew = fieldsNew[0].split("\\|");
                      if ((timesNew != null) && (timesNew.length > 0)) {
                        String publishedTimeNew = timesNew[0];
                        Date publishedTimeDateNew = TMCTimeConverter.toDate(publishedTimeNew, null);

                        if (logger.isDebugEnabled()) {
                          logger.debug("newElement=" + newElement);
                          logger.debug("publishedTimeDateNew=" + publishedTimeDateNew);
                          logger.debug("publishedTimeDateNew.getDay()=" + publishedTimeDateNew.getDay());
                          logger.debug("publishedTimeDate.getDay()=" + publishedTimeDate.getDay());
                        }

                        if ((publishedTimeDateNew != null) && (publishedTimeDate != null) && (publishedTimeDateNew.getDay() != publishedTimeDate.getDay())) {
                          if (logger.isDebugEnabled())
                            logger.debug("Previous element is other day");
                          return true;
                        }
                      }
                      else {
                        logger.error("From isVeryOld, timesNew is null. That is strange");
                      }
                    }
                    else {
                      logger.error("From isVeryOld, fieldsMessage is null. That is strange");
                    }
                  }
                  else {
                    logger.error("From isVeryOld, newElement is null. That is strange");
                  }
                }
                else {
                  logger.error("From isVeryOld, times is null. That is strange");
                }
              }
              else {
                logger.error("From isVeryOld, fields is null. That is strange");
              }
            }
            catch (ArrayIndexOutOfBoundsException aiobe) {
              logger.error("Error in the isVeryOld method");
              logger.error(aiobe);
              aiobe.printStackTrace();
            }
          }

          return false;
    }
}
