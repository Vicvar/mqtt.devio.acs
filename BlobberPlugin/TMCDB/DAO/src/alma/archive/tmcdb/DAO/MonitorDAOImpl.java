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
/** MonitorDAOImpl is the implementation of MonitorDAO interface.
 * This class is used by blobbers to persist monitoring informacion through
 * store method. Besides that, is on charge of Control Device
 * properties autoconfiguration.
 *
 * @author Pablo Burgos
 * @since ACS-8_0_0-B Jun2009
 * @version "@(#) $Id: MonitorDAOImpl.java,v 1.25 2012/03/01 10:16:25 hsommer Exp $
 */
package alma.archive.tmcdb.DAO;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.hibernate.NonUniqueObjectException;

import alma.ACSErrTypeCommon.wrappers.AcsJUnexpectedExceptionEx;
import alma.DAOErrType.wrappers.AcsJDBConnectionFailureEx;
import alma.DAOErrType.wrappers.AcsJDynConfigFailureEx;
import alma.DAOErrType.wrappers.AcsJGettingMonitorCharacteristicsEx;
import alma.DAOErrType.wrappers.AcsJStoreFailureEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.concurrent.NamedThreadFactory;
import alma.acs.container.ContainerServices;
import alma.acs.monitoring.DAO.ComponentData;
import alma.acs.monitoring.DAO.MonitorDAO;
import alma.acs.monitoring.blobber.BlobberPluginAlmaImpl;
import alma.acs.monitoring.blobber.BlobberWatchDog;
import alma.acs.tmcdb.Assembly;
import alma.acs.tmcdb.AssemblyType;
import alma.acs.tmcdb.BACIProperty;
import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.DefaultBaciProperty;
import alma.acs.tmcdb.DefaultComponent;
import alma.acs.tmcdb.DefaultMonitorPoint;
import alma.acs.tmcdb.HWConfiguration;
import alma.acs.tmcdb.MonitorData;
import alma.acs.tmcdb.MonitorDataId;
import alma.acs.tmcdb.MonitorPoint;
import alma.acs.tmcdb.MonitorPointDatatype;
import alma.acs.util.IsoDateFormat;
import alma.archive.tmcdb.DAO.BlobDataQueue.TransactionScope;
import alma.archive.tmcdb.persistence.ComponentNameHelper;
import alma.archive.tmcdb.persistence.TMCDBConfig;
import alma.archive.tmcdb.persistence.TMCDBPersistence;

/**
 * This class is currently not used in operations 
 * (creation is commented out in {@link BlobberPluginAlmaImpl#createMonitorDAOs()}) 
 * because of http://ictjira.alma.cl/browse/ICT-462?focusedCommentId=44849 
 */
@SuppressWarnings("deprecation")
public class MonitorDAOImpl implements MonitorDAO
{
	public static final int MAX_QUEUE_SIZE = 100000;
	
	private final ContainerServices containerServices;
	private final Logger log;
//	private final BlobberWatchDog watchDog;
	
	/**
	 * The TMCDB ConfigurationId gets fetched once on demand,
	 * based on the Configuration name 
	 * (which is a constant defined in TMCDBConfig.getInstance().getConfigurationName()).
	 * @see #getConfigurationId(EntityManager, String)
	 */
	private volatile Integer cachedConfigurationId;
	private volatile Integer cachedHwConfigurationId;

	/**
	 * key = property name with path, for which auto-configuration has failed already before.<br>
	 * value = (not used) 
	 * <p>
	 * Note that this map is only accessed from {@link #store(ComponentData)} and thus does not need to be re-entrant.
	 */
	private HashMap<String, Object> myConfiguredComponentsMap = new HashMap<String, Object>();

	/**
	 * The DB transaction controlled externally (by the blobber).
	 * @see #openTransactionStore()
	 * @see #closeTransactionStore() 
	 */
	private EntityTransaction transactionStore;
	private final TMCDBPersistence myPersistenceLayer;
	private EntityManager entityManagerStore;

	private boolean dbConnectionEnabled = false;
	//private boolean monitoringOnlyEnabled = false; //TODO: This is currently never used.
	private String configurationName = null;
	private final HashSet<String> simulatedAntennaHashSet;
	
	/**
	 * 
	 */
	private final HashMap<ComponentData, MonitorCharacteristicIDs> componentData2MonitorCharacteristicIDs_HM;

	/**
	 * Maximum for {@link #countAttempts} before giving up.
	 */
	private static final int MaxAttempts = 10;

	/**
	 * A shared counter for failed attempts to connect to the database.
	 * Note that openTransactionStore / closeTransactionStore do not count {@link PersistenceException}
	 * toward this number, while {@link #store(ComponentData)} does.
	 */
	private volatile int countAttempts = 0;

	/**
	 * The queue for ComponentData, with additional info about transactions
	 * as received through calls to the openTransaction and closeTransaction methods.
	 */
	private final BlobDataQueue myBlobDataQueue;
	
	/**
	 * Gets data from {@link #myBlobDataQueue} and stores it in the database.
	 */
	private final BlobConsumer myBlobConsumer;
	
	/**
	 * The thread that runs {@link #myBlobConsumer}.
	 */
	private final ExecutorService blobConsumerExecutor;


