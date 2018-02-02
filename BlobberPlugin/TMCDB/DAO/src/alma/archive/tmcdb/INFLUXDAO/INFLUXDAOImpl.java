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
 * License aInteger with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.archive.tmcdb.INFLUXDAO;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.InfluxDBFactory;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import com.squareup.moshi.*;
import okio.BufferedSource;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Constructor;
import java.lang.InterruptedException;



import alma.DAOErrType.wrappers.AcsJDBConnectionFailureEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.ACSErrTypeCommon.wrappers.AcsJCouldntCreateObjectEx;
import alma.acs.logging.AcsLogLevel;
import alma.acs.container.ContainerServices;
import alma.acs.concurrent.NamedThreadFactory;
import alma.acs.monitoring.DAO.ComponentData;
import alma.acs.monitoring.DAO.MonitorDAO;
import alma.acs.monitoring.blobber.BlobberWatchDog;
import alma.archive.tmcdb.persistence.TMCDBConfig;


@SuppressWarnings("deprecation")
public class INFLUXDAOImpl implements MonitorDAO
{
	private ContainerServices containerServices;
	private Logger log;

	private LinkedBlockingQueue<ComponentData> myMQDataQueue;
	private final BlobConsumer myBlobConsumer;
        private final ExecutorService blobConsumerExecutor;
	//private Thread myBlobConsumerThread = null;
	private final HashSet<String> simulatedAntennaHashSet;

	// Influx attributes
	private InfluxDB influxDB;
	private String location;
	private String broker_url;
	private String aRetentionPolicy;
	private String influx_dbname;
	private String influx_user;
	private String influx_password;
	private String influx_measurement;
	private boolean mqEnabled = false;
	private boolean mqConnected = false;


	public INFLUXDAOImpl(ContainerServices cs, BlobberWatchDog watchDog) {
		this.containerServices = cs;
		this.log = cs.getLogger();
		System.out.println("Se llamo a INFLUXDAO");
		TMCDBConfig config = TMCDBConfig.getInstance(log);
		mqEnabled = config.isBrokerEnabled();
		broker_url = config.getBrokerURL();
		aRetentionPolicy = config.getRetentionPolicy();
		influx_dbname = config.getInfluxDBName();
		influx_user = config.getInfluxUser();
		influx_password = config.getInfluxPassword();
		influx_measurement = config.getInfluxMeasurement();
		log.info("This mqDAO will use the following settings for storing data: "
			+ "mqstore_enabled=" + mqEnabled
			+ ", broker_url=" + broker_url
			+ ", aRetentionPolicy=" + aRetentionPolicy
			+ ", influx_dbname=" + influx_dbname
			+ ", influx_user=" + influx_user
			+ ", influx_password=" + influx_password
			+ ", influx_measurement=" + influx_measurement);

		HashSet<String> tmpSimulatedAntennaHashSet = config.getAntennaSimulatedSet(); // need this to allow declaring simulatedAntennaHashSet as final field
		if (tmpSimulatedAntennaHashSet == null) {
			simulatedAntennaHashSet = new HashSet<String>(1);
			simulatedAntennaHashSet.add("NONE");
			log.info("No simulated antennas on current deployment.");
		} else {
			simulatedAntennaHashSet = tmpSimulatedAntennaHashSet;
			for (Object simulatedAntennaName : simulatedAntennaHashSet) {
				log.info("Simulated antenna '" + (String) simulatedAntennaName + "' detected. No monitoring info coming from this antenna will be persisted to TMC");
			}
		}

		myMQDataQueue = new LinkedBlockingQueue<ComponentData>(100000);
		watchDog.addQueueToWatch(myMQDataQueue, "mq", 100000);

		//this.myBlobConsumer = new BlobConsumer();
		ThreadFactory tf = new NamedThreadFactory(containerServices.getThreadFactory(), "MQBlobConsumerThread");
		blobConsumerExecutor = Executors.newSingleThreadExecutor(tf);
		myBlobConsumer = new BlobConsumer();
		blobConsumerExecutor.execute(myBlobConsumer);
		
	}

	public void store(ComponentData inData) throws Exception {
		myMQDataQueue.put(inData);
	}

