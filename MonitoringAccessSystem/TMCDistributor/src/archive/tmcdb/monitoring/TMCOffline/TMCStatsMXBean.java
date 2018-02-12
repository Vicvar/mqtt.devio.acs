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

/**
 * Stats interface
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public interface TMCStatsMXBean {
    public long getQueueSize();
    public int getSleepTimeForConsumers();

    public long getEnqueueCounter(); // Per day
    public long getDropCounter(); // Per day
    public long getDequeueCounter(); // Per day
    public long getFlushQueueCounter(); // Per day
    public long getWriteCounter(); // Per day

    public double getAverageEnqueueThroughput(); // Per day
    public double getAverageDropThroughput(); // Per day
    public double getAverageDequeueThroughput(); // Per day
    public double getAverageWriteThroughput(); // Per day

    public double getAverageCLOBSize();
    public double getAverageDiskWriteTime(); // Per day
    public double getAverageCLOBProcessTime(); // Per day

    public void adjustSleepTimeForConsumers(int millisecond); // Invoked actions by the user
    public void flushQueueData(); // Invoked actions by the user
    public void cleanCounters(); // Invoked actions by the user
}