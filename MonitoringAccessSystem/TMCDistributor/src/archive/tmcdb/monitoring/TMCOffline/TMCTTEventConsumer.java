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

import java.util.Hashtable;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.log4j.Logger;

import java.lang.NumberFormatException;

import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;
import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCSizeStats;

import alma.tmcdb.utils.MonitorPointNameResolver;

import redis.clients.jedis.exceptions.JedisDataException;

/**
 * It class consumes ActiveMQ messages and writes the output
 * in Redis NonSQL<br>
 *
 * This class run like as Java Thread
 *
 * @version 2.0
 * @author pmerino@alma.cl
 */
public class TMCTTEventConsumer implements TMCEventConsumer {
    /** The monitor point name by default */
    private static final String MP_NAME_DEFAULT = "UNSET";

    /** The monitor point id by default */
    private static final String MP_ID_DEFAULT = "UNSET";

    /** The unknown monitor point name by default */
    private static final String UNKNOWN_MP_NAME_DEFAULT = "UNKNOWN";

    /** The topic name */
    private String topicName = "TOPIC_NAME_DEFAULT";

    /** The application name by default */
    private static final String APPLICATION_NAME_DEFAULT = "TMCS";

    /** Max permitted CLOB size by default */
    private static final int MAX_CLOB_SIZE_DEFAULT = 1000;

    /** Max time limit by default */
    private static final long TIME_LIMIT_DEFAULT = 135762911990000000L;

    /** The time delta default */
    private static final long TIME_DELTA_DEFAULT = 100000000000000000L;

    /** The invalid clob character */
    private static final String INVALID_CLOB_CHARACTER_DEFAULT = "X";

    /** The logger */
    private static final Logger log = Logger.getLogger(TMCTTEventConsumer.class);

    /** The queue */
    protected LinkedBlockingQueue dataQueue;

    /** Monitor point resolver */
    protected MonitorPointNameResolver mpResolver;

    /** The stats list */
    protected Hashtable statsList;

    /** The sleep time */
    protected int sleepTime = 0;

    /** Monitor point unresolver */
    private Hashtable unresolvedMonitorPoints;

    /** Tje Jedis publisher */
    private TMCJedisPublisher jedisPublisher;

    /** TMC properties */
    private TMCProperties tmcProperties;

    /** The monitor point name */
    private String mpName = MP_NAME_DEFAULT;

    /** The monitor point id */
    private String mpId = MP_ID_DEFAULT;

    /** The monitor point unknown */
    private String unknownMpName = UNKNOWN_MP_NAME_DEFAULT;

    /** The application name */
    private String applicationName = APPLICATION_NAME_DEFAULT;

    /** The max clob size */
    private int maxClobSize = MAX_CLOB_SIZE_DEFAULT;

    /** The time limit */
    private long timeLimit = TIME_LIMIT_DEFAULT;

    /** The time delta default */
    private long timeDelta = TIME_DELTA_DEFAULT;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The invalid clob character */
    private String invalidClobCharacter = INVALID_CLOB_CHARACTER_DEFAULT;

    /**
     * Constructor
     * initializes attributes with the values of  configuration file
     * mpName and mpId are then resolved as a combination of other constants
     */
    public TMCTTEventConsumer(TMCProperties tmcProperties) {
        setTmcProperties(tmcProperties);
        if (tmcProperties != null) {
            mpName = tmcProperties.getProperties().getProperty(TMCConstants.UNSET_VALUE);
            mpId = tmcProperties.getProperties().getProperty(TMCConstants.UNSET_VALUE);
            unknownMpName = tmcProperties.getProperties().getProperty(TMCConstants.UNKNOWN_MONITOR_POINT_NAME);
            topicName = tmcProperties.getProperties().getProperty(TMCConstants.TOPIC_NAME);
            applicationName = tmcProperties.getProperties().getProperty(TMCConstants.APPLICATION_NAME);

            try {
                maxClobSize = (new Integer(tmcProperties.getProperties().getProperty(TMCConstants.MAX_CLOB_SIZE))).intValue();
            }
            catch (NumberFormatException nfe) {
                log.error("Error when is trying to transform maxClobSize");
                log.error(nfe);
                nfe.printStackTrace();
            }

            try {
                timeLimit = (new Long(tmcProperties.getProperties().getProperty(TMCConstants.TIME_LIMIT))).longValue();
            }
            catch (NumberFormatException nfe) {
                log.error("Error when is trying to transform timeLimit");
                log.error(nfe);
                nfe.printStackTrace();
            }

            try {
                timeDelta = (new Long(tmcProperties.getProperties().getProperty(TMCConstants.TIME_DELTA))).longValue();
            }
            catch (NumberFormatException nfe) {
                log.error("Error when is trying to transform timeDelta");
                log.error(nfe);
                nfe.printStackTrace();
            }

            if (log.isDebugEnabled()) {
                log.debug("mpName=" + mpName);
                log.debug("mpId=" + mpId);
                log.debug("unknownMpName=" + unknownMpName);
                log.debug("topicName=" + topicName);
                log.debug("applicationName=" + applicationName);
                log.debug("maxClobSize=" + maxClobSize);
                log.debug("timeLimit=" + timeLimit);
                log.debug("timeDelta=" + timeDelta);
            }
        }
    }

