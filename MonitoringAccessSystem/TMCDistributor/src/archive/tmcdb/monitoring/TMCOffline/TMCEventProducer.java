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

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;

import java.util.Date;

/**
 * This class listeners the messages of ActiveMQ and put the
 * data in the queue data structure
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCEventProducer implements MessageListener {
    /** The monitor point name by default */
    private static final String ANTENNA_TYPE_DEFAULT = ".*";

    /** The boundary value by default */
    private static final double BOUNDARY_DATA_QUEUE_DEFAULT = 0.9;

    /** The max data queue value by default */
    private static final long MAX_DATA_DATA_QUEUE_DEFAULT = 1000000;

    /** The limit value by default */
    private static final long LIMIT_DEFAULT = -1;

    /** The application name by default */
    private static final String APPLICATION_NAME_DEFAULT = "TMCS";
    
    /** The logger */
    private static final Logger log = Logger.getLogger(TMCEventProducer.class);

    /** Linked blocking queue */
    private LinkedBlockingQueue dataQueue;

    /** The trhead name */
    private String threadName;

    /** Producer stats */
    private TMCProducerStats producerStats;

    /** Drop stats */
    private TMCProducerStats dropStats;

    /** Flush stats */
    private TMCProducerStats flushStats;

    /** The antenna type */
    private String antennaType = ANTENNA_TYPE_DEFAULT;

    /** The boundary data queue */
    private double boundaryDataQueue = BOUNDARY_DATA_QUEUE_DEFAULT;

    /** The maximun data queue */
    private long maxDataQueue = MAX_DATA_DATA_QUEUE_DEFAULT;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The limit */
    private long limit = LIMIT_DEFAULT;

    /** The application name */
    private String applicationName = APPLICATION_NAME_DEFAULT;

    /**
     * Constructor
     */
    public TMCEventProducer() {
        this(APPLICATION_NAME_DEFAULT);
    }

    /**
     * Constructor
     * 
     * @param applicationName The application name
     */
    public TMCEventProducer(String applicationName) {
        setApplicationName(applicationName);
    }

    /**
     * Gets the dataQueue reference
     *
     * @return The dataQueue reference
     */
    public LinkedBlockingQueue getDataQueue() {
        return dataQueue;
    }

    /**
     * Sets the dataQueue reference
     *
     * @param The dataQueue reference
     */
    public void setDataQueue(LinkedBlockingQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    /**
     * Gets the producersStats reference
     *
     * @return The producerStats reference
     */
    public TMCProducerStats getProducerStats() {
        return producerStats;
    }

    /**
     * Sets the producerStats reference
     * 
     * @param producerStats The producerStats reference
     */
    public void setProducerStats(TMCProducerStats producerStats) {
        this.producerStats = producerStats;
    }

    /**
     * Gets the dropStats reference
     *
     * @return The dropStats reference
     */
    public TMCProducerStats getDropStats() {
        return dropStats;
    }

    /**
     * Gets the flushStats reference
     *
     * @return The flushStats reference
     */
    public TMCProducerStats getFlushStats() {
        return flushStats;
    }

    /**
     * Sets the flushStats reference
     *
     * @param flushStats The flushStats reference
     */
    public void setFlushStats(TMCProducerStats flushStats) {
        this.flushStats = flushStats;
    }

    /**
     * Sets the dropStats reference
     *
     * @param dropStats The dropStats reference
     */
    public void setDropStats(TMCProducerStats dropStats) {
        this.dropStats = dropStats;
    }

    /**
     * Gets the antenna type
     *
     * @return The antenna type
     */
    public String getAntennaType() {
        return antennaType;
    }

    /**
     * Sets the antenna type
     *
     * @param The antenna type
     */
    public void setAntennaType(String antennaType) {
        this.antennaType = antennaType;
    }

    /**
     * Sets the boundary queue size
     *
     * @param The boundary queue size
     */
    public void setBoundaryDataQueue(double boundaryDataQueue) {
        this.boundaryDataQueue = boundaryDataQueue;
    }

    /**
     * Sets the max queue size
     *
     * @param The max queue size
     */
    public void setMaxDataQueue(long maxDataQueue) {
        this.maxDataQueue = maxDataQueue;
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
     * Sets the applicationName
     *
     * @param The applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * This method receives the message of ApacheActiveMQ
     *
     * @param message The message
     */
    public void onMessage(Message message) {
        this.threadName = Thread.currentThread().getName();

        if (log.isDebugEnabled())
          log.debug("Listening messages from the queue... " + this.threadName);
        try
        {
          MapMessage data = (MapMessage)message;
          String componentName = data.getString("componentName");
          if ((this.antennaType != null) && (componentName != null) && (componentName.matches(this.antennaType))) {
            Long startTime = Long.valueOf(data.getLong("startTime"));

            Date currentDate = new Date();
            if (TMCTimeConverter.isOtherDay(currentDate, this.startupDate)) {
              if (log.isInfoEnabled())
                log.info("Is other day. The producerStats, dropStats and flushStats will be reseted. startupDate=" + this.startupDate + " currentDate=" + currentDate);
              this.startupDate = new Date(currentDate.getTime());
              if (this.producerStats != null) {
                this.producerStats.reset();
              }
              if (this.dropStats != null) {
                this.dropStats.reset();
              }
              if (this.flushStats != null) {
                this.flushStats.reset();
              }
            }

            if (isLimit()) {
              String msg = "Removing messages from the queue. The limit was exceeded from " + this.applicationName + ". " + "queueSize=" + this.dataQueue.size() + " " + "maxDataQueue=" + this.maxDataQueue + " " + "boundaryDataQueue=" + this.boundaryDataQueue + " " + "limit=" + this.limit;

              log.error(msg);

              this.dataQueue.clear();

              if (this.flushStats != null) {
                this.flushStats.addCount();
              }
            }

            if (this.dataQueue.offer(data)) {
              if (this.producerStats != null) {
                this.producerStats.addCount();
              }

            }
            else if (this.dropStats != null) {
              this.dropStats.addCount();
            }

            if (log.isDebugEnabled()) {
              log.debug("threadName=" + this.threadName + "; " + "serialNumber=" + data.getString("serialNumber") + "; " + "propertyName=" + data.getString("propertyName") + "; " + "index=" + data.getInt("index") + "; " + "componentName=" + componentName + "; " + "location=" + data.getString("location") + "; " + "sampleSize=" + data.getLong("sampleSize") + "; " + "startTime=" + startTime + "; " + "endTime=" + data.getLong("endTime") + "; " + "monitorPointName=" + data.getString("monitorPointName") + "; " + "monitorPointId=" + data.getString("monitorPointId") + "; " + "dataQueue.size=" + this.dataQueue.size() + "; " + "dataQueue.remainingCapacity=" + this.dataQueue.remainingCapacity() + "; " + "clob=" + data.getString("clob") + "; ");
            }

          }
          else if (log.isInfoEnabled()) {
            log.info("componentName: " + componentName + " does not match with antennaType: " + this.antennaType);
          }
        }
        catch (JMSException ex) {
          log.error("JMS is failed: " + ex);
          ex.printStackTrace();
        }
    }

    /**
     * If is the limit or not
     * 
     * @return True if is the limit, false in other case
     */
    private boolean isLimit() {
        long queueSize = this.dataQueue.size();
        if (this.limit == LIMIT_DEFAULT)
          this.limit = (long)(this.maxDataQueue * this.boundaryDataQueue);
        return queueSize >= this.limit;
    }
}
