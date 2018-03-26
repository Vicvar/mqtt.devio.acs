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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnectionFactory;

import alma.tmcdb.utils.MonitorPointNameResolver;

import org.exolab.castor.xml.XMLException;

import java.io.IOException;

import org.apache.log4j.Logger;

import archive.tmcdb.monitoring.TMCStats.TMCTimeStatistic;
import archive.tmcdb.monitoring.TMCStats.TMCProducerStats;
import archive.tmcdb.monitoring.TMCStats.TMCSizeStats;

import archive.tmcdb.monitoring.TMCAgent.TMCJMXAgent;

/**
 * This class gets elements of the ActiveMQ and puts those elements
 * on Redis.<br> This class runs like as daemon.<br>
 * This class is a implementation of TMCTTArchiver interface.
 *
 * @version 3.0
 * @author pmerino@alma.cl
 */
public abstract class TMCTTAbstractArchiver implements TMCArchiver, TMCStatsMXBean {
    /** The topic name by default */
    private static final String TOPIC_NAME_DEFAULT = "tmc";

    /** The bean name by default */
    private static final String BEAN_NAME_DEFAULT = "archive.tmcdb.monitoring.TMCOffline:type=TMCTTArchiverImpl";

    /** The logger */
    private static final Logger log = Logger.getLogger(TMCTTAbstractArchiver.class);

    /** The queue */
    protected LinkedBlockingQueue dataQueue;

    /** ActiveMQ factory */
    private ActiveMQConnectionFactory factory;

    /** Topic connection */
    private static TopicConnection topicConnection;

    /** Topic session */
    private static TopicSession topicSession;

    /** Topic subscriber */
    private static TopicSubscriber topicSubscriber;

    /** The topic */
    private static Topic topic;

    /** TMCDB Cache Loader */
    private MonitorPointNameResolver mpResolver;

    /** Unresolver monitor point name */
    private Hashtable unresolvedMonitorPoints;

    /** The TMC properties */
    protected TMCProperties tmcProperties;

    /** The TMC Event Producer */
    protected TMCEventProducer tmcEventProducer;

    /** The event consumer list */
    protected Hashtable tmcEventConsumerList;

    /** The JMX agent */
    protected TMCJMXAgent tmcJmxAgent;

    /** The topic name */
    private String topicName = TOPIC_NAME_DEFAULT;

    /** The bean name */
    private String beanName = BEAN_NAME_DEFAULT;

    /**
     * Constructor
     */
    public TMCTTAbstractArchiver(TMCProperties tmcProperties) {
        setTmcProperties(tmcProperties);
        if (tmcProperties != null) {
            topicName = tmcProperties.getProperties().getProperty(TMCConstants.TOPIC_NAME);
            beanName = tmcProperties.getProperties().getProperty(TMCConstants.BEAN_NAME);
            if (log.isDebugEnabled()) {
                log.debug("topicName=" + topicName);
                log.debug("beanName=" + beanName);
            }
        }
    }

    /**
     * Runs the principal flow
     */
    public void run() {
        loadMonitorPointName();
        connectToJMS();
        registerMBean();
        startConsumer();
        produceStats();
    }