	public MonitorDAOImpl(ContainerServices cs, BlobberWatchDog watchDog) {
		this.containerServices = cs;
		this.log = cs.getLogger();
//		this.watchDog = watchDog;

		myPersistenceLayer = new TMCDBPersistence(log);
		TMCDBConfig config = TMCDBConfig.getInstance(log);
		dbConnectionEnabled = config.isDBConnectionEnabled();
		//monitoringOnlyEnabled = config.isMonitoringOnlyEnabled();
		configurationName = config.getConfigurationName();
		log.info("This DAO will use the following settings for storing data: "
			+ "dbstore_enabled=" + dbConnectionEnabled
			+ ", configurationName=" + configurationName);

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

		componentData2MonitorCharacteristicIDs_HM = new HashMap<ComponentData, MonitorCharacteristicIDs>(10000);

		myBlobDataQueue = new BlobDataQueue(MAX_QUEUE_SIZE, log);
		watchDog.addQueueToWatch(myBlobDataQueue.asFlatCollection(), "db", MAX_QUEUE_SIZE);

		// Set up the thread and runnable to take data from the queue and store it into the DB.
		// We use Executors#newSingleThreadExecutor instead of creating our own thread so that a new worker thread gets started
		// automatically in case the old one dies.
		ThreadFactory tf = new NamedThreadFactory(containerServices.getThreadFactory(), "BlobConsumerThread");
		blobConsumerExecutor = Executors.newSingleThreadExecutor(tf);
		myBlobConsumer = new BlobConsumer();
		blobConsumerExecutor.execute(myBlobConsumer);
	}


