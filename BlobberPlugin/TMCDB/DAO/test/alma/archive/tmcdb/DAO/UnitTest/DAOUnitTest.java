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
/** DAOUnitTest implements the unit test for testing DAO Layer.
 * For testing, TestNG is in use.
 * This test persists information into HSQLDB database, verifying after that
 * the consistency of such insertions.
 *
 * @author Pablo Burgos
 * @since ACS-8_0_0-B Jun2009
 * @version "@(#) $Id: DAOUnitTest.java,v 1.14 2012/02/29 17:24:35 hsommer Exp $
 */
package alma.archive.tmcdb.DAO.UnitTest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import alma.acs.container.testsupport.DummyContainerServices;
import alma.acs.monitoring.DAO.ComponentData;
import alma.acs.monitoring.DAO.ComponentStatistics;
import alma.acs.monitoring.DAO.MonitorDAO;
import alma.acs.monitoring.blobber.BlobberWatchDogAlmaImpl;
import alma.acs.tmcdb.Assembly;
import alma.acs.tmcdb.MonitorData;
import alma.archive.tmcdb.DAO.MonitorCharacteristicIDs;
import alma.archive.tmcdb.DAO.MonitorDAOImpl;
import alma.archive.tmcdb.DAO.queries.QueryDAO;
import alma.archive.tmcdb.DAO.queries.QueryDAOImpl;
import alma.archive.tmcdb.MQDAO.MQDAOImpl;
import alma.archive.tmcdb.persistence.TMCDBPersistence;

public class DAOUnitTest {
	private String basedir;
	private JdbcDatabaseTester dbTester;
	private Timestamp startTimestamp;
	private Timestamp middleTimestamp;
	private Timestamp stopTimestamp;
	private static String testCLOB = "|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5|276544325617189181|5.5";

	private static java.util.logging.Logger log = Logger.getLogger("alma.archive.tmcdb.DAO.UnitTest.DAOUnitTest");

	private DummyContainerServices containerServices = new DummyContainerServices(DAOUnitTest.class.getSimpleName(),log) {
		private ThreadFactory tf = new ThreadFactory() {
			public Thread newThread(Runnable r) {
				return new Thread(r);
			}
		};
		public ThreadFactory getThreadFactory() {
			return tf;
		}
	};

	@Parameters({"basedir"})
	@BeforeClass(groups = {"dao2database"})
	public void loadDB(String basedir) throws Exception {
		this.basedir = basedir;
		dbTester = new JdbcDatabaseTester("org.hsqldb.jdbc.JDBCDriver",
				"jdbc:hsqldb:hsql://localhost/tmcdb", "sa", "");
		System.out.println(">>DatabaseTest >> basedir=" + basedir);
		IDataSet dataSet = new FlatXmlDataSet(new File(basedir
				+ "/resources/BaselineData/BaselineDataAutoconfigurationTMC.xml"));
		IDatabaseConnection connection = dbTester.getConnection();
		try {
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
		} finally {
			connection.close();
		}

	}

	@AfterClass(groups = {"dao2database"})
	public void tearDown() throws Exception {
		dbTester.onTearDown();

	}

	@Test(groups ={"dao2database"}, dependsOnMethods = {"getMonitorCharacteristicIDsTest"})
	public void getAssemblyIdTest(){
		TMCDBPersistence myPersistenceLayer = new TMCDBPersistence(log);
		EntityManager entityManagerStore = myPersistenceLayer.getEntityManager();
		Query query = entityManagerStore.createNamedQuery("findAssemblyBySerialNumberAndConfigurationId");
		query.setParameter("serialNumber", "7979797979");
		query.setParameter("hwConfigurationId", 1);
		Assembly assembly = (Assembly) query.getSingleResult();
		String assemblyTypeName = assembly.getAssemblyType().getAssemblyTypeName();
		assert (assemblyTypeName.equalsIgnoreCase("ACME"));
	}


	@Test(groups = {"dao2database"})
	public void getMonitorCharacteristicIDsTest() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		TMCDBPersistence myPersistenceLayer = new TMCDBPersistence(log);
		EntityManager entityManagerStore = myPersistenceLayer.getEntityManager();
		BlobberWatchDogAlmaImpl watchDog = new BlobberWatchDogAlmaImpl(containerServices);
		MonitorDAO monitorDAO = new MonitorDAOImpl(containerServices, watchDog);
		Object monitorCharacteristicIDsObject = null;
		Object monitorCharacteristicIDsObject2 = null;
		/**
		 * Test:
		 * We test a new blob: assembly, component, property and monitor point are unknown
		 * getMonitorCharacteristicIDs() takes care of autoconfiguring the assembly
		 *	  Consist on assert:
		 *					   componentId
		 *					   assemblyId
		 *					   baciPropertyId
		 *					   monitorPointId
		 *					   isOnDB (must be false in this case)
		 */

