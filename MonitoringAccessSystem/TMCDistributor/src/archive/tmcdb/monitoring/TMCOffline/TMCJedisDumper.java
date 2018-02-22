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

import java.util.Set;
import java.util.Iterator;
import java.util.Date;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;

/**
 * Dumper from Redis to Text Files<br>
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCJedisDumper {
    /** The application name by default */
    private static final String APPLICATION_NAME_DEFAULT = "TMCS";

    /** The influxDB status by default */
    private static final String INFLUX_STATUS_DEFAULT = "true";

    /** The archiver status by default */
    private static final String ARCHIVER_STATUS_DEFAULT = "true";

    /** The logger */
    private static final Logger logger = Logger.getLogger(TMCJedisDumper.class);

    /** Jedis singleton connection */
    private final TMCJedisSingleton jedisSingleton = TMCJedisSingleton.getInstance();

    /** The tmc properties */
    private TMCProperties tmcProperties;

    /** The writer stats */
    private TMCProducerStats writerStats;

    /** The time statistic */
    private TMCTimeStatistic diskWriteTime;

    /** The startup date */
    private Date startupDate = TMCConstants.STARTUP_DATE_DEFAULT;

    /** The application name */
    private String applicationName = APPLICATION_NAME_DEFAULT;

    /** The influxDB status */
    private String influxStatus = INFLUX_STATUS_DEFAULT;

    /** The archiver status*/
    private String archiverStatus = ARCHIVER_STATUS_DEFAULT;
    /**
     * Sets the startupDate
     *
     * @param The startupDate
     */
    public void setStartupDate(String startupDate) {
        this.startupDate = TMCTimeConverter.toDate(startupDate, TMCConstants.YYYY_MM_DD);
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
     * Sets the writer stats
     *
     * @param writerStats The writer stats
     */
    public void setWriterStats(TMCProducerStats writerStats) {
        this.writerStats = writerStats;
    }

    /**
     * Sets the writer stats
     *
     * @param writerStats The writer stats
     */
    public void setDiskWriteTime(TMCTimeStatistic diskWriteTime) {
        this.diskWriteTime = diskWriteTime;
    }

    /**
     * Constructor.
     *
     * @param tmcProperties The properties
     */
    public TMCJedisDumper(TMCProperties tmcProperties) {
        setTmcProperties(tmcProperties);

        if (tmcProperties != null) {
          this.applicationName = TMCProperties.getProperties().getProperty("application_name");
	  this.influxStatus = TMCProperties.getProperties().getProperty("influx_status");
	  this.archiverStatus = TMCProperties.getProperties().getProperty("archiver_status");
        }
        if (TMCJedisSingleton.getJedis() != null) {
          if (!TMCJedisSingleton.getJedis().isConnected()) {
            TMCJedisSingleton.getJedis().disconnect();
            TMCJedisSingleton.getJedis().connect();
          }

          Set keys = TMCJedisSingleton.getJedis().keys(this.applicationName + "*");

          if (logger.isInfoEnabled()) {
            logger.info("keys.size=" + keys.size());
          }
          Iterator it = keys.iterator();
          while (it.hasNext()) {
            String channel = (String)it.next();
	/*
	* If there is data not published after finishing a previous process, they are published
	* For each baci runs on a different thread
	*/
	    if (this.archiverStatus.equals("true")){
	       TMCSyncDumper dumperArchiver = new TMCSyncDumper(channel, TMCJedisSingleton.getJedis().lrange(channel, 0L, -1L), tmcProperties, this.writerStats, this.diskWriteTime, this.startupDate);
	       dumperArchiver.run();
	    }
	    if (this.influxStatus.equals("true")){
	       TMCSyncInfluxDumper dumperInflux = new TMCSyncInfluxDumper(channel, TMCJedisSingleton.getJedis().lrange(channel, 0L, -1L), tmcProperties, this.writerStats, this.diskWriteTime, this.startupDate);
	       dumperInflux.run();
 	    }
            if (logger.isInfoEnabled()) {
              logger.info("Cleaning channel " + channel);
	      logger.info("influxStatus " + influxStatus);
	      logger.info("archiverStatus " + archiverStatus);
            }
            
          }

          TMCJedisSingleton.getJedis().flushAll();
        }
        else {
            if (logger.isDebugEnabled())
                logger.debug("Jedis is null. This is strange.");
        }
    }
}
