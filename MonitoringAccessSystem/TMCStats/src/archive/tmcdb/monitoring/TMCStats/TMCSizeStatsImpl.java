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
 * It class intercepts methdos for size stat
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCSizeStatsImpl implements TMCSizeStats {
	/** The logger */
	private static final Logger log = Logger.getLogger(TMCSizeStatsImpl.class);

	/** The count */
	private static long count;

	/** The total time */
	private static long total;

	/** The name of the object */
	private String name;
	
	/**
	 * Constructor
	 *
	 * @param name The name of the object
	 */
	public TMCSizeStatsImpl(String name) {
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
		total = 0;
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
	 * Add the count
	 *
	 * @param count The count
	 */
	public void addCount() {
		this.count++;
	}
	
	/**
	 * Adds size
	 *
	 * @param time The size to add
	 */
	public void addSize(long size) {
		count++;
		total += size;
	}
	
	/**
	 * Gets the total
	 *
	 * @return The total
	 */
	public long getTotal() {
		return total;
	}
	
	/**
	 * Sets the total
	 *
	 * @param The total
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	
	/**
	 * Gets the average size
	 *
	 * @return The average size
	 */
	public double getAverageSize() {
		return count > 0 ? total / count : 0;
	}
}