	@Override
	public void close() {
		myBlobConsumer.cancel();
		myPersistenceLayer.close();

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

	/**
	 * Marks the beginning of a transaction in the data queue. 
	 * When this marker gets processed later, {@link #openDatabaseTransaction()} will be called.
	 * @see alma.acs.monitoring.DAO.MonitorDAO#openTransactionStore()
	 */
	@Override
	public void openTransactionStore(String transactionName) {
		myBlobDataQueue.openTransaction(transactionName);
	}

	private void openDatabaseTransaction() throws AcsJDBConnectionFailureEx {
		if (dbConnectionEnabled) {
			log.fine("About to connect to DB ...");
			while (countAttempts < MaxAttempts) {
				try {
					entityManagerStore = this.myPersistenceLayer.getEntityManager();
					transactionStore = entityManagerStore.getTransaction();
					transactionStore.begin();
					return; // the good case
				} catch (PersistenceException ex) {
					throw new AcsJDBConnectionFailureEx("Persistence Exception caught: ", ex);
				} catch (Exception e) {
					// Here an exception has been caught likely caused by a Database disconnection or network issue.
					// After MaxAttempts, connections to the Database will not be attempted anymore. Refer to COMP-4240
					countAttempts += 1;
					if (countAttempts > MaxAttempts) {
						dbConnectionEnabled = false;
						send_alarm("Monitoring", "DAO", 1, true);
						throw new AcsJDBConnectionFailureEx("DAO couldn't get connected to Database (open). Attempt " + countAttempts + " out of " + MaxAttempts, e);
					}
					// sleep 1s before trying again
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Marks the end of transaction in the data queue. 
	 * When this marker gets processed later, {@link #closeDatabaseTransaction()} will be called.
	 * @see alma.acs.monitoring.DAO.MonitorDAO#closeTransactionStore()
	 */
	@Override
	public void closeTransactionStore() throws AcsJUnexpectedExceptionEx {
		try {
			myBlobDataQueue.closeTransaction();
		} catch (Exception ex) {
			throw new AcsJUnexpectedExceptionEx(ex);
		}
	}

	private void closeDatabaseTransaction() throws AcsJDBConnectionFailureEx {
		if (dbConnectionEnabled) {
			log.fine("About to disconnect from DB ...");
			try {
				transactionStore.commit();
				entityManagerStore.close();
			} catch (PersistenceException ex) {
				throw new AcsJDBConnectionFailureEx("Persistence Exception caught: ", ex);
			} catch (Exception e) {
				/*
				 * Here an exception has been caught likely caused by a Database disconnection or network issue.
				 * After MaxAttempts, connections to the Database will not be attempted anymore. Refer to COMP-4240.
				 */
				countAttempts += 1;
				if (countAttempts > MaxAttempts) {
					dbConnectionEnabled = false;
					send_alarm("Monitoring", "DAO", 1, true);
				}
				throw new AcsJDBConnectionFailureEx("DAO couldn't get connected to Database (close). Attempt " + countAttempts + " out of " + MaxAttempts, e);
			}
		}
	}
	
	
	/**
	 * @return true if auto-configuration for the given property has failed before.
	 */
	public boolean hasFailedToBeConfigured(ComponentData inData) {
		/*
		 * We must be sure to just try auto-configuration once for a given
		 * component name. This is to avoid new attempts each time data with
		 * that component information is retrieved from a monitordatablock.
		 */
		return this.myConfiguredComponentsMap.containsKey(inData.propertyPathname());
	}

	/**
	 * Mark the given property as auto-configuration-failed, to avoid future attempts.
	 */
	public void setHasFailedToBeConfigured(ComponentData inData) {
		String hashEntry = inData.propertyPathname();
		this.myConfiguredComponentsMap.put(inData.propertyPathname(), null);

		if ( log.isLoggable(Level.FINE) )
			log.fine("Dynamic configuration failed for property " + hashEntry);
	}

	/**
	 * Reads the configuration ID matching <code>configurationName</code> from the database once,
	 * and then caches the value in {@link #cachedConfigurationId}.
	 */
	private Integer getConfigurationId(EntityManager currentEntityManagerStore, String configurationName)
		throws NonUniqueResultException, NoResultException {
		if (cachedConfigurationId == null) {
			Query query = currentEntityManagerStore.createNamedQuery("findConfigurationByName");
			query.setParameter("configurationName", configurationName);
			Configuration conf = (Configuration) query.getSingleResult();
			cachedConfigurationId = conf.getConfigurationId();

			if ( log.isLoggable(Level.FINE) ) {
				log.fine("Resolved configurationId=" + cachedConfigurationId
						+ " for configurationName=" + configurationName);
			}
		}
		return cachedConfigurationId;
	}

	/**
	 * Reads the HwConfigurationId matching the given swConfigurationId from the database once,
	 * and then caches the value in {@link #cachedHwConfigurationId}.
	 * Note that HwConfiguration matches Configuration 1-to-1 in the TMCDB schema.
	 */
	private Integer getHwConfigurationId(EntityManager currentEntityManagerStore, Integer swConfigurationId)
			throws NonUniqueResultException, NoResultException {
		if (cachedHwConfigurationId == null) {
			Query query = currentEntityManagerStore.createNamedQuery("findHwConfBySwConfigId");
			query.setParameter("swConfigurationId", swConfigurationId);
			HWConfiguration conf = (HWConfiguration) query.getSingleResult();
			cachedHwConfigurationId = conf.getConfigurationId();

			if ( log.isLoggable(Level.FINE) ) {
				log.fine("Resolved HwConfigurationId=" + cachedHwConfigurationId
					+ " for swConfigurationId=" + swConfigurationId);
			}
		}
		return cachedHwConfigurationId;
	}

	private Integer getAssembly(EntityManager currentEntityManagerStore,
			Integer hwConfigId, String serialNumber)
			throws NonUniqueResultException, NoResultException {
		Query query = currentEntityManagerStore.createNamedQuery("findAssemblyBySerialNumberAndConfigurationId");
		query.setParameter("serialNumber", serialNumber);
		query.setParameter("hwConfigurationId", hwConfigId);
		Assembly assembly = (Assembly) query.getSingleResult();

		if ( log.isLoggable(Level.FINE) )
			log.fine("assemblyId = " + assembly.getAssemblyId());
		return assembly.getAssemblyId();
	}

	private Integer getComponent(EntityManager currentEntityManagerStore,
			Integer configurationId, String componentName)
			throws NonUniqueResultException, NoResultException {
		Query query = currentEntityManagerStore.createNamedQuery("findComponentByComponentName");
		String tokens[] = ComponentNameHelper.getPathAndName(componentName);
		query.setParameter("path", tokens[0]);
		query.setParameter("componentName", tokens[1]);
		query.setParameter("configurationId", configurationId);
		Component comp = (Component) query.getSingleResult();

		if ( log.isLoggable(Level.FINE) )
			log.fine("componentId = " + comp.getComponentId());
		return comp.getComponentId();
	}

	private Integer getBaciProperty(EntityManager currentEntityManagerStore,
			Integer componentId, String propertyName)
			throws NonUniqueResultException, NoResultException {
		Query query = currentEntityManagerStore.createNamedQuery("findBACIPropertyIdByPropertyNameANDComponentId");
		query.setParameter("componentId", componentId);
		query.setParameter("propertyName", propertyName);
		BACIProperty baciProp = (BACIProperty) query.getSingleResult();

		if ( log.isLoggable(Level.FINE) )
			log.fine("baciPropertyId = " + baciProp.getBACIPropertyId());
		return baciProp.getBACIPropertyId();
	}

	private Integer getMonitorPointId(EntityManager currentEntityManagerStore,
			Integer assemblyId, Integer baciPropertyId, int index)
			throws NonUniqueResultException, NoResultException {
		Query query = currentEntityManagerStore.createNamedQuery("findMonitorPointIdByAssemblyIdANDBACIPropertyIdANDIndex");
		query.setParameter("assemblyId", assemblyId);
		query.setParameter("BACIPropertyId", baciPropertyId);
		query.setParameter("indice", index);
		MonitorPoint mp = (MonitorPoint) query.getSingleResult();
		Integer monitorPointId = mp.getMonitorPointId();

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("index = " + index);
			log.fine("monitorPointId = " + monitorPointId);
		}
		return monitorPointId;
	}

	/**
	 * This method is called each time a property information wants to be
	 * persisted to database. A caching strategy was implemented to reduce
	 * drastically the amount of queries against database.
	 * <p>
	 * Performs auto-configuration if needed, see {@link #configureNewAssembly(MonitorCharacteristicIDs, ComponentData)}.
	 */
	public MonitorCharacteristicIDs getMonitorCharacteristicIDs(
			EntityManager currentEntityManagerStore,
			String configurationName,
			ComponentData inData)
		throws AcsJGettingMonitorCharacteristicsEx, AcsJDynConfigFailureEx {

		// constructor of MonitorCharacteristicIDs sets all to -1 and false
		MonitorCharacteristicIDs monitorCharacteristicIDs = new MonitorCharacteristicIDs();

		if (componentData2MonitorCharacteristicIDs_HM.containsKey(inData)) {
			/*
			 * This means we already have key in our HashMap... So its not
			 * needed to perform the query again,
			 * Let's look for it in the Map and return it
			 */
			MonitorCharacteristicIDs ids = componentData2MonitorCharacteristicIDs_HM.get(inData);
			if ( log.isLoggable(Level.FINE) ) {
				log.fine("getMonitorCharacteristicIDs found answer in HashMap for"
					+ " ComponentName=" + inData.componentName
					+ " PropertyName=" + inData.propertyName
					+ " index=" + inData.index
					+ " AssemblyId=" + ids.getAssemblyId()
					+ " BaciPropertyId=" + ids.getBACIPropertyId()
					+ " ComponentId=" + ids.getComponentId()
					+ " ConfigurationId=" + ids.getConfigurationId()
					+ " MonitorPointId=" + ids.getMonitorPointId()
					+ " isOnDB=" + ids.isOnDB());
			}
			return ids;
		} else {
			/*
			 * well, in this case the key (inData) is not in our hash map. Lets'
			 * look for it and if the finding is successful, then add it to
			 * HashMap
			 */
			if ( log.isLoggable(Level.FINE) ) {
				log.fine("getMonitorCharacteristicIDs not found in hashmap for"
					+ " ComponentName=" + inData.componentName
					+ " PropertyName=" + inData.propertyName
					+ " index=" + inData.index
					+ ", => Dynamic Configuration Started!");
			}

			// Get the Configuration ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get configurationid for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);

				}
				Integer configurationId = getConfigurationId(currentEntityManagerStore, configurationName);
				monitorCharacteristicIDs.setConfigurationId(configurationId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Configurationid= " + configurationId
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Could not find configuration '" + configurationName + "'", e);
			} catch (NonUniqueResultException e) {
				// this means problems too
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple configurations with name '" + configurationName + "'", e);
			}

			// Get the HwConfiguration ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get hwConfigurationId for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
				Integer hwConfigurationId = getHwConfigurationId(currentEntityManagerStore, monitorCharacteristicIDs.getConfigurationId());
				monitorCharacteristicIDs.setHwConfigurationId(hwConfigurationId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("hwConfigurationId= " + monitorCharacteristicIDs.getHwConfigurationId()
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Could not find HW configuration for configuration '" + configurationName + "'", e);
			} catch (NonUniqueResultException e) {
				// this means problems too
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple HW configurations for configuration '" + configurationName + "'", e);
			}

			// Get (and auto-configure) the Assembly ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get assemblyId for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
				Integer assemblyId = getAssembly(currentEntityManagerStore, monitorCharacteristicIDs.getHwConfigurationId(), inData.serialNumber.toUpperCase());
				monitorCharacteristicIDs.setAssemblyId(assemblyId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("assemblyId= " + monitorCharacteristicIDs.getAssemblyId()
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException e) {
				/*
				 * at this point we know that autoconfiguration is needed because there
				 * is no assembly within this configuration with the given serialnumber
				 */
				Integer assemblyId = configureNewAssembly(monitorCharacteristicIDs, inData);
				monitorCharacteristicIDs.setAssemblyId(assemblyId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Assembly Dynamically Autoconfigured!! assemblyId= " + assemblyId
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NonUniqueResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple assemblyIds with serialnumber="
								+ inData.serialNumber.toUpperCase(), e);
			}

			// Get the Component ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get componentId for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
				Integer componentId = getComponent(currentEntityManagerStore, monitorCharacteristicIDs.getConfigurationId(), inData.componentName);
				monitorCharacteristicIDs.setComponentId(componentId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("componentId= " + componentId
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException ex) {
				throw new AcsJGettingMonitorCharacteristicsEx(
					"Component Not Found. This must be configured by TMCDB procedure", ex);
			} catch (NonUniqueResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple componentId matching the given componentName.", e);
			}

			// Get the BACIProperty ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get baciPropertyId for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
				Integer baciPropertyId = getBaciProperty(currentEntityManagerStore, monitorCharacteristicIDs.getComponentId(), inData.propertyName);
				monitorCharacteristicIDs.setBACIPropertyId(baciPropertyId);
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("baciPropertyId= " + monitorCharacteristicIDs.getBACIPropertyId()
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException e) {
				throw new AcsJGettingMonitorCharacteristicsEx(
					"BACI property Not Found. This must be configure bt TMCDB procedure", e);
			} catch (NonUniqueResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple BACI properties matching the given property name and componentId.", e);
			}

			// Get (and auto-configure) the MonitorPoint ID
			try {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Attempting to get monitorPointId for "
							+ " ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}

				Integer monitorPointId = getMonitorPointId(currentEntityManagerStore,
						monitorCharacteristicIDs.getAssemblyId(),
						monitorCharacteristicIDs.getBACIPropertyId(),
						inData.index);
				monitorCharacteristicIDs.setIndex(inData.index);
				monitorCharacteristicIDs.setMonitorPointId(monitorPointId);
				monitorCharacteristicIDs.setIsOnDB(true);

				if ( log.isLoggable(Level.FINE) ) {
					log.fine("monitorPointId=" + monitorCharacteristicIDs.getMonitorPointId()
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NoResultException e) {
				/*
				 * at this point we know that autoconfiguration is needed there
				 * is no monitorpoint matching the given assemblyId,
				 * baciPropertyId and index within this configuration since the
				 * baciProperty is known (getBaciPropertyId returned a valid
				 * value) it means that that a new monitorpoint is being
				 * monitored (change to the devices spreadsheet) static tables
				 * (defaultMonitorPoint) must have been updated to be able to
				 * store this data
				 */
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("About to autoconfigure monitor point for bacipropertyid="
							+ monitorCharacteristicIDs.getBACIPropertyId() + " corresponding to "
							+ inData.componentName + " " + inData.propertyName);
				}
				Integer monitorPointId = configureNewMonitorPoint(monitorCharacteristicIDs, inData);
				monitorCharacteristicIDs.setIndex(inData.index);
				monitorCharacteristicIDs.setMonitorPointId(monitorPointId);
				// we leave setIsOnDB as false to signal that this blob was just configured
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("DynamicConfiguration succesfull. monitorPointId= " + monitorCharacteristicIDs.getMonitorPointId()
							+ " for ComponentName=" + inData.componentName
							+ " PropertyName=" + inData.propertyName);
				}
			} catch (NonUniqueResultException e) {
				// this means problems
				throw new AcsJGettingMonitorCharacteristicsEx(
						"Found multiple monitor points matching the given assemblyId, baciPropertyId and index.", e);
			}
			//Just at this point I can add the componentData information to the HashMap, since I'm
			//sure I have all needed fields

			Query query = currentEntityManagerStore.createNamedQuery("findMonitorPointNameGivenMonitorPointId");
			query.setParameter("monitorPointId", monitorCharacteristicIDs.getMonitorPointId());

			monitorCharacteristicIDs.setMonitorPointName((String) query.getSingleResult());

			componentData2MonitorCharacteristicIDs_HM.put(inData, monitorCharacteristicIDs);
			
			if ( log.isLoggable(Level.FINE) ) {
				log.fine("Dynamic Configuration succeeded! Information added to hashmap:"
						+ " ComponentName=" + inData.componentName
						+ " PropertyName=" + inData.propertyName
						+ " index=" + inData.index
						+ " AssemblyId=" + monitorCharacteristicIDs.getAssemblyId()
						+ " BaciPropertyId=" + monitorCharacteristicIDs.getBACIPropertyId()
						+ " ComponentId=" + monitorCharacteristicIDs.getComponentId()
						+ " ConfigurationId=" + monitorCharacteristicIDs.getConfigurationId()
						+ " MonitorPointId=" + monitorCharacteristicIDs.getMonitorPointId()
						+ " isOnDB=" + monitorCharacteristicIDs.isOnDB());
			}
		}

		return monitorCharacteristicIDs;
	}

	/**
	 * Insert new data to the data queue.
	 * <p>
	 * @TODO (hso): We should discuss again the return behavior of this method. 
	 *              Currently the call will block if the queue is full with 100000 entries,
	 *              even though BlobberWatchDogAlmaImpl#run() tries to prevent this.
	 *              I think at some point of discussion we had said that this is good, so that the 
	 *              blobber can learn about DAO problems in this way, raising an alarm if it fails to process
	 *              all data during one blobber cycle and so on.
	 *              Given that the DAO anyway tries to throw away data if necessary, we could also throw it away
	 *              here if the queue is full (watch dog failure).
	 */
	@Override
	public void store(ComponentData inData) throws Exception {
		TransactionScope t = myBlobDataQueue.getOpenTransaction();
		if (t != null) {
			t.getDataQueue().put(inData);
		}
		else {
			throw new IllegalStateException("Failed to store data because no transaction was open.");
		}
	}

	/**
	 * Store method is the main method inside MonitorDAOImpl class. This method
	 * attempts to persists the ComponentData object into the database. This
	 * method will attempt to get the Monitor Characteristics IDs. These ID's
	 * are the primary keys of Dynamic Tables on TMC schema definition:
	 * Assembly, Component, BACIProperty and MonitorPoint. If one of these ID's
	 * can not be found on Database, for the running Configuration ID,
	 * autoconfiguration will be attempt. Autoconfiguration is a Dynamic
	 * Algorithm, that infers Dynamic Monitoring Information from the data
	 * inside static tables DefaultComponent, DefaultBACIProperty,
	 * DefaultMonitorPoint. If the inference process is successful, Dynamic
	 * Tables will be filled in with correct monitoring information.
	 *
	 * @param inData Data for one monitored property.
	 * @throws Exception
	 */
	protected void dbStore(ComponentData inData) throws Exception {
		if (!dbConnectionEnabled)
			return;

		// We skip monitoring information coming from simulated antennas
		String[] compNameSegments = inData.componentName.split("/");
		if ( compNameSegments.length >= 2 && simulatedAntennaHashSet.contains(compNameSegments[1]) ) {
			if ( log.isLoggable(Level.FINER) )
				log.finer("Dropping blob data for " + compNameSegments[1] + ":" + inData.propertyPathname());
			return;
		}
		//log.finer("Passed the filter for non-simulated antennas for " inData.componentName.split("/")[1] + ":" inData.propertyPathname());

		if ( log.isLoggable(Level.FINE) )
			log.fine("Handling blob for component/property: " + inData.toString());

		try {
			if (!hasFailedToBeConfigured(inData)) {
				if ( log.isLoggable(Level.FINE) )
					log.fine("Will insert blob data for property " + inData.propertyPathname());

				MonitorCharacteristicIDs monitorCharacteristicIDs;
				try {
					// find or create the meta data
					monitorCharacteristicIDs = getMonitorCharacteristicIDs(entityManagerStore, configurationName, inData);
				} catch (AcsJGettingMonitorCharacteristicsEx e) {
					// This exception typically is thrown when a NonUniqueResultException has been received
					throw new AcsJStoreFailureEx("Failure when getting monitor characteristics", e);
				} catch (AcsJDynConfigFailureEx e) {
					// This exception is thrown when an attempt to auto configure has been made and it failed
					setHasFailedToBeConfigured(inData);

					log.log(Level.FINE, "Monitor point could not be autoconfigured for component = "
							+ inData.componentName + ", serialNumber = " + inData.serialNumber
							+ ", propertyName = " + inData.propertyName + ", index = " + inData.index + " .", e);

					throw new AcsJStoreFailureEx("Failure when configuring DB for: " + inData.componentName
							+ ":" + inData.propertyName + ":" + inData.index, e);
				}
				persistNewMonitorData(entityManagerStore, inData, monitorCharacteristicIDs);
			} else {
				if ( log.isLoggable(Level.FINER) ) {
					String msg = "Dropping blob data for '" + inData.propertyPathname()
							+ "' since monitor point could not be configured previously.";
					log.finer(msg);
				}
			}
		} catch (PersistenceException ex) {
			/*
			 * Here a Persistence exception has been caught. After MaxAttempts, connections to the Database will not be
			 * attempted anymore. Refer to COMP-4240.
			 * @TODO: Distinguish between the various causes of PersistenceException.
			 *        COMP-4240 assumes a DB connection problem, caused by a wrong dbConfig entry or by a network problem.
			 *        However, in early R9.0.4 testing, the cause was a NonUniqueObjectException 
			 *        which has nothing to do with DB connection problems.
			 *        The following is just an ad-hoc implementation and should be checked!
			 */
			if (ex.getCause() instanceof NonUniqueObjectException) {
				NonUniqueObjectException ex2 = (NonUniqueObjectException) ex.getCause();
				Object identifier = ex2.getIdentifier();
				if (identifier instanceof MonitorData) {
					MonitorData monitorData = (MonitorData) identifier;
					int monitorPointId = monitorData.getId().getMonitorPointId();
					Date date = monitorData.getId().getMonitorTS();
					log.severe("NonUniqueObjectException caught for monitorPointId=" + monitorPointId + ", monitorTS=" + IsoDateFormat.formatDate(date));
				}
				else {
					// for some reason we did not run into the above if block, so now let's check what data really is attached.
					log.severe("NonUniqueObjectException caught with identifier type=" + ex2.getIdentifier().getClass().getName() + " and EntityName=" + ex2.getEntityName());
				}
				throw ex2;
			}
			else {
				countAttempts += 1;
				if (countAttempts > MaxAttempts) {
					dbConnectionEnabled = false;
					send_alarm("Monitoring", "DAO", 1, true);
				}
				throw new AcsJDBConnectionFailureEx("DAO couldn't get connected to Database (store). Attempt " + countAttempts
						+ " out of " + MaxAttempts, ex);
			}
		}
	}

	/**
	 * This methods gets the IDL URI based on the component name.
	 * <p>
	 * Special support for unit tests running without ACS services: 
	 * All component names ending with "ACME" will yield "IDL:alma/Control/ACME:1.0".
	 */
	public String getComponentIDL(String componentName) throws Exception {
		if ((componentName.substring(componentName.lastIndexOf("/") + 1)).equalsIgnoreCase("ACME")) {
			return "IDL:alma/Control/ACME:1.0";
		} else {
			String type = containerServices.getComponentDescriptor(componentName).getType();
			if ( log.isLoggable(Level.FINE) ) {
			log.fine("getComponentIDL: ComponentName: " + componentName
				+ " Type: " + type);
			}
			return type;
		}
	}

	/**
	 * This methods assumes that the AssemblyTypeName corresponds to the string
	 * after the last '/' of the Corresponding component's IDL URI example:
	 * "IDL:alma/Control/MountVertex:1.0" -> "MountVertex"
	 */
	private String getAssemblyTypeName(String componentIDL) {
		String assemblyTypeName = componentIDL.split(":")[1];
		String[] aux = assemblyTypeName.split("/");
		assemblyTypeName = aux[aux.length - 1];
		return assemblyTypeName;
	}

	public Integer configureNewAssembly(MonitorCharacteristicIDs monitorCharacteristicIDs,
			ComponentData inData)
		throws AcsJDynConfigFailureEx {

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Going to add configuration for assembly with SN = "
					+ inData.serialNumber.toUpperCase());
		}

		// Look up the assembly type associated with inData
		String assemblyIDL;
		try {
			assemblyIDL = getComponentIDL(inData.componentName);
		} catch (Exception e) {
			throw new AcsJDynConfigFailureEx(
				"Could not get IDL based on component name. Aborting dynamic configuration", e);
		}

		String assemblyTypeName = getAssemblyTypeName(assemblyIDL);

		/*
		 * The next queries and updates must be handled as a transaction since
		 * we can have several blobbers modifying the tables involved in
		 * autoconfiguration (dirty reading can happen). 
		 * This is a nested transaction inside store transaction.
		 */
		EntityManager entityManager = this.myPersistenceLayer.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		/*
		 * Transaction must be enclosed in a try catch statement, to be able to
		 * rollback the transaction in case of failure
		 */
		try {
			AssemblyType assemblyType = getAssemblyTypeByLikeAssemblyCode(assemblyTypeName, entityManager);
			Assembly newAssembly = persistNewAssembly(entityManager, assemblyType, monitorCharacteristicIDs.getHwConfigurationId(), inData);
			transaction.commit();
			return newAssembly.getAssemblyId();
		} catch (NonUniqueResultException e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Found multiple assembly types matching the assembly code "
				+ assemblyTypeName + ".", e);
		} catch (NoResultException e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Found no assembly type matching the assembly code "
				+ assemblyTypeName + ".", e);
		} catch (Exception e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Failure while persisting new assembly: "
						+ assemblyTypeName + ".", e);
		} finally {
			if (entityManager != null) {
				if (transaction.isActive() && transaction.getRollbackOnly()) {
					try {
						if ( log.isLoggable(Level.FINE) ) {
							log.fine("Failed to add configuration for assembly with SN = "
									+ inData.serialNumber.toUpperCase());
						}
						transaction.rollback();
					} catch (RuntimeException rbEx) {
						log.log(Level.FINE, "Couldn't roll back transaction.", rbEx);
					}
				} else {
					if ( log.isLoggable(Level.FINE) ) {
						log.fine("Added configuration for assembly with SN = " 
							+ inData.serialNumber.toUpperCase());
					}
				}

				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Closing entity manager opened to create assembly "
							+ inData.serialNumber.toUpperCase());
				}
				entityManager.close();
			}
		}
	}

