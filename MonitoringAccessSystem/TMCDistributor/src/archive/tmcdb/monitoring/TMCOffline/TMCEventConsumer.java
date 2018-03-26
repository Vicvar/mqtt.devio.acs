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

import java.lang.Runnable;

import java.util.concurrent.LinkedBlockingQueue;

import alma.tmcdb.utils.MonitorPointNameResolver;

import java.util.Hashtable;

/**
 * Interface for consumers. All consumers should be implements of this class.
 * It groups many functionallity for consumers.
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public interface TMCEventConsumer extends Runnable {
    /**
     * Gets the dataQueue reference
     * 
     * @return The dataQueue reference
     */
    public LinkedBlockingQueue getDataQueue();

    /**
     * Sets the dataQueue reference
     * 
     * @param dataQueue The dataQueue reference
     */
    public void setDataQueue(LinkedBlockingQueue dataQueue);

    /**
     * Gets the mpResolver reference
     *
     * @return The mpResolver reference
     *
     */
    public MonitorPointNameResolver getMpResolver();

    /**
     * Sets the mpResolver reference
     *
     * @param mpResolver The mpResolver reference
     */
    public void setMpResolver(MonitorPointNameResolver mpResolver);

    /**
     * Gets the stats list
     * 
     * @return The stats list
     */
    public Hashtable getStatsList();

    /**
     * Sets the stats list
     *
     * @param The stats  list
     */
    public void setStatsList(Hashtable statsList);

    /**
     * Sets the sleep time
     *
     * @param millisecond The millisecond to set
     */
    public void setSleepTime(int millisecond);

    /**
     * Gets the sleep time
     * 
     * @return The millisecond
     */
    public int getSleepTime();

    /**
     * Run the thread
     */
    public void run();

    /**
     * Gets the publisher
     */
    public Object getPublisher();
}