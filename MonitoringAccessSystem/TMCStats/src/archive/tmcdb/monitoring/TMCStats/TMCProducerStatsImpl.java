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
package archive.tmcdb.monitoring.TMCStats;

import java.text.DecimalFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * It class intercepts methdos for clob process time stat
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCProducerStatsImpl implements TMCProducerStats, Runnable {
	/** The logger */
	private static final Logger log = Logger.getLogger(TMCProducerStatsImpl.class);
	
	/** The count */
	private long count;

	/** The name of the object */
	private String name;

	/** The last processed time */
	private long lastProcessedTime;

	/** The last count */
	private long lastCount;

	/** The last average throughput */
	private double lastAverageThroughput;
	
	/**
	 * Constructor
	 *
	 * @param name The name of the object
	 */
	public TMCProducerStatsImpl(String name) {
		if (log.isInfoEnabled())
			log.info("TMCProducerStatsImpl " + name + " is initialized");
		this.name = name;
		reset();
	}
	
	/**
	 * Resets the stats
	 */
	public synchronized void reset() {
		count = 0;
		lastCount = 0;
		lastProcessedTime = System.currentTimeMillis();
	}
	
	/**
	 * Gets the last average throughput
	 *
	 * @return The last average throughput
	 */
	public synchronized double getLastAverageThroughput() {
		return this.lastAverageThroughput;
	}
	
	/**
	 * Gets the count
	 *
	 * @return The count
	 */
	public synchronized long getCount() {
		return count;
	}

	public synchronized void addCount() {
		count++;
	}
	
	/**
	 * Rounds the value two decimals
	 *
	 * @param d The value
	 */
	public double roundTwoDecimals(double d) {
		double rounded = -1;
	    DecimalFormat twoDForm = new DecimalFormat("#.##");
	    
		try {
			rounded = Double.valueOf(twoDForm.format(d));
		}
		catch (java.lang.NumberFormatException e) {
			log.error("NumberFormatException: " + e);
			log.error("Number to format: " + d + " in " + name);
		}

		return rounded;
	}
	
	/**
	 * Run the statistic
	 */
	public void run() { 
		try { 
			while(true) {
				updateAverageThroughput();
				//sleep for 5 seconds
				Thread.sleep(5000);
			} 
		} catch(InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Updates the average throughput
	 */
	public synchronized void updateAverageThroughput() {
		//calculate the average through put (message consumed by the TMCEventProducer/second in a period of one minute)

		long diffTime;
		long diffMessages;
		long currentCount;
		long now;
		Date date = new Date();

		if (count == 0) {
			this.lastAverageThroughput = 0;
			return;
		}

		now = System.currentTimeMillis();
		date.setTime(now);
		currentCount = this.count;
		diffMessages  = currentCount - lastCount;
		this.lastAverageThroughput = ((double)diffMessages / (now - lastProcessedTime)) * 1000.0;
		if (log.isInfoEnabled())
			log.info("Stats " + date + ", throughtput average= " + roundTwoDecimals(lastAverageThroughput) + " diffMessage=" + diffMessages + " diff time=" + (now - lastProcessedTime));
		//update snapshot value for next calculation
		this.lastProcessedTime = now;
		this.lastCount = currentCount;
	}
}