    /**
     * Loads the monitor point names from mpResolver
     */
    public void loadMonitorPointName() {
        try {
            if (log.isInfoEnabled())
                log.info("Loading monitor point name cache ....");

            if (mpResolver != null)
                mpResolver.loadMonitorPointFromXML();

            if (log.isInfoEnabled())
                log.info("Monitor point name resolver is  ready.");
        }
        catch (XMLException ex) {
            log.error("XML Exception when load monitor point name: " + ex);
            ex.printStackTrace();
        }
        catch (IOException ex) {
            log.error("IO Exception when load monitor point name: " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Connecto to JMS
     */
    public void connectToJMS() {
        try {
            if (factory != null) {
                topicConnection = factory.createTopicConnection();
                topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                topic = topicSession.createTopic(topicName);
                topicSubscriber = topicSession.createSubscriber(topic);
            }

            if (topicSubscriber != null && tmcEventProducer != null)
                topicSubscriber.setMessageListener(tmcEventProducer);

            if (topicConnection != null)
                topicConnection.start();

            if (log.isInfoEnabled())
                log.info("Connected to JMS");
        }
        catch (JMSException ex) {
            log.error("Failed to connect to JMS \n" + ex);
            log.error("Exit ...");
            ex.printStackTrace();
        }
    }

    /**
     * Permits register JMX
     */
    public void registerMBean() {
        if (tmcJmxAgent != null)
            tmcJmxAgent.registerMBean(this, tmcProperties.getProperties().getProperty(TMCConstants.BEAN_NAME));
    }

    /**
     * Start the data consumers
     */
    public void startConsumer() {
        // Iteration in the map
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            Enumeration names = tmcEventConsumerList.keys();
            int i = 1;
            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                TMCEventConsumer consumer = (TMCEventConsumer) tmcEventConsumerList.get(key);
                if (consumer != null)
                    new Thread(consumer, "Consumer-0" + i).start();
                i++;
            }
        }

        if (log.isInfoEnabled())
            log.info("TMCTTArchiver is ready, waiting for data...");
    }

    /**
     * Produces the stats
     */
    public abstract void produceStats();

    /**
     * Gets the queue size
     * 
     * @return The queue size
     */
    public long getQueueSize() {
        return dataQueue.size();
    }

    /**
     * Gets the average CLOB size
     * 
     * @return The average CLOB size
     */
    @Override
    public double getAverageCLOBSize() {
        TMCEventConsumer tmcEventConsumer = null;
        double averageSize = -1;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer != null && tmcEventConsumer.getStatsList() != null && tmcEventConsumer.getStatsList().get("ClobSize") != null)
                averageSize = ((TMCSizeStats) tmcEventConsumer.getStatsList().get("ClobSize")).getAverageSize();
        }
        return averageSize;
    }

    /**
     * Gets the average disk write time
     * 
     * @return The average disk write time
     */
    @Override
    public double getAverageDiskWriteTime() {
        TMCEventConsumer tmcEventConsumer = null;
        double averageTime = -1;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer != null && tmcEventConsumer.getStatsList() != null && tmcEventConsumer.getStatsList().get("DiskWriteTime") != null)
                averageTime = ((TMCTimeStatistic) tmcEventConsumer.getStatsList().get("DiskWriteTime")).getAverageTime();
        }
        return averageTime;
    }

    /**
     * Gets the average CLOB process time
     * 
     * @return The average CLOB process time
     */
    @Override
    public double getAverageCLOBProcessTime() {
        TMCEventConsumer tmcEventConsumer = null;
        double averageTime = -1;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer != null && tmcEventConsumer.getStatsList() != null && tmcEventConsumer.getStatsList().get("ClobProcessTime") != null)
                averageTime = ((TMCTimeStatistic) tmcEventConsumer.getStatsList().get("ClobProcessTime")).getAverageTime();
        }
        return averageTime;
    }

    /**
     * Gets the average enqueue throughput
     * 
     * @return The average enqueue throughput
     */
    @Override
    public double getAverageEnqueueThroughput() {
        return tmcEventProducer != null && tmcEventProducer.getProducerStats() != null ? tmcEventProducer.getProducerStats().getLastAverageThroughput():-1;
    }

    /**
     * Gets the average dequeue throughput
     * 
     * @return The average dequeue throughput
     */
    @Override
    public double getAverageDequeueThroughput() {
        TMCEventConsumer tmcEventConsumer = null;
        double averageThroughput = -1;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer != null && tmcEventConsumer.getStatsList() != null && tmcEventConsumer.getStatsList().get("ConsumerStats") != null)
                averageThroughput = ((TMCProducerStats) tmcEventConsumer.getStatsList().get("ConsumerStats")).getLastAverageThroughput();
        }
        return averageThroughput;
    }

    /**
     * Gets the average write throughput
     * 
     * @return The average write throughput
     */
    public abstract double getAverageWriteThroughput();

    /**
     * Gets the average drop throughput
     * 
     * @return The average drop throughput
     */
    @Override
    public double getAverageDropThroughput() {
        return tmcEventProducer != null && tmcEventProducer.getDropStats() != null ? tmcEventProducer.getDropStats().getLastAverageThroughput():-1;
    }

    /**
     * Gets the enqueue counter
     * 
     * @return The enqueue counter
     */
    @Override
    public long getEnqueueCounter() {
        return tmcEventProducer != null && tmcEventProducer.getProducerStats() != null ? tmcEventProducer.getProducerStats().getCount():-1;
    }

    /**
     * Gets the dequeue counter
     * 
     * @return The dequeue counter
     */
    @Override
    public long getDequeueCounter() {
        long counter = -1;

        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            TMCEventConsumer tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer != null && tmcEventConsumer.getStatsList() != null && tmcEventConsumer.getStatsList().get("ConsumerStats") != null)
                counter = ((TMCProducerStats) tmcEventConsumer.getStatsList().get("ConsumerStats")).getCount();
        }

        return counter;
    }

    /**
     * Gets the drop counter
     * 
     * @return The drop counter
     */
    @Override
    public long getDropCounter() {
        return tmcEventProducer != null && tmcEventProducer.getDropStats() != null ? tmcEventProducer.getDropStats().getCount():-1;
    }

    /**
     * Gets the sleep time for consumers
     * 
     * @return The sleep time for consumers
     */
    @Override
    public int getSleepTimeForConsumers() {
        TMCEventConsumer tmcEventConsumer = null;
        int sleepTimeForConsumers = -1;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            sleepTimeForConsumers = tmcEventConsumer.getSleepTime();
        }
        return sleepTimeForConsumers;
    }

    /**
     * Gets the flush counter
     * 
     * @return The flush counter
     */
    @Override
    public long getFlushQueueCounter() {
        return tmcEventProducer != null && tmcEventProducer.getFlushStats() != null ? tmcEventProducer.getFlushStats().getCount():-1;
    }

    /**
     * Adjusts sleep time for all consumers
     * 
     * @param milliseconds The milliseconds to adjust
     */
    @Override
    public void adjustSleepTimeForConsumers(int milliseconds) {
        TMCEventConsumer tmcEventConsumer = null;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty())
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
        if (tmcEventConsumer != null)
            tmcEventConsumer.setSleepTime(milliseconds);
    }

    /**
     * Clean the counters
     */
    @Override
    public void cleanCounters() {
        // Clean the enqueue counter
        if (tmcEventProducer != null && tmcEventProducer.getProducerStats() != null)
            tmcEventProducer.getProducerStats().reset();

        // Clean the drop counter
        if (tmcEventProducer != null && tmcEventProducer.getDropStats() != null)
            tmcEventProducer.getDropStats().reset();

        // Clean the flush queue counter
        if (tmcEventProducer != null && tmcEventProducer.getFlushStats() != null)
            tmcEventProducer.getFlushStats().reset();

        // Clean the write counter
        TMCEventConsumer tmcEventConsumer = null;
        if (tmcEventConsumerList != null && !tmcEventConsumerList.isEmpty()) {
            tmcEventConsumer = (TMCEventConsumer) tmcEventConsumerList.get("Consumer-01");
            if (tmcEventConsumer.getPublisher() != null) {
                TMCJedisPublisher jedisPublisher = (TMCJedisPublisher) tmcEventConsumer.getPublisher();
                if (tmcEventConsumer != null && jedisPublisher != null)
                    jedisPublisher.getWriterStats().reset();
            }
        }
    }

    /**
     * Gets the factory reference
     * 
     * @return factory The factory reference
     */
    public ActiveMQConnectionFactory getFactory() {
        return this.factory;
    }

    /**
     * Sets the factory reference
     * 
     * @param factory The factory reference
     */
    public void setFactory(ActiveMQConnectionFactory factory) {
        this.factory = factory;
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
     * @param dataQueue The dataQueue reference
     */
    public void setDataQueue(LinkedBlockingQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    /**
     * Gets the mpResolver reference
     * 
     * @return The mpResolver reference
     */
    public MonitorPointNameResolver getMpResolver() {
        return mpResolver;
    }

    /**
     * Sets the mpResolver reference
     * 
     * @param mpResolver The mpResolver reference
     */
    public void setMpResolver(MonitorPointNameResolver mpResolver) {
        this.mpResolver = mpResolver;
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
     * Gets the tmcEventProducer reference
     * 
     * @return The tmcEventProducer reference
     */
    public TMCEventProducer getTmcEventProducer() {
        return tmcEventProducer;
    }

    /**
     * Sets the tmcEventProducer reference
     * 
     * @param tmcEventProducer The tmcEventProducer reference
     */
    public void setTmcEventProducer(TMCEventProducer tmcEventProducer) {
        this.tmcEventProducer = tmcEventProducer;
    }

    /**
     * Gets the tmcEventConsumerList reference
     * 
     * @return The tmcEventConsumerList reference
     */
    public Hashtable getTmcEventConsumerList() {
        return tmcEventConsumerList;
    }

    /**
     * Sets the tmcEventConsumerList reference
     * 
     * @param tmcEventConsumerList The tmcEventConsumerList reference
     */
    public void setTmcEventConsumerList(Hashtable tmcEventConsumerList) {
        this.tmcEventConsumerList = tmcEventConsumerList;
    }

    /**
     * Gets the properties
     * 
     * @return The properties
     */
    public TMCProperties getTmcProperties() {
        return tmcProperties;
    }

    /**
     * Sets the properties
     * 
     * @param tmcProperties The properties
     */
    public void setTmcProperties(TMCProperties tmcProperties) {
        this.tmcProperties = tmcProperties;
    }

    /**
     * Gets the topic connection
     * 
     * @return The topic connection
     */
    public TopicConnection getTopicConnection() {
        return topicConnection;
    }

    /**
     * Gets the topic session
     * 
     * @return The topic session
     */
    public TopicSession getTopicSession() {
        return topicSession;
    }

    /**
     * Gets the topic subscriber
     * 
     * @return The topic subscriber
     */
    public TopicSubscriber getTopicSubscriber() {
        return topicSubscriber;
    }

    /**
     * Gets the topic
     * 
     * @return The topic
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * Sets the tmc agent
     *
     * @param tmcJmxAgent the tmc agent
     */
    public void setTmcJmxAgent(TMCJMXAgent tmcJmxAgent) {
        this.tmcJmxAgent = tmcJmxAgent;
    }
}
