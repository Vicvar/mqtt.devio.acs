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
package alma.acs.monitoring.blobber;


import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import alma.acs.container.ContainerServices;
import alma.acs.monitoring.DAO.ComponentData;


public class BlobberWatchDogAlmaImpl implements BlobberWatchDog, Runnable
{
	private final ContainerServices containerServices;
	private final Logger logger;
	private final HashMap <String, QueueWithInfo> myQueues;
	private volatile boolean shouldTerminateRunLoop;
	
	private MBeanServer server;
	private ObjectName qBeanName = null;


	public BlobberWatchDogAlmaImpl(ContainerServices containerServices) {
		this.containerServices = containerServices;
		this.logger = containerServices.getLogger();

		myQueues = new HashMap<String, QueueWithInfo>();
		server = null;

	}

	@Override
	public void addQueueToWatch(Collection<ComponentData> queue, String queueName, int maxQueueSize) {
		QueueWithInfo queueWithInfo = new QueueWithInfo(queue, queueName, maxQueueSize);
		myQueues.put(queueName, queueWithInfo);
	}

	@Override
	public void removeQueueToWatch(String queueName) {
		myQueues.remove(queueName);
	}

	@Override
	public long getQueueSize(String queueName) {
		QueueWithInfo qi = myQueues.get(queueName);
		if (qi != null) {
			return qi.queue.size();
		}
		return -1L;
	}

	public void init() {
		//JMX registration
		String componentName = containerServices.getName();
		try {
			this.server = ManagementFactory.getPlatformMBeanServer();
			QueueWatcher qBean = new QueueWatcher(this);
			qBeanName = new ObjectName("alma.acs.monitoring.blobber.BlobberWatchDog:type=QueueWatcher,name=" + componentName);
			this.server.registerMBean(qBean, qBeanName);
			logger.info("BlobberWatchDog registered in JMX for component: " + componentName);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Failed to register BlobberWatchDog with JXM", ex);
		}
		shouldTerminateRunLoop = false;
	}

	public void cleanUp() {
		shouldTerminateRunLoop = true;
		try {
			this.server.unregisterMBean(qBeanName);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Failed to clean up JMX registration.", ex);
		}
	}

	public void run() {
		this.logger.info("Starting Blobber Watch Dog Thread.");
		Thread.currentThread().setName("BlobberWatchDogThread");

		// Queues cleanup loop
		while(!shouldTerminateRunLoop) {
			for (QueueWithInfo qi : myQueues.values()) {
			System.out.println("Hola"+qi.queue.toString());
			System.out.println(qi.queueName);
			System.out.println(qi.queue.size()+"   "+qi.maxSize);
				if (qi.queue.size() > qi.maxSize - 1000) {
					this.logger.warning("Capacity of queue '" + qi.queueName + "' is less than 1000. Clearing it!");
					//TODO: Set alarm
					System.out.println("Cleanning");
					qi.queue.clear();
				}
			}
			try {
				// Sleep for one second
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				//ex.printStackTrace();
			}
		}
		logger.fine("Watchdog terminated run loop.");
	}

	private static class QueueWithInfo {
		Collection<?> queue;
		String queueName;
		int maxSize;
		
		QueueWithInfo(Collection<?> queue, String queueName, int maxSize) {
			this.queue = queue;
			this.queueName = queueName;
			this.maxSize = maxSize;
		}
	}
}