    /**
     * Gets the dataQueue reference
     *
     * @return The dataQueue reference
     */
    @Override
    public LinkedBlockingQueue getDataQueue() {
        return dataQueue;
    }

    /**
     * Sets the dataQueue reference
     *
     * @param dataQueue The dataQueue reference
     */
    @Override
    public void setDataQueue(LinkedBlockingQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    /**
     * Gets the mpResolver reference
     *
     * @return The mpResolver reference
     */
    @Override
    public MonitorPointNameResolver getMpResolver() {
        return mpResolver;
    }

    /**
     * Sets the mpResolver reference
     *
     * @param mpResolver The mpResolver reference
     */
    @Override
    public void setMpResolver(MonitorPointNameResolver mpResolver) {
        this.mpResolver = mpResolver;
    }

    /**
     * Gets the stats list
     *
     * @return The stats list
     */
    @Override
    public Hashtable getStatsList() {
        return this.statsList;
    }

    /**
     * Sets the stats list
     *
     * @param The stats  list
     */
    @Override
    public void setStatsList(Hashtable statsList) {
        this.statsList = statsList;
    }

    /**
     * Gets the Jedis Publisher
     *
     * @return The Jedis Publisher
     */
    @Override
    public Object getPublisher() {
        return jedisPublisher;
    }

    /**
     * Sets the Jedis Publisher
     *
     * @param jedisPublisher The jedis publisher
     */
    public void setJedisPublisher(TMCJedisPublisher jedisPublisher) {
        this.jedisPublisher = jedisPublisher;
    }

    /**
     * Sets the sleep time
     *
     * @param millisecond The millisecond to set
     */
    @Override
    public void setSleepTime(int millisecond) {
        this.sleepTime = millisecond;
    }

    /**
     * Gets the sleep time
     *
     * @return The millisecond
     */
    @Override
    public int getSleepTime() {
        return sleepTime;
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
     * @param The TMC properties
     */
    public void setTmcProperties(TMCProperties tmcProperties) {
        this.tmcProperties = tmcProperties;
    }

    /**
     * Gets the unresolvedMonitorPoints reference
     *
     * @return unresolvedMonitorPoints The unresolvedMonitorPoints reference
     */
    public Hashtable getUnresolvedMonitorPoints() {
        return unresolvedMonitorPoints;
    }

    /**
     * Sets the unresolvedMonitorPoints reference
     *
     * @param unresolvedMonitorPoints The unresolvedMonitorPoints reference
     */
    public void setUnresolvedMonitorPoints(Hashtable unresolvedMonitorPoints) {
        this.unresolvedMonitorPoints = unresolvedMonitorPoints;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try
        {
          while (true)
          {
            long start = System.nanoTime();
            insertLastMonitorPointData((MapMessage)this.dataQueue.take());
            long stop = System.nanoTime();

            Date currentDate = new Date();
            if (TMCTimeConverter.isOtherDay(currentDate, this.startupDate)) {
              if (log.isInfoEnabled())
                log.info("Is other day. The clobProcessTime and consumerStats will be reseted. startupDate=" + this.startupDate + " currentDate=" + currentDate);
              this.startupDate = new Date(currentDate.getTime());
              if ((this.statsList != null) && (!this.statsList.isEmpty())) {
                TMCTimeStatistic clobProcessTime = (TMCTimeStatistic)this.statsList.get("ClobProcessTime");
                if (clobProcessTime != null)
                  clobProcessTime.reset();
                TMCProducerStats consumerStats = (TMCProducerStats)this.statsList.get("ConsumerStats");
                if (consumerStats != null) {
                  consumerStats.reset();
                }
              }
            }

            if ((this.statsList != null) && (!this.statsList.isEmpty())) {
              TMCTimeStatistic clobProcessTime = (TMCTimeStatistic)this.statsList.get("ClobProcessTime");
              if (clobProcessTime != null) {
                clobProcessTime.addTime((stop - start) / 1000L);
              }
              else if (log.isDebugEnabled()) {
                log.debug("Clob process time does not exist");
              }

              TMCProducerStats consumerStats = (TMCProducerStats)this.statsList.get("ConsumerStats");
              if (consumerStats != null) {
                consumerStats.addCount();
              }
              else if (log.isDebugEnabled()) {
                log.debug("Consumer stats does not exist");
              }
            }

            if (log.isDebugEnabled()) {
              log.debug("queque size from TMCEventConsumer: " + this.dataQueue.size());
            }
            Thread.sleep(this.sleepTime);
          }
        }
        catch (InterruptedException iex) {
          log.error("InterruptedException exception: " + iex);
          iex.printStackTrace();
        }
        catch (Exception e) {
          log.error("Unexception exception: " + e);
          e.printStackTrace();
        }
    }

    /**
     * Insert the last monitor point data of a message
     *
     * @param data The message
     */
    private void insertLastMonitorPointData(MapMessage data) {
        Date date = new Date();
        String[] temp = null;
        String tempTime = null;

        float averageValue = -1.0F;
        int count = 0;
        float totalValue = 0.0F;
        long totalTime = 0L;
        long averageTimestamp = -1L;

        boolean errorParsingClob = false;
        try
        {
          String componentName = data.getString("componentName");
          String propertyName = data.getString("propertyName");
          int indexInt;
          String indexString;
          try {
            indexInt = data.getInt("index");
            indexString = new Integer(indexInt).toString();
          }
          catch (NumberFormatException nfe) {
            log.error("Error when insertLastMonitorPointData is trying to transform indexInt");
            log.error(nfe);
            nfe.printStackTrace();
            return;
          }
          /* here is set mpName, later it will represent the name of the generated file   */
          this.mpName = propertyName+"_"+indexInt;
          if (this.unknownMpName.equalsIgnoreCase(this.mpName)) {
            String reason = "Unknown monitor point name. Bad data, drop it. componentName=" + componentName + ";" + "baci=" + propertyName + ";" + "index=" + indexString + ";" + "monitorPointName=" + this.mpName;

            if (log.isInfoEnabled())
              log.info(reason);
            if (this.unresolvedMonitorPoints.get(propertyName + "_" + indexString) == null) {
              this.unresolvedMonitorPoints.put(propertyName + "_" + indexString, reason);
            }
            return;
          }

          if (log.isDebugEnabled()) {
            log.debug("Monitor point name resolved sucessfully " + this.mpName);
          }

          if ((componentName != null) && (componentName.contains("AOSTiming"))) {
            componentName = componentName + "_" + data.getString("serialNumber");
          }	
	  /**
          * here is set mpId, this corresponds to the redis channel, example "TMCS:tmc:System/Subsystem/SensorTag_01:baci_0"
	  * where ":" will be used as a separator to generate an array an array of 4 elements
          * If the amount of ":" is modified, it will be necessary to modify the Dumper codes
	  */
          this.mpId = TMCUtils.generateKey(this.applicationName, topicName + ":" + componentName, this.mpName);

          String clob = data.getString("clob");
          temp = clob.replaceAll("\n", "|").split("\\|");
          for (int i = temp.length - 1; i > 0; i -= 2) {
            try {
              totalValue = (float)(totalValue + Double.parseDouble(temp[i]));
            }
            catch (NumberFormatException nfe) {
              String reason = "Error parsing the value. componentName=" + componentName + ";" + "baci=" + propertyName + ";" + "index=" + indexString + ";" + "monitorPointName=" + this.mpName + ";" + "value=" + temp[i] + ";" + "clob=" + clob;

              if (log.isInfoEnabled())
                log.info(reason);
              if ("".equals(temp[i])) {
                errorParsingClob = true;
                break;
              }
            }
            catch (ArrayIndexOutOfBoundsException aibe) {
              log.error("Error of index of bound when parse the value");
              log.error("i=" + i + " temp.length=" + temp.length);
              log.error(aibe);
              aibe.printStackTrace();
            }
            try
            {
              tempTime = temp[(i - 1)];
              totalTime += Long.parseLong(temp[(i - 1)]);
            }
            catch (NumberFormatException nfe) {
              String reason = "Error parsing the time. componentName=" + componentName + ";" + "baci=" + propertyName + ";" + "index=" + indexString + ";" + "monitorPointName=" + this.mpName + ";" + "time=" + temp[(i - 1)] + ";" + "clob=" + clob;

              log.error(reason);
              errorParsingClob = true;
              break;
            }
            catch (ArrayIndexOutOfBoundsException aibe) {
              log.error("Error of index of bound when parse the time");
              log.error("(i-1)=" + (i - 1) + " temp.length=" + temp.length);
              log.error(aibe);
              aibe.printStackTrace();
            }

            count++;
            if (count >= 10)
            {
              break;
            }
          }
          if (errorParsingClob) {
            if (log.isInfoEnabled()) {
              log.info("The clob has errors. So, the clob will be transformed");
              log.info("oldClob=" + clob);
            }
            clob = replaceClob(clob);
            if (log.isInfoEnabled()) {
              log.info("newClob=" + clob);
            }
          }

          if (!"".equals(clob)) {
            if (count > 0) {
              averageValue = totalValue / count;
              averageTimestamp = totalTime / count;
              averageTimestamp = (averageTimestamp - 122192928000000000L) / 10000L;
            }

            long startTime = data.getLong("startTime");
            long endTime = data.getLong("endTime");

            String key = this.mpId;
            String value = startTime + ";" + endTime + ";" + averageTimestamp + ";" + clob + ";" + averageValue;

            if (TMCTimeConverter.isTimeVeryOld(startTime, this.timeLimit)) {
              long transformedStartTime = startTime + this.timeDelta;

              String reason = "The startTime is very old. Bad date, transforming it. componentName=" + componentName + ";" + "baci=" + propertyName + ";" + "index=" + indexString + ";" + "monitorPointName=" + this.mpName + ";" + "startTime=" + startTime + ";" + "transformedStartTime=" + transformedStartTime + ";" + "endTime=" + endTime + ";" + "averageTimestamp=" + averageTimestamp + ";" + "clob=" + clob + ";" + "averageValue=" + averageValue;

              if (log.isInfoEnabled()) {
                log.info(reason);
              }
              startTime = transformedStartTime;
            }

            try
            {
              this.jedisPublisher.publish(startTime, key, value);
            }
            catch (JedisDataException jde) {
              log.error("From insertLastMonitorPointData. Failed to publish to Redis");
              log.error(jde);
              jde.printStackTrace();
              return;
            }

            if ((this.statsList != null) && (!this.statsList.isEmpty())) {
              TMCSizeStats clobSize = (TMCSizeStats)this.statsList.get("ClobSize");
              if (clobSize != null)
                clobSize.addSize(clob.length());
            }
          }
          else {
            String reason = "The clob is empty. This will be removed. componentName=" + componentName + ";" + "baci=" + propertyName + ";" + "index=" + indexString + ";" + "monitorPointName=" + this.mpName + ";" + "clob=" + clob;

            log.error(reason);
          }
        }
        catch (JMSException e) {
          log.error("Failed to update monitoring points " + e);
          e.printStackTrace();
          return;
        }
    }

    /**
     * Sets the startupDate
     *
     * @param The startupDate
     */
    public void setStartupDate(String startupDate) {
        this.startupDate = TMCTimeConverter.toDate(startupDate, TMCConstants.YYYY_MM_DD);
        log.info("startupDate=" + startupDate);
    }

    /**
     * Replace the clob, when it has problem
     * 
     * @param clob The clob to replace
     * @return The replaced clob
     */
    public String replaceClob(String clob) {
        String newClob = "";
        clob = clob.replaceAll("\n", "|");
        String[] temp = clob.split("\\|");
        for (int i = 0; i < temp.length; i += 2) {
          String time = temp[i];
          if (i + 1 < temp.length) {
            String value = temp[(i + 1)];
            if ((!"".equals(time)) && (!"".equals(value))) {
              if ("".equals(newClob))
                newClob = newClob + time + "|" + value;
              else
                newClob = newClob + "|" + time + "|" + value;
            }
          }
        }
        if (!"".equals(newClob))
          newClob = newClob + "\n";
        return newClob;
    }
}