		MockComponentData componentData = new MockComponentData(null, log);
		componentData.componentName = "CONTROL/DV03/ACME";
		componentData.index = 0;
		componentData.serialNumber = "7979797979";
		componentData.propertyName = "TEMP";

		final Method getMonitorCharacteristicIDs = MonitorDAOImpl.class.getDeclaredMethod("getMonitorCharacteristicIDs",
				EntityManager.class, String.class, ComponentData.class);
		getMonitorCharacteristicIDs.setAccessible(true);

		monitorCharacteristicIDsObject = getMonitorCharacteristicIDs.invoke(monitorDAO, entityManagerStore, "test",
				componentData);

		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject).isOnDB() == false);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject).getMonitorPointId() == 1);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject).getBACIPropertyId() == 40);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject).getComponentId() == 1);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject).getAssemblyId() == 1);
		MockComponentData componentData2 = new MockComponentData(null, log);
		componentData2.componentName = "CONTROL/DV04/ACME";
		componentData2.index = 0;
		componentData2.serialNumber = "12345678";
		componentData2.propertyName = "FREQ";
		monitorCharacteristicIDsObject2 = getMonitorCharacteristicIDs.invoke(monitorDAO, entityManagerStore, "test",
				componentData2);

		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).isOnDB() == false);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getMonitorPointId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getBACIPropertyId() == 43);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getComponentId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getAssemblyId() == 2);

		monitorCharacteristicIDsObject2 = getMonitorCharacteristicIDs.invoke(monitorDAO, entityManagerStore, "test",
				componentData2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).isOnDB() == true);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getMonitorPointId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getBACIPropertyId() == 43);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getComponentId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getAssemblyId() == 2);
	}

	@Test(groups = {"dao2database"}, dependsOnMethods = {"getAssemblyIdTest"})
	public void storeTest() throws Exception {
		// Please note that the timestamp field is managed by the DAO.
		// Since the timestamp is a key for the MonitorData Table, and this key
		// is cahnging each time I do an insert
		// we can repeat the command store many times as follows.
		Object monitorCharacteristicIDsObject2 = null;
		TMCDBPersistence myPersistenceLayer = new TMCDBPersistence(log);
		EntityManager entityManagerStore = myPersistenceLayer.getEntityManager();
		startTimestamp = new Timestamp(System.currentTimeMillis());
		BlobberWatchDogAlmaImpl watchDog = new BlobberWatchDogAlmaImpl(containerServices);
		MonitorDAO monitorDAO = new MonitorDAOImpl(containerServices, watchDog);
		MockComponentData data = new MockComponentData(testCLOB, log);
		data.componentName = "CONTROL/DV04/ACME";
		data.index = 0;
		data.propertyName = "FREQ";
		data.serialNumber = "12345678";
		data.startTime = 276544325617189181L;
		data.stopTime = 276544325617189190L;
		final Method getMonitorCharacteristicIDs = MonitorDAOImpl.class.getDeclaredMethod("getMonitorCharacteristicIDs",
				EntityManager.class, String.class, ComponentData.class);
		getMonitorCharacteristicIDs.setAccessible(true);

		monitorCharacteristicIDsObject2 = getMonitorCharacteristicIDs.invoke(monitorDAO, entityManagerStore, "test", data);

		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).isOnDB() == true);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getMonitorPointId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getBACIPropertyId() == 43);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getComponentId() == 2);
		assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getAssemblyId() == 2);

		Thread.sleep(3000);
		Thread watchDogThread = containerServices.getThreadFactory().newThread(watchDog);
		watchDogThread.start();
		monitorDAO.openTransactionStore("MyOneAndOnlyStoreTestTransaction");
		assert (watchDog.getQueueSize("db") == 0L);

		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(3000);

		middleTimestamp = new Timestamp(System.currentTimeMillis());

		ComponentStatistics stats = new ComponentStatistics();
		stats.max = new Integer(10);
		stats.mean = new Integer(5);
		stats.min = new Integer(2);
		stats.stdDev = new Double(2.3);
		data.statistics = stats;

		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(3000);

		monitorDAO.closeTransactionStore();
		assert (watchDog.getQueueSize("db") == 0L);
		watchDogThread.interrupt();
		watchDog = null;

		stopTimestamp = new Timestamp(System.currentTimeMillis());	 
	}

	@Test(groups = {"dao2database"}, dependsOnMethods = {"storeTest"})
	public void monitorDAOConformanceTest() {
		// Since I baselined the data, I know that the monitorpointid=2 for the
		// DUT
		QueryDAO queryDAO = new QueryDAOImpl(log);
		List monitorDataList=null;
		monitorDataList = queryDAO.getMonitorDataList(2, startTimestamp,
				middleTimestamp);

		if (monitorDataList != null) {
			Iterator i = monitorDataList.iterator();
			int counter = 0;
			while (i.hasNext()) {
				counter++;
				MonitorData monitorData = (MonitorData) i.next();
				assert monitorData.getStartTime() == 276544325617189181L;
				assert monitorData.getMonitorClob().equals(testCLOB);
				assert monitorData.getSampleSize() == 19;
			}

			assert counter == 6;

			List monitorDataList2 = queryDAO.getMonitorDataList(2,
					middleTimestamp, stopTimestamp);

			i = monitorDataList2.iterator();
			counter = 0;
			while (i.hasNext()) {
				counter++;
				MonitorData monitorData = (MonitorData) i.next();
				assert monitorData.getStartTime() == 276544325617189181L;
				assert monitorData.getMonitorClob().equals(testCLOB);
				assert monitorData.getSampleSize() == 19;
				assert monitorData.getMinStat() == 2;
				assert monitorData.getMaxStat() == 10;
				assert monitorData.getMeanStat() == 5;
				assert monitorData.getStdDevStat() == 2.3;
			}
			assert counter == 4;
		}  
	}

	@Test(groups = {"dao2database"})
	public void mqStoreTest() throws Exception {
		startTimestamp = new Timestamp(System.currentTimeMillis());
		BlobberWatchDogAlmaImpl watchDog = new BlobberWatchDogAlmaImpl(containerServices);
		MonitorDAO monitorDAO = new MQDAOImpl(containerServices, watchDog);
		MockComponentData data = new MockComponentData(testCLOB, log);
		data.componentName = "CONTROL/DV04/ACME";
		data.index = 0;
		data.propertyName = "FREQ";
		data.serialNumber = "12345678";
		data.startTime = 276544325617189181L;
		data.stopTime = 276544325617189190L;

		//TODO monitorCharacteristicIDsObject2 = getMonitorCharacteristicIDs.invoke(monitorDAO, entityManagerStore, "test", data);
		//assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).isOnDB() == true);
		//assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getMonitorPointId() == 2);
		//assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getBACIPropertyId() == 43);
		//assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getComponentId() == 2);
		//assert (((MonitorCharacteristicIDs) monitorCharacteristicIDsObject2).getAssemblyId() == 2);

		Thread.sleep(3000);
		Thread watchDogThread = containerServices.getThreadFactory().newThread(watchDog);
		watchDogThread.start();
		monitorDAO.openTransactionStore("DummyTransactionNameNotUsed");
		assert (watchDog.getQueueSize("db") == 0L);

		middleTimestamp = new Timestamp(System.currentTimeMillis());

		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(1000);
		monitorDAO.store(data);
		Thread.sleep(3000);

		monitorDAO.closeTransactionStore();
		assert (watchDog.getQueueSize("db") == 0L);
		watchDogThread.interrupt();
		watchDog = null;

		stopTimestamp = new Timestamp(System.currentTimeMillis());	 
	}

    /*
     * QueryDAO test
    */
    /*
   @Test(groups = {"dao2database"}, dependsOnMethods = {"monitorDAOTest"})
   public void getLocationsTest() {
       QueryDAO queryDAO = new QueryDAOImpl(log);
       List locations = queryDAO.getLocations();

       assert locations.contains("DV01");
       assert locations.contains("DV02");

   }

   @Test(groups = {"dao2database"}, dependsOnMethods = {"monitorDAOTest"})
   public void getComponentNameTest() {
       QueryDAO queryDAO = new QueryDAOImpl(log);
       String componentName1 = queryDAO.getComponentName("3456328928847");

       assert componentName1.equals("CONTROL/DV01/PSA");

       String componentName2 = queryDAO.getComponentName("23424422344");

       assert componentName2.equals("CONTROL/DV02/ACME");

   }

   @Test(groups = {"dao2database"}, dependsOnMethods = {"monitorDAOTest"})
   public void getSerialNumberTest() {
       QueryDAO queryDAO = new QueryDAOImpl(log);
       String serialNumber1 = queryDAO.getSerialNumber("CONTROL/DV01/PSA");
       assert serialNumber1.equals("3456328928847");

       String serialNumber2 = queryDAO.getSerialNumber("CONTROL/DV02/ACME");
       assert serialNumber2.equals("23424422344");
   }

   @Test(groups = {"dao2database"}, dependsOnMethods = {"monitorDAOTest"})
   public void getAllSerialNumbersTest() {
       QueryDAO queryDAO = new QueryDAOImpl(log);
       ArrayList<String> allSerialNumberList = queryDAO.getAllSerialNumbers();
       assert allSerialNumberList.contains("3456328928847");
       assert allSerialNumberList.contains("23424422344");
   } */
    /**
     * @Test(groups = {"dao2database"}, dependsOnMethods = {"monitorDAOTest"})
     *              public void getMonitorDataTest(){ long monitorPointId= }
     */

	private static class MockComponentData extends ComponentData {

		private final String mockClob;

		public MockComponentData(String clob, Logger logger) {
			super(null, logger);
			this.mockClob = clob;
		}

		@Override
		public String getClob() {
			return mockClob;
		}
	}
}
