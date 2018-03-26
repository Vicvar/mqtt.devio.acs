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
package archive.tmcdb.monitoring.TMCOffline.test;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Before;

import archive.tmcdb.monitoring.TMCOffline.TMCTTArchiverImpl;

/**
 * Test for the TMCTTArchiverImpl class
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCTTArchiverImplTest {
    private TMCTTArchiverImpl tmcTTArchiverImpl;

    @Before
    public void setUp() {
        tmcTTArchiverImpl = new TMCTTArchiverImpl(null);
    }

    @Test
    public void testLoadMonitorPointName() {
        tmcTTArchiverImpl.loadMonitorPointName();
        assertNotNull(tmcTTArchiverImpl.getMpResolver());
    }

    @Test
    public void testConnectToJMS() {
        tmcTTArchiverImpl.connectToJMS();
        assertNotNull(tmcTTArchiverImpl.getTopicConnection());
        assertNotNull(tmcTTArchiverImpl.getTopicSession());
        assertNotNull(tmcTTArchiverImpl.getTopic());
        assertNotNull(tmcTTArchiverImpl.getTopicSubscriber());
    }

    @Test
    public void testStartConsumer() {
    }

    @Test
    public void testProduceStats() {
    }

    @Test
    public void testGetQueueSize() {
    }

    @Test
    public void testGetAverageCLOBSize() {
        double result = tmcTTArchiverImpl.getAverageCLOBSize();
        double expectedAverageClobSize = -1;
    }

    @Test
    public void testGetAverageDiskWriteTime() {
        double result = tmcTTArchiverImpl.getAverageDiskWriteTime();
        double expectedAverageClobSize = -1;
    }

    @Test
    public void testGetAverageCLOBProcessTime() {
        double result = tmcTTArchiverImpl.getAverageCLOBProcessTime();
        double expectedAverageCLOBProcessTime = -1;
    }

    @Test
    public void testGetAverageEnqueueThroughput() {
        double result = tmcTTArchiverImpl.getAverageEnqueueThroughput();
        double expectedAverageEnqueueThroughput = -1;
    }

    @Test
    public void testGetAverageDequeueThroughput() {
        double result = tmcTTArchiverImpl.getAverageDequeueThroughput();
        double expectedAverageDequeueThroughput = -1;
    }

    @Test
    public void testGetAverageDropThroughput() {
        double result = tmcTTArchiverImpl.getAverageDropThroughput();
        double expectedAverageDropThroughput = -1;
    }

    @Test
    public void testGetEnqueueCounter() {
        double result = tmcTTArchiverImpl.getEnqueueCounter();
        double expectedEnqueueCounter = -1;
    }

    @Test
    public void testGetDropCounter() {
        double result = tmcTTArchiverImpl.getDropCounter();
        double expectedDropCounter = -1;
    }

    @Test
    public void testGetWriteCounter() {
        double result = tmcTTArchiverImpl.getWriteCounter();
        double expectedWriteCounter = -1;
    }

    @Test
    public void testFlushQueueData() {
        tmcTTArchiverImpl.flushQueueData();
    }

    @Test
    public void testGetSleepTimeForConsumers() {
        double result = tmcTTArchiverImpl.getSleepTimeForConsumers();
        double expectedSleepTimeForConsumers = -1;
    }

    @Test
    public void testGetFlushQueueCounter() {
        double result = tmcTTArchiverImpl.getFlushQueueCounter();
        double expectedFlushQueueCounter = -1;
    }

    @Test
    public void testAdjustSleepTimeForConsumers() {
    }

    @Test
    public void testCleanCounters() {
    }

    @Test
    public void testGetFactory() {
    }
}