	private Integer configureNewMonitorPoint(MonitorCharacteristicIDs monitorCharacteristicIDs,
			ComponentData inData)
		throws AcsJDynConfigFailureEx {

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Going to add configuration for monitor point index = " + inData.index
					+ ", of property = " + inData.propertyName);
		}

		Integer assemblyId = monitorCharacteristicIDs.getAssemblyId();
		Integer propertyId = monitorCharacteristicIDs.getBACIPropertyId();
		String assemblyIDL;
		try {
			assemblyIDL = getComponentIDL(inData.componentName);
		} catch (Exception e) {
			throw new AcsJDynConfigFailureEx(
				"Could not get IDL based on component name. Aborting dynamic configuration", e);
		}

		String assemblyTypeName = getAssemblyTypeName(assemblyIDL);

		/*
		 * The next queries and updates must be handled as a transaction
		 * since we can have several blobbers modifying the tables involved
		 * in autoconfiguration (dirty reading can happens).
		 * In some sense this is a nested transaction inside store transaction.
		 */
		EntityManager entityManager = this.myPersistenceLayer.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		/*
		 * Transaction must be enclosed in a try catch statement, to be able to rollback
		 * the transacion in case
		 */
		try {
			DefaultComponent defaultComponent = getDefaultComponentByLikeAssemblyTypeName(entityManager, assemblyTypeName);
			DefaultBaciProperty defaultBACIProperty = getDefaultBACIPropertyByDefaultComponentIdAndPropertyName(entityManager,
				defaultComponent.getDefaultComponentId(), inData.propertyName);
			DefaultMonitorPoint defaultMonitorPoint = getDefaultMonitorPointByDefaultBACIPropId(entityManager,
				defaultBACIProperty.getDefaultBaciPropId(), inData.index);
			persistNewMonitorPoint(entityManager, defaultMonitorPoint, propertyId, assemblyId);
			transaction.commit();
			Integer newMonitorPointId = getMonitorPointId(entityManager, assemblyId, propertyId, inData.index);
			return newMonitorPointId;
		} catch (NonUniqueResultException e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Found multiple default monitor point matching the index"
				+ inData.index + ".", e);
		} catch (NoResultException e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Found no default monitor point matching the given index"
				+ inData.index + ".", e);
		} catch (Exception e) {
			transaction.setRollbackOnly();
			throw new AcsJDynConfigFailureEx(
				"Failure while persisting new monitorpoint: "
				+ inData.propertyName + ".", e);
		} finally {
			if (entityManager != null) {
				if ( log.isLoggable(Level.FINE) ) {
					log.fine("Closing entity manager opened to create monitor point for BACIProperty "
						+ inData.propertyName);
				}
				if ( transaction.isActive() && transaction.getRollbackOnly() ) {
					try {
						log.fine("Exception detected, rollback.");
						transaction.rollback();
					} catch (RuntimeException rbEx) {
						if ( log.isLoggable(Level.FINE) )
							log.fine("Couldn't roll back transaction: " + rbEx.toString());
					}
				}
				entityManager.close();
			}
		}
	}

	private AssemblyType getAssemblyTypeByLikeAssemblyCode(String assemblyTypeName,
			EntityManager entityManager)
		throws NonUniqueResultException, NoResultException {
	
		Query query = entityManager.createNamedQuery("findAssemblyTypeByLikeAssemblyCode");
		query.setParameter("assemblyTypeName", assemblyTypeName);
		AssemblyType assemblyType = (AssemblyType) query.getSingleResult();
		
		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Assembly Type name to be associated is "
					+ assemblyType.getAssemblyTypeName());
		}
		
		return assemblyType;
	}

	private DefaultComponent getDefaultComponentByLikeAssemblyTypeName(
			EntityManager entityManager, String assemblyTypeName)
		throws NonUniqueResultException, NoResultException {

		Query query = entityManager.createNamedQuery("findDefaultComponentByLikeAssemblyTypeName");
		query.setParameter("assemblyTypeName", assemblyTypeName);
		DefaultComponent defaultComponent = (DefaultComponent) query.getSingleResult();

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Default component: "
					+ defaultComponent.getAssemblyType().getAssemblyTypeName());
		}

		return defaultComponent;
	}

	private DefaultBaciProperty getDefaultBACIPropertyByDefaultComponentIdAndPropertyName(
			EntityManager entityManager, Integer defaultComponentId, String propertyName)
		throws NonUniqueResultException, NoResultException {
	
		Query baciQuery = entityManager.createNamedQuery("findDefaultBACIPropertyByDefaultComponentId");
		baciQuery.setParameter("defaultComponentId", defaultComponentId);
		baciQuery.setParameter("propertyName", propertyName);
		DefaultBaciProperty defaultBACIProperty = (DefaultBaciProperty) baciQuery.getSingleResult();
	
		return defaultBACIProperty;
	}

	private DefaultMonitorPoint getDefaultMonitorPointByDefaultBACIPropId(
			EntityManager entityManager, Integer defaultBACIPropId, int index)
		throws NoResultException, NonUniqueResultException {

		Query monitorQuery = entityManager.createNamedQuery("findDefaultMonitorPointListByDefaultBACIPropId");
		monitorQuery.setParameter("defaultBaciPropId", defaultBACIPropId);
		monitorQuery.setParameter("indice", index);
		DefaultMonitorPoint defaultMonitorPoint = (DefaultMonitorPoint) monitorQuery.getSingleResult();

		return defaultMonitorPoint;
	}

	private Assembly persistNewAssembly(EntityManager entityManager,
			AssemblyType assemblyType, Integer hwConfigId,
			ComponentData inData) {

		// HWConfiguration object need to set the Assembly.HWConfigurationId column
		HWConfiguration hwConfig = new HWConfiguration();
		hwConfig.setConfigurationId(hwConfigId);

		Assembly assembly = new Assembly();
		assembly.setAssemblyType(assemblyType);
		assembly.setHWConfiguration(hwConfig);
		assembly.setSerialNumber(inData.serialNumber);
		assembly.setData(null);
		entityManager.persist(assembly);

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Assembly " + assemblyType.getAssemblyTypeName()
				+ "with serial number " + inData.serialNumber
				+ " was added to configuration id " + hwConfig.getConfigurationId());
		}

		return assembly;
	}

	private void persistNewMonitorPoint(EntityManager entityManager,
			DefaultMonitorPoint defaultMonitorPoint,
			Integer baciPropertyId,
			Integer assemblyId) {

		// BACIProperty and Assembly objects needed to set the
		// MonitorPoint.BACIPropertyId and MonitorPoint.assemblyId columns
		BACIProperty baciProperty = new BACIProperty();
		baciProperty.setBACIPropertyId(baciPropertyId);
		Assembly assembly = new Assembly();
		assembly.setAssemblyId(assemblyId);

		MonitorPoint monitorPoint = new MonitorPoint();
		monitorPoint.setBACIProperty(baciProperty);
		monitorPoint.setMonitorPointName(defaultMonitorPoint.getMonitorPointName());
		monitorPoint.setAssembly(assembly);
		monitorPoint.setIndice(defaultMonitorPoint.getIndice());
		monitorPoint.setDataType(MonitorPointDatatype.valueOfForEnum(defaultMonitorPoint.getDataType().toString()));
		monitorPoint.setRCA(defaultMonitorPoint.getRCA());
		monitorPoint.setTeRelated(defaultMonitorPoint.getTeRelated());
		monitorPoint.setRawDataType(defaultMonitorPoint.getRawDataType());
		monitorPoint.setWorldDataType(defaultMonitorPoint.getWorldDataType());
		monitorPoint.setUnits(defaultMonitorPoint.getUnits());
		monitorPoint.setScale(defaultMonitorPoint.getScale());
		monitorPoint.setOffset(defaultMonitorPoint.getOffset());
		monitorPoint.setMinRange(defaultMonitorPoint.getMinRange());
		monitorPoint.setMaxRange(defaultMonitorPoint.getMinRange());
		monitorPoint.setDescription(defaultMonitorPoint.getDescription());
		entityManager.persist(monitorPoint);

		if ( log.isLoggable(Level.FINE) ) {
			log.fine("Monitor Point " + defaultMonitorPoint.getMonitorPointName()
					+ " has been configured.");
		}
	}

	private void persistNewMonitorData(EntityManager entityManager,
			ComponentData inData,
			MonitorCharacteristicIDs monitorCharacteristicIDs) 
			throws PersistenceException {
		MonitorData monitorData = new MonitorData();

		MonitorDataId monDataId = new MonitorDataId();
		monDataId.setMonitorPointId(monitorCharacteristicIDs.getMonitorPointId());
		monDataId.setMonitorTS(new Timestamp(System.currentTimeMillis()));
		monitorData.setId(monDataId);
		monitorData.setStartTime(inData.startTime);
		monitorData.setEndTime(inData.stopTime);
		monitorData.setSampleSize(inData.sampleSize);
		monitorData.setMonitorClob(inData.getClob());

		if (inData.statistics != null) {
			monitorData.setMinStat(inData.statistics.min.doubleValue());
			monitorData.setMaxStat(inData.statistics.max.doubleValue());
			monitorData.setMeanStat(inData.statistics.mean.doubleValue());
			monitorData.setStdDevStat(inData.statistics.stdDev.doubleValue());
		}

		entityManagerStore.persist(monitorData);
	}

	public List getMonitorData(Integer monitorPointId,
			Timestamp startTimestamp,
			Timestamp stopTimestamp) {
		EntityManager entityManager = this.myPersistenceLayer.getEntityManager();

		Query query = entityManager.createNamedQuery("findMonitorDataByMonitorPointIdAndTimestampRange");
		query.setParameter("monitorPointId", monitorPointId);
		query.setParameter("startTimestamp", startTimestamp);
		query.setParameter("stopTimestamp", stopTimestamp);

		return query.getResultList();
	}

	private void send_alarm(String faultFamily, String faultMember, int faultCode, boolean active) {
		containerServices.getAlarmSource().setAlarm(faultFamily, faultMember, faultCode, active);
	}

	
	
	
	/**
	 * Consumes data from {@link MonitorDAOImpl#myBlobDataQueue} 
	 * and stores it using {@link MonitorDAOImpl#dbStore(ComponentData)}.
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
			log.info("Starting blob consumer thread.");
			long start=0;
			long end =0;
			boolean lastPollNoOp = false;
			TransactionScope t = null;
			
			while(!shouldTerminate) {
				
				// get a transaction whose data we should process
				if (t == null) {
					t = myBlobDataQueue.getOldestTransaction();
					if (t == null) {
						lastPollNoOp = true;
						try {
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							log.log(Level.WARNING, "Unexpected InterruptedException in thread " + Thread.currentThread().getName()
									+ ". Will terminate this thread.", ex);
							break; // end the run method
						}
						continue; // keep trying to get a transaction
					}
					else {
						try {
							openDatabaseTransaction();
						} catch (AcsJDBConnectionFailureEx ex) {
							// @TODO+ Report this to the watch dog so that also the upper blobber layers get informed.
							log.log(Level.SEVERE, "Failed to open the DB transaction '" + t.getTransactionName() + "'.", ex);
						}
					}
				}
				
				ComponentData tempBlobData = null;
				
				if (!lastPollNoOp) {
					start = System.currentTimeMillis();
				}
				
				try {
					// wait for data, but only 500 ms, to re-evaluate the shouldTerminate flag often enough.
					tempBlobData = t.getDataQueue().poll(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException ex) {
					log.log(Level.WARNING, "Unexpected InterruptedException in thread " + Thread.currentThread().getName()
							+ ". Will terminate this thread.", ex);
					break; // end the run method
				}
				if (tempBlobData == null) {
					// the above poll call timed out. No data was available.
					lastPollNoOp = true;
				}
				else if (TransactionScope.isEndOfQueue(tempBlobData)) {
					// all data has been taken from the current transaction's queue
					lastPollNoOp = true;
					try {
						closeDatabaseTransaction();
						if (log.isLoggable(Level.FINE)) {
							log.fine("Done with async storing to DB of transaction '" + t.getTransactionName() + "'.");
						}
					} catch (AcsJDBConnectionFailureEx ex) {
						// @TODO+ Report this to the watch dog so that also the upper blobber layers get informed.
						log.log(Level.SEVERE, "Failed to close the DB transaction '" + t.getTransactionName() + "'.", ex);
					}
					myBlobDataQueue.removeTransaction(t);
					t = null; // so that next loop iteration asks for a new transaction.
				}
				else {
					// got data to process
					lastPollNoOp = false;
					try {
						dbStore(tempBlobData);
					} catch (Exception ex) {
						// @TODO+ Report this to the watch dog so that also the upper blobber layers get informed.
						log.log(Level.SEVERE, "Caught exception from async call to dbStore:", ex);
					}
					end = System.currentTimeMillis();
					if ( log.isLoggable(Level.FINER) )
						log.finer(Thread.currentThread().getName()
								+ ":DB consumer: data taken from the queue, queue size="
								+ myBlobDataQueue.size() + ", consume time=" + (end-start));
				}
			} // end of big while loop
			
			int queueSize = myBlobDataQueue.size();
			if (queueSize > 0) {
				log.warning("Terminating thread '" + Thread.currentThread().getName() + 
						"' while the queue still contains " + queueSize + " BlobData elements.");
			}
		}
	}

}