	/**
	 * Sends data over JMS to the TMCDumper
	 * <p>
	 * Really needed are <code>inData.startTime</code>, <code>inData.componentName</code>, 
	 * <code>inData.index</code>, <code>inData.clob</code>.
	 * <p>
	 * The consumer side code is under ADC/SW/TMCDB/TMC-WS.
	 */
	private void mqStore(ComponentData inData) throws Exception {
		if (!mqEnabled || !mqConnected)
			return;

		// We skip monitoring information coming from simulated antennas
		/*if ( simulatedAntennaHashSet.contains((inData.componentName.split("/"))[1]) ) {
			if ( log.isLoggable(Level.FINER) )
				log.finer("Dropping blob data for " + inData.componentName.split("/")[1] + ":" + inData.propertyPathname());
			return;
		}

		if ( log.isLoggable(Level.FINE) )
			log.fine("Handling blob for component/property: " + inData.toString());
			//log.fine("publishNewMonitorData Called: CompName:" + inData.componentName + ", propName: " + inData.propertyName + ", index: " + inData.index);
*/
		try {
			
			if (inData.statistics != null) {
				this.influxDB.write(Point.measurement(influx_measurement)
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("serialNumber", inData.serialNumber)
					.addField("componentName", inData.componentName)
					.addField("clob", inData.getClob())
					.addField("index", inData.index)
					.addField("propertyName", inData.propertyName)
					.addField("location", location)
					.addField("sampleSize", inData.sampleSize)
					.addField("startTime", inData.startTime)
					.addField("endTime", inData.stopTime)
					.addField("maxStat", inData.statistics.max.doubleValue())
					.addField("minStat", inData.statistics.min.doubleValue())
					.addField("meanStat", inData.statistics.mean.doubleValue())
					.addField("stdDevStat", inData.statistics.stdDev.doubleValue())
					.build());
			}else{
				this.influxDB.write(Point.measurement(influx_measurement)
					.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.addField("serialNumber", inData.serialNumber)
					.addField("componentName", inData.componentName)
					.addField("clob", inData.getClob())
					.addField("index", inData.index)
					.addField("propertyName", inData.propertyName)
					.addField("location", location)
					.addField("sampleSize", inData.sampleSize)
					.addField("startTime", inData.startTime)
					.addField("endTime", inData.stopTime)
					.build());
			}
			

			log.fine(">>>>>>>>>>>>>>>> data sent to InfluxBD");

		} catch (NullPointerException ex) {
			log.warning("NULL POINTER EXCEPTION WHILE PREPARING MESSAGE TO MQ: " + ex);
			ex.printStackTrace();
		}
	}

	public void close() {
		//if (this.myBlobConsumerThread != null) {
		//	this.myBlobConsumerThread.interrupt();
		//	this.myBlobConsumerThread = null;
		//}

		myBlobConsumer.cancel();

		if (mqEnabled && mqConnected) {
			mqConnected = false;
		}

		// after the above cancel() call, the loop should terminate itself. 
		// We wait up to 1000 ms, and log a warning if the loop takes longer or fails otherwise.
		blobConsumerExecutor.shutdown();
		boolean shutdownOK = false;
		try {
			shutdownOK = blobConsumerExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			// log below...
		}
		if (!shutdownOK) {
			log.warning("Failed to orderly shut down the blobConsumerExecutor within 1000 ms.");
		}
	}

	//TODO: This should eventually be moved to a connect() method in the future, as it is needed only once.
	

	
	
	public void openTransactionStore(String transactionName) throws AcsJDBConnectionFailureEx {
		//if (this.myBlobConsumerThread == null) {
		//	this.myBlobConsumerThread = this.containerServices.getThreadFactory().newThread(this.myBlobConsumer);
		//	this.myBlobConsumerThread.start();
		//}

		// If already connected, do nothing
		if (mqEnabled && !mqConnected) {
			log.info("About to connect to influxdb ...");
			location = System.getenv("LOCATION");
			if (location == null) {
				location = "GENERIC";
			}
				this.influxDB = InfluxDBFactory.connect(broker_url, influx_user, influx_password);	
				boolean checkDbExistence = influxDB.databaseExists(influx_dbname);
				if (!checkDbExistence)
					influxDB.createDatabase(influx_dbname);
				this.influxDB.setDatabase(influx_dbname);
				//influxDB.createRetentionPolicy(aRetentionPolicy, influx_dbName, "3d", "30m", 2, true);
				this.influxDB.setRetentionPolicy(aRetentionPolicy);
			
			mqConnected = true;
		}
	}


	public void closeTransactionStore() throws AcsJDBConnectionFailureEx {
		// Do nothing.
	}


	/**
	 * This threadable class consumes the data queue.
	 */
	class BlobConsumer implements Runnable
	{
		protected volatile boolean shouldTerminate = false;

		public BlobConsumer() {
		}

		/**
		 * Sets a flag so that the run method will stop processing as soon as possible.
		 */
		public void cancel() {
			shouldTerminate = true;
		}

		public void run() {
			log.info("Starting MQ blob consumer thread.");
			Thread.currentThread().setName("MQBlobConsumerThread");
			long start=0;
			long end =0;
			while(!shouldTerminate) {
				if (myMQDataQueue.size() > 0) {
					ComponentData tempBlobData = null;
					start = System.currentTimeMillis();
					try {
						// @TODO (hso): the possibly blocking call to take() circumvents thread termination based on the shouldTerminate flag.
						// See MonitorDAOImpl.BlobConsumer#run() for the use of "poll".
						tempBlobData = myMQDataQueue.take();
						System.out.println("tempBlobData "+tempBlobData);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					try {
						mqStore(tempBlobData);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					end = System.currentTimeMillis();
					if ( log.isLoggable(Level.FINER) )
						log.finer(Thread.currentThread().getName()
							+ ":MQ consumer: data taken from the queue, queue size="
							+ myMQDataQueue.size() + " consume time=" + (end-start));
				} else {
					//Thread.yield();
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						log.log(Level.WARNING, "Unexpected InterruptedException in thread " + Thread.currentThread().getName()
								+ ". Will terminate this thread.", ex);
						break; // end the run method
					}
				}
			}
			int queueSize = myMQDataQueue.size();
			if (queueSize > 0) {
				log.warning("Terminating thread '" + Thread.currentThread().getName() +
					"' while the queue still contains " + queueSize + " BlobData elements.");
			}
		}
	}
}
