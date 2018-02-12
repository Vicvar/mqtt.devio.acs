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

import org.apache.log4j.Logger;

/**
 * It class intercepts methdos for clob process time stat
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCTimeStatisticImpl implements TMCTimeStatistic {
	/** The logger */
	private static final Logger log = Logger.getLogger(TMCTimeStatisticImpl.class);

	/** The count */
	private static long count;

	/** The max time */
	private static long maxTime;

	/** The min time */
	private static long minTime;

	/** The total time */
	private static long totalTime;

	/** The name of the object */
	private String name;
	
	/**
	 * Constructor
	 *
	 * @param name The name of the object
	 */
	public TMCTimeStatisticImpl(String name) {
		if (log.isInfoEnabled())
			log.info(name + " is initialized");
		this.name = name;
		reset();
	}
	
	/**
	 * Sets the name
	 *
	 * @param The name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Resets the statistic
	 */
	public void reset() {
		count = 0;
		maxTime = 0;
		minTime = 0;
		totalTime = 0;
	}
	
	/**
	 * Gets the count
	 *
	 * @return The count
	 */
	public long getCount() {
		return count;
	}
	
	/**
	 * Sets the count
	 *
	 * @param count The count
	 */
	public void setCount(long count) {
		this.count = count;
	}
	
	/**
	 * Adds time
	 *
	 * @param time The time to add
	 */
	public void addTime(long time) {
		count++;
		totalTime += time;

		//save the max and min
		if (time > maxTime) 
			maxTime = time;

		if (time < minTime) 
			minTime = time;
	}
	
	/**
	 * Gets the maximun time
	 *
	 * @return The maximun time
	 */
	public long getMaxTime() {
		return maxTime;
	}
	
	/**
	 * Sets the max time
	 *
	 * @param maxTime The max time
	 */
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}
	
	/**
	 * Gets the minimun time
	 *
	 * @return The minimun time
	 */
	public long getMinTime() {
		return minTime;
	}
	
	/**
	 * Sets the minimun time
	 *
	 * @param minimunTime Sets the minimun time
	 */
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}
	
	/**
	 * Gets the total time
	 *
	 * @return The total time
	 */
	public long getTotalTime() {
		return totalTime;
	}
	
	/**
	 * Sets the total time
	 *
	 * @param The total time
	 */
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	
	/**
	 * Gets the average time
	 *
	 * @return The average time
	 */
	public double getAverageTime() {
		if (count <= 2) 
			return 0;

		//drop max and min to avoid distorsion
		long realTotalTime = totalTime - maxTime - minTime;
		double average = realTotalTime / (count-2);

		return average;
	}
}
