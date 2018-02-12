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

import java.util.Enumeration;

import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;

import org.apache.log4j.Logger;

/**
 * This class gets elements of the ActiveMQ and puts those elements
 * on Redis.<br> This class runs like as daemon.<br>
 * This class is a implementation of TMCTTArchiver interface.
 *
 * @version 3.0
 * @author pmerino@alma.cl
 */
public class TMCTTArchiverImpl extends TMCTTAbstractArchiver {
    /** The logger */
    private static final Logger log = Logger.getLogger(TMCTTArchiverImpl.class);

    /**
     * Constructor
     */
    public TMCTTArchiverImpl(TMCProperties tmcProperties) {
        super(tmcProperties);
    }

    /**
     * Produces the stats
     */
    @Override
    public void produceStats() {
        if (tmcEventProducer != null && tmcEventProducer.getProducerStats() != null)
            new Thread(tmcEventProducer.getProducerStats(), "ProducerStats").start();

        if (tmcEventProducer != null && tmcEventProducer.getDropStats() != null)
            new Thread(tmcEventProducer.getDropStats(), "DropStats").start();

        // Iteration in the map
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            Enumeration names = tmcEventConsumerList.keys();
            int i = 0;
            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                TMCEventConsumer tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get(key);
                if (tmcEventConsumer != null && tmcEventConsumer.getPublisher() != null) {
                    TMCJedisPublisher jedisPublisher = (TMCJedisPublisher) tmcEventConsumer.getPublisher();
                    new Thread(jedisPublisher.getWriterStats(), "WriterStats").start();

                    TMCProducerStats consumerStats = (TMCProducerStats) tmcEventConsumer.getStatsList().get("ConsumerStats");
                    new Thread(consumerStats, "ConsumerStats").start();
                }
                i++;
            }
        }
    }

    /**
     * Gets the average write throughput
     *
     * @return The average write throughput
     */
    @Override
    public double getAverageWriteThroughput() {
        TMCEventConsumer tmcEventConsumer = null;
        double averageThroughput = -1;

        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer.getPublisher() != null) {
                TMCJedisPublisher jedisPublisher = (TMCJedisPublisher) tmcEventConsumer.getPublisher();
                if (tmcEventConsumer != null && jedisPublisher != null)
                    averageThroughput = jedisPublisher.getWriterStats().getLastAverageThroughput();
            }
        }

        return averageThroughput;
    }

    /**
     * Gets the average write throughput
     *
     * @return The average write throughput
     */
    @Override
    public long getWriteCounter() {
        TMCEventConsumer tmcEventConsumer = null;
        long writeCounter = -1;

        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer.getPublisher() != null) {
                TMCJedisPublisher jedisPublisher = (TMCJedisPublisher) tmcEventConsumer.getPublisher();
                if (tmcEventConsumer != null && jedisPublisher != null)
                    writeCounter = jedisPublisher.getWriterStats().getCount();
            }
        }

        return writeCounter;
    }

    /**
     * Flush the queue data
     */
    @Override
    public void flushQueueData() {
        if (log.isInfoEnabled())
            log.info("Removing messages from the queue. The flush method was executed in the flow.");
            dataQueue.clear();

        if (tmcEventProducer.getDropStats() != null)
            tmcEventProducer.getDropStats().reset();

        if (tmcEventProducer.getFlushStats() != null)
            tmcEventProducer.getFlushStats().addCount();
    }
}
