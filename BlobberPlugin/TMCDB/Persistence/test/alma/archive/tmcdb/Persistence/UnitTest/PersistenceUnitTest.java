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
/**
 * This is the unit test for TMC Persistence module.
 * It uses TestNG as the testing framework.
 * @author Pablo Burgos
 * @version $Id: PersistenceUnitTest.java,v 1.9 2013/03/17 15:01:29 tstaig Exp $
 * @since ACS-8_0_0-B
 *
 */
package alma.archive.tmcdb.Persistence.UnitTest;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import alma.acs.tmcdb.Assembly;
import alma.acs.tmcdb.AssemblyType;
import alma.acs.tmcdb.AssemblyTypeBEType;
import alma.acs.tmcdb.BACIPropArchMech;
import alma.acs.tmcdb.BACIProperty;
import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.ImplLangEnum;
import alma.acs.tmcdb.ComponentType;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.DefaultBaciProperty;
import alma.acs.tmcdb.DefaultComponent;
import alma.acs.tmcdb.HWConfiguration;
import alma.acs.tmcdb.LRUType;
import alma.acs.tmcdb.MonitorData;
import alma.acs.tmcdb.MonitorDataId;
import alma.acs.tmcdb.MonitorPoint;
import alma.acs.tmcdb.MonitorPointDatatype;
import alma.archive.tmcdb.persistence.TMCDBConfig;
import alma.archive.tmcdb.persistence.TMCDBPersistence;
//
public class PersistenceUnitTest {

	private Logger log;
	private EntityManager entityManager;
	private TMCDBPersistence myPersistenceLayer;
	private String componentName = "PSA";
	private String path = "CONTROL/DV01";
	private String propertyName = "VOLTAGE_MID_1";
	private Logger logger;

	@BeforeClass(groups = { "persistence2database" })
	public void setUp() {
		logger = Logger.getAnonymousLogger();
		myPersistenceLayer = new TMCDBPersistence(logger);
		entityManager = this.myPersistenceLayer.getEntityManager();
		log = Logger.getLogger("alma.archive.tmcdb.Persistence.UnitTest");
	}

	@AfterClass(groups = { "persistence2database" })
	public void tearDown() {
		this.entityManager.close();
		this.myPersistenceLayer.close();
	}

	@Test(groups = { "persistence2database" })
	public void pojoConfigurationTest() {
		log.info(">>>>>>> pojoConfigurationTest executing... ");
		// First unit of work
		log.info("---->Testing Configuration NamedQueries class ");
		log.info("     Persisting...");

		Integer configurationId1 = new Integer(1);
		String configurationName1 = "test";
		String fullName1 = "This is the test configuration for TMCDB";
		Boolean active1 = true;
		Timestamp creationTime1 = new Timestamp(System.currentTimeMillis());
		String description1 = "Here comes the description for this TMCDB monitor configuration";

		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			Configuration configurationWrite1 = new Configuration();
//			configurationWrite1.setConfigurationId(configurationId1);
			configurationWrite1.setConfigurationName(configurationName1);
			configurationWrite1.setFullName(fullName1);
			configurationWrite1.setActive(active1);
			configurationWrite1.setCreationTime(creationTime1);
			configurationWrite1.setDescription(description1);

			entityManager.persist(configurationWrite1);
			entityTransaction.commit();
		} finally {
		}

		Integer configurationId2 = new Integer(2);
		String configurationName2 = "TMCDB";
		String fullName2 = "20090412-TMCDB-STE1";
		Boolean active2 = true;
		Timestamp creationTime2 = new Timestamp(System.currentTimeMillis());
		String description2 = "Here we are adding a second configuration";

		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			Configuration configurationWrite2 = new Configuration();
//			configurationWrite2.setConfigurationId(configurationId2);
			configurationWrite2.setConfigurationName(configurationName2);
			configurationWrite2.setFullName(fullName2);
			configurationWrite2.setActive(active2);
			configurationWrite2.setCreationTime(creationTime2);
			configurationWrite2.setDescription(description2);

			entityManager.persist(configurationWrite2);
			entityTransaction.commit();
		} finally {
		}

		log.info("     Reading from database configuration 1...");

		Configuration configurationRead1 = entityManager.find(
				Configuration.class, configurationId1);
		assert configurationRead1.getConfigurationName().equals(
				configurationName1);
		assert configurationRead1.getCreationTime().equals(creationTime1);
		assert configurationRead1.getDescription().equals(description1);
		assert configurationRead1.getActive().equals(active1);

		log.info("     Reading from database configuration2...");

		Configuration configurationRead2 = entityManager.find(
				Configuration.class, configurationId2);
		assert configurationRead2.getConfigurationName().equals(
				configurationName2);
		assert configurationRead2.getCreationTime().equals(creationTime2);
		assert configurationRead2.getDescription().equals(description2);
		assert configurationRead2.getActive() == active2;

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoConfigurationTest" })
	public void findConfigurationByNameTest() {

		Query query = entityManager.createNamedQuery("findConfigurationByName");
		query.setParameter("configurationName", TMCDBConfig.getInstance(logger)
				.getConfigurationName());
		Configuration conf = (Configuration) query.getSingleResult();
		assert conf.getConfigurationName().equals("test");
		assert conf.getConfigurationId() == 1;

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoConfigurationTest" })
	public void pojoHWConfigurationTest() {
		log.info(">>>>>>> pojoConfigurationTest executing... ");
		// First unit of work
		log.info("---->Testing Configuration NamedQueries class ");
		log.info("     Persisting...");

		Integer swConfigurationId = new Integer(0);

		try {
			Configuration config = new Configuration();
			config.setConfigurationId(swConfigurationId);
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			HWConfiguration hwConfigurationWrite1 = new HWConfiguration();
			hwConfigurationWrite1.setConfiguration(config);
			hwConfigurationWrite1.setTelescopeName("ALMA");

			entityManager.persist(hwConfigurationWrite1);
			entityTransaction.commit();
		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoConfigurationTest" })
	public void pojoComponentTypeTest() {
		String idl = "IDL:alma/Control/PSA:1.0";
		// String urn = "urn:schemas-cosylab-com:PSA:1.0";
		log.info("    About to write to DB ComponentType 1...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			ComponentType componentTypeWrite = new ComponentType();
			// componentTypeWrite.setComponentTypeId(componentTypeId);
			componentTypeWrite.setIDL(idl);
			// componentTypeWrite.setUrn(urn);

			entityManager.persist(componentTypeWrite);
			entityTransaction.commit();
		} finally {
		}
		// long componentTypeId2 = 2;
		String idl2 = "IDL:alma/Control/PSD:1.0";
		// String urn2 = "urn:schemas-cosylab-com:PSD:1.0";
		log.info("    About to write to DB ComponentType 2...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			ComponentType componentTypeWrite = new ComponentType();
			// componentTypeWrite.setComponentTypeId(componentTypeId2);
			componentTypeWrite.setIDL(idl2);
			// componentTypeWrite.setUrn(urn2);

			entityManager.persist(componentTypeWrite);
			entityTransaction.commit();
		} finally {
		}

		try {
			log.info("     Reading from database ComponentType 1...");

			Query query = entityManager
					.createNamedQuery("findComponentTypeBylikeIDL");
			query.setParameter("IDL", "%PSA%");
			ComponentType componentType = (ComponentType) query
					.getSingleResult();

			assert componentType.getIDL().equals(idl);
			// assert componentTypeRead.getUrn().equals(urn);
			// Now I retrieve the associated schema
			// Schemas schema=componentTypeRead.getSchema();
			// if (schema==null) {
			// log.info(">>>>>>>>  UPS!  Null");
			// }
			// assert schema.getUrn().equals(urn);

		} finally {
		}
		try {
			log.info("     Reading from database ComponentType 2...");
			Query query = entityManager
					.createNamedQuery("findComponentTypeBylikeIDL");
			query.setParameter("IDL", "%PSA%");
			ComponentType componentType = (ComponentType) query
					.getSingleResult();

			assert componentType.getIDL().equals(idl2);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" })
	public void pojoLRUTypeTest() {
		String lruName = "PSA";
		String fullName = "Power Supply Analog";
		String icd = "ALMA-60.40.30.20.10-D";
		long icdDate = 20080802;
		String description = "This power supply feeds the backend analog rack on each antenna";
		log.info("    About to write to DB LRUTYPE 1...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			LRUType lruTypeWrite = new LRUType();
			lruTypeWrite.setLRUName(lruName);
			lruTypeWrite.setFullName(fullName);
			lruTypeWrite.setICD(icd);
			lruTypeWrite.setICDDate(icdDate);
			lruTypeWrite.setDescription(description);

			entityManager.persist(lruTypeWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database LRUType 1...");
			LRUType lruTypeRead = entityManager.find(LRUType.class, lruName);
			assert lruTypeRead.getLRUName() == lruName;
			assert lruTypeRead.getDescription().equals(description);
			assert lruTypeRead.getNotes() == null;

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = {
			"pojoLRUTypeTest", "pojoComponentTypeTest" })
	public void pojoAssemblyTypeTest() {
		String assemblyTypeName = "PSA";
		String fullName = "Power Supply Analog";
		String lruName = "PSA";
		String baseElementType = "Antenna";
		String description = "This power supply feeds the backend analog rack on each antenna";
		String notes = "This are the boring notes";
		log.info("    About to write to DB AssemblyType 1...");

		ComponentType componentType = null;
		try {
			log.info("     Reading from database ComponentType 2...");
			Query query = entityManager
					.createNamedQuery("findComponentTypeBylikeIDL");
			query.setParameter("IDL", "%PSA%");
			componentType = (ComponentType) query.getSingleResult();

		} finally {
		}

		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			LRUType lruType = new LRUType();
			lruType.setLRUName(lruName);
			AssemblyType AssemblyTypeWrite = new AssemblyType();
			AssemblyTypeWrite.setAssemblyTypeName(assemblyTypeName);
			AssemblyTypeWrite.setBaseElementType(AssemblyTypeBEType.ANTENNA);
			AssemblyTypeWrite.setLRUType(lruType);
			AssemblyTypeWrite.setFullName(fullName);
			AssemblyTypeWrite.setDescription(description);
			AssemblyTypeWrite.setNotes(notes);
			AssemblyTypeWrite.setComponentType(componentType);
			AssemblyTypeWrite.setProductionCode("lala");
			AssemblyTypeWrite.setSimulatedCode("lalaSim");

			entityManager.persist(AssemblyTypeWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database AssemblyType 1...");

			AssemblyType AssemblyTypeRead = entityManager.find(
					AssemblyType.class, assemblyTypeName);
			assert AssemblyTypeRead.getLRUType().getLRUName().equals(lruName);
			assert AssemblyTypeRead.getDescription().equals(description);
			assert AssemblyTypeRead.getNotes().equals(notes);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = {
			"pojoAssemblyTypeTest", "pojoConfigurationTest",
			"pojoHWConfigurationTest" })
	public void pojoAssemblyTest() {
		Integer assemblyId = null;
		String assemblyTypeName = "PSA";

//		Configuration config = new Configuration();
//		config.setConfigurationId(1);
		HWConfiguration hwConf = new HWConfiguration();
		hwConf.setConfigurationId(0);
//		hwConf.setConfiguration(config);

		String serialNumber = "3456328928847";
		String data = "This assembly is one of the most important ones";
		log.info("    About to write to DB Assembly 1...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			Assembly AssemblyWrite = new Assembly();
			// AssemblyWrite.setAssemblyId(assemblyId);
			AssemblyType assemblyType = new AssemblyType();
			assemblyType.setAssemblyTypeName(assemblyTypeName);
			AssemblyWrite.setAssemblyType(assemblyType);
			AssemblyWrite.setHWConfiguration(hwConf);
			AssemblyWrite.setSerialNumber(serialNumber);
			AssemblyWrite.setData(data);

			entityManager.persist(AssemblyWrite);
			entityTransaction.commit();
		} catch (Exception e) {
			log.severe("An exception was caught with message ->"
					+ e.getMessage());

		} finally {
		}
		try {
			log.info("     Reading from database Assembly just inserted...");
			Query query = entityManager
					.createNamedQuery("findAssemblyBySerialNumberAndConfigurationId");
			query.setParameter("serialNumber", serialNumber);
			query.setParameter("hwConfigurationId", 0);
			Assembly AssemblyRead = (Assembly) query.getSingleResult();
			assemblyId = AssemblyRead.getAssemblyId();
			log.info(" AssemblyId assigned by Generator was: " + assemblyId);
			assert AssemblyRead.getSerialNumber().equals(serialNumber);
			assert AssemblyRead.getData().equals(data);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoAssemblyTest" })
	public void findAssemblyBySerialNumberAndConfigurationIdTest() {
		Query query = entityManager
				.createNamedQuery("findAssemblyBySerialNumberAndConfigurationId");
		query.setParameter("serialNumber", "3456328928847");
		query.setParameter("hwConfigurationId", 0);
		Assembly assembly = (Assembly) query.getSingleResult();
		assert assembly.getAssemblyType().getAssemblyTypeName().equals("PSA");

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = {
			"pojoComponentTypeTest", "pojoConfigurationTest" })
	public void pojoComponentTest() {
		Integer componentId = null;
		Boolean realTime = true;
		String code = "PSAImpl";

		Boolean isAutoStart = true;
		Boolean isDefault = true;
		Boolean isStandaloneDefined = true;
		Boolean isControl = true;
		int keepAliveTime = 10;
		int minLogLevel = 0;
		int minLogLevelLocal = 1;
		String xmlDoc = "here goes the xmlDoc :)";
		log.info("    About to write to DB Component 1...");

		ComponentType componentType = null;
		try {
			log.info("     Reading from database ComponentType 2...");
			Query query = entityManager
					.createNamedQuery("findComponentTypeBylikeIDL");
			query.setParameter("IDL", "%PSA%");
			componentType = (ComponentType) query.getSingleResult();
		} finally {
		}

		Configuration configuration = new Configuration();
		configuration.setConfigurationId(0);
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			Component ComponentWrite = new Component();

			ComponentWrite.setComponentType(componentType);
			ComponentWrite.setComponentName(componentName);
			ComponentWrite.setPath(path);
			ComponentWrite.setConfiguration(configuration);
			ComponentWrite.setImplLang(ImplLangEnum.CPP);
			ComponentWrite.setRealTime(realTime);
			ComponentWrite.setCode(code);
			ComponentWrite.setIsAutostart(isAutoStart);
			ComponentWrite.setIsDefault(isDefault);
			ComponentWrite.setIsControl(isControl);
			ComponentWrite.setIsStandaloneDefined(isStandaloneDefined);
			ComponentWrite.setKeepAliveTime(keepAliveTime);
			ComponentWrite.setMinLogLevel((byte) minLogLevel);
			ComponentWrite.setMinLogLevelLocal((byte) minLogLevelLocal);
			ComponentWrite.setXMLDoc(xmlDoc);
			entityManager.persist(ComponentWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database Component just inserted...");
			// Component ComponentRead = entityManager.find(Component.class,
			// componentId);
			Query query = entityManager
					.createNamedQuery("findComponentByComponentName");
			query.setParameter("componentName", componentName);
			query.setParameter("path", path);
			query.setParameter("configurationId", 0);
			Component component = (Component) query.getSingleResult();
			componentId = component.getComponentId();
			log.info(" ComponentId assigned by Generator was: " + componentId);
			assert component.getXMLDoc().equals(xmlDoc);
			assert component.getCode().equals(code);
			assert component.getComponentName().equals(componentName);
			assert component.getPath().equals(path);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoComponentTest" })
	public void findComponentByComponentNameTest() {
		Query query1 = entityManager
				.createNamedQuery("findConfigurationByName");
		query1.setParameter("configurationName", TMCDBConfig
				.getInstance(logger).getConfigurationName());
		Configuration conf = (Configuration) query1.getSingleResult();

		Query query2 = entityManager
				.createNamedQuery("findComponentByComponentName");
		query2.setParameter("path", path);
		query2.setParameter("componentName", componentName);
		query2.setParameter("configurationId", conf.getConfigurationId());
		Component comp = (Component) query2.getSingleResult();
		assert comp.getComponentName().equals(componentName);
		assert comp.getPath().equals(path);
		assert comp.getComponentId() == 1;

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoComponentTest" })
	public void findAllComponentsByConfigurationIdTest() {
		Query query = entityManager.createNamedQuery("findAllComponentsByConfigurationId");
		query.setParameter("configurationId", 0);
		List<?> componentList = query.getResultList();
		log.info("componentList(0)= "
				+ ((Component) componentList.get(0)).getComponentName());
		assert (((Component) componentList.get(0)).getComponentName()
				.equals(componentName));
		assert (((Component) componentList.get(0)).getPath().equals(path));
	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoComponentTest" })
	public void pojoBACIPropertyTest() {

		String description = "This read line 1";
		String format = "this is the form";
		String units = "volt";
		Integer resolution = new Integer(2324);
		Double archiveDelta = new Double(2.2);
		Double archiveMinInt = new Double(0.564);
		Double archiveMaxInt = new Double(0.23);
		Double defaultTimerTrig = new Double(10.3);
		Double minTimerTrig = new Double(3.3);
		Boolean initializeDEVIO = true;
		Double minDeltaTrig = new Double(0.5);
		String defaultValue = "0.0";
		Integer archivePriority = new Integer(2);

		log.info("    About to write to DB BACIProperty 1...");
		Component component = null;
		try {
			Query query = entityManager
					.createNamedQuery("findComponentByComponentName");
			query.setParameter("componentName", componentName);
			query.setParameter("path", path);
			query.setParameter("configurationId", 0);
			component = (Component) query.getSingleResult();
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			BACIProperty BACIPropertyWrite = new BACIProperty();
			BACIPropertyWrite.setComponent(component);
			BACIPropertyWrite.setPropertyName(propertyName);
			BACIPropertyWrite.setDescription(description);
			BACIPropertyWrite.setFormat(format);
			BACIPropertyWrite.setUnits(units);
			BACIPropertyWrite.setResolution(resolution.toString());
			BACIPropertyWrite.setArchive_min_int(archiveMinInt);
			BACIPropertyWrite.setArchive_max_int(archiveMaxInt);
			BACIPropertyWrite.setDefault_timer_trig(defaultTimerTrig);
			BACIPropertyWrite.setMin_timer_trig(minTimerTrig);
			BACIPropertyWrite.setArchive_delta(archiveDelta);
			BACIPropertyWrite.setInitialize_devio(initializeDEVIO);
			BACIPropertyWrite.setMin_delta_trig(minDeltaTrig);
			BACIPropertyWrite.setDefault_value(defaultValue);
			BACIPropertyWrite.setArchive_priority(archivePriority);
			BACIPropertyWrite.setArchive_mechanism(BACIPropArchMech.MONITOR_COLLECTOR);
			BACIPropertyWrite.setArchive_suppress(false);

			entityManager.persist(BACIPropertyWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database BACIProperty 1...");

			Query query = entityManager
					.createNamedQuery("findBACIPropertyIdByPropertyNameANDComponentId");
			query.setParameter("componentId", component.getComponentId());
			query.setParameter("propertyName", propertyName);
			BACIProperty baciProperty = (BACIProperty) query.getSingleResult();

			assert baciProperty.getPropertyName().equals(propertyName);
			assert baciProperty.getFormat().equals(format);
			assert baciProperty.getArchive_min_int().equals(archiveMinInt);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoBACIPropertyTest" })
	public void findBACIPropertyIdByPropertyNameANDComponentIdTest() {
		Query query = entityManager
				.createNamedQuery("findComponentByComponentName");
		query.setParameter("componentName", componentName);
		query.setParameter("path", path);
		query.setParameter("configurationId", 0);
		Component component = (Component) query.getSingleResult();

		query = entityManager
				.createNamedQuery("findBACIPropertyIdByPropertyNameANDComponentId");
		query.setParameter("componentId", component.getComponentId());
		query.setParameter("propertyName", "VOLTAGE_MID_1");
		BACIProperty baciProp = (BACIProperty) query.getSingleResult();
		assert baciProp.getResolution().equals(new Integer(2324));
		assert baciProp.getBACIPropertyId().equals(new Integer(1));

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = {
			"pojoAssemblyTest", "pojoBACIPropertyTest" })
	public void pojoMonitorPointTest() {
		int baciPropertyId = 0;
		String monitorPointName = "VOLTAGE_MID_1";
		int assemblyId = 0;
		int index = 0;
		String rca = "0x30";
		Boolean teRelated = true;
		String rawDatatype = "unit[8]";
		String worldDatatype = "float";
		String description = "Finally I'm finishing with this persistence classes";
		String minRange = "3.1416152";
		log.info("    About to write to DB MonitorPoint 1...");
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();

		BACIProperty baciProperty = new BACIProperty();
		baciProperty.setBACIPropertyId(baciPropertyId);
		MonitorPoint MonitorPointWrite = new MonitorPoint();
		MonitorPointWrite.setBACIProperty(baciProperty);
		MonitorPointWrite.setMonitorPointName(monitorPointName);
		Assembly assembly = new Assembly();
		assembly.setAssemblyId(assemblyId);
		MonitorPointWrite.setAssembly(assembly);
		MonitorPointWrite.setIndice(index);
		MonitorPointWrite.setDataType(MonitorPointDatatype.FLOAT);
		MonitorPointWrite.setRCA(rca);
		MonitorPointWrite.setTeRelated(teRelated);
		MonitorPointWrite.setRawDataType(rawDatatype);
		MonitorPointWrite.setWorldDataType(worldDatatype);
		MonitorPointWrite.setDescription(description);
		MonitorPointWrite.setMinRange(minRange);
		entityManager.persist(MonitorPointWrite);
		entityTransaction.commit();

		log.info("     Reading from database MonitorPoint 1...");

		MonitorPoint MonitorPointRead = entityManager.find(MonitorPoint.class,
				1);
		assert MonitorPointRead.getBACIProperty().getBACIPropertyId() == baciPropertyId;
		assert MonitorPointRead.getMonitorPointName().equals(monitorPointName);
		assert MonitorPointRead.getDescription().equals(description);
		assert MonitorPointRead.getMinRange() == minRange;
		assert MonitorPointRead.getIndice().equals(index);

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoMonitorPointTest" })
	public void findMonitorPointIdByAssemblyIdANDBACIPropertyIdTest() {
		Query query1 = entityManager
				.createNamedQuery("findMonitorPointIdByAssemblyIdANDBACIPropertyId");
		query1.setParameter("assemblyId", 0);
		query1.setParameter("BACIPropertyId", 0);
		MonitorPoint mp = (MonitorPoint) query1.getSingleResult();
		assert mp.getMonitorPointId() == 1;
		assert mp.getMonitorPointName().equals("VOLTAGE_MID_1");

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoMonitorPointTest" })
	public void pojoMonitorDataTest() {
		int monitorPointId = 0;
		// Timestamp monitorTS = new Timestamp(System.currentTimeMillis());
		Timestamp monitorTS = new Timestamp(1234567890123456790L);
		long startTime = 1234567890123456789L;
		long endTime = 1234567890123456791L;
		// Timestamp monitorTS = new Timestamp(System.currentTimeMillis());
		int sampleSize = 10;
		String monitorClob = "kfebvjebvejb";
		double minStat = 2.3;
		double maxStat = 2.343244;
		double meanStat = 2.11112;
		log.info("    About to write to DB MonitorData 1...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			MonitorData MonitorDataWrite = new MonitorData();
			MonitorDataId monDataId = new MonitorDataId();
			monDataId.setMonitorPointId(monitorPointId);
			monDataId.setMonitorTS(monitorTS);
			MonitorDataWrite.setId(monDataId);
			MonitorDataWrite.setStartTime(startTime);
			MonitorDataWrite.setEndTime(endTime);
			MonitorDataWrite.setSampleSize(sampleSize);
			MonitorDataWrite.setMonitorClob(monitorClob);
			MonitorDataWrite.setMinStat(minStat);
			MonitorDataWrite.setMaxStat(maxStat);
			MonitorDataWrite.setMeanStat(meanStat);

			entityManager.persist(MonitorDataWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database MonitorData 1...");
			MonitorDataId monitorDataKey = new MonitorDataId();
			monitorDataKey.setMonitorPointId(1);
			monitorDataKey.setMonitorTS(monitorTS);
			MonitorData MonitorDataRead = entityManager.find(MonitorData.class,
					monitorDataKey);
			assert MonitorDataRead.getMinStat() == minStat;
			assert MonitorDataRead.getMonitorClob().equals(monitorClob);
			assert MonitorDataRead.getId().getMonitorTS().equals(monitorTS);
			assert MonitorDataRead.getStartTime() == startTime;

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoMonitorDataTest" })
	public void findMonitorDataByMonitorPointIdAndTimestampRange() {
		Query query1 = entityManager
				.createNamedQuery("findMonitorDataByMonitorPointIdAndTimestampRange");
		query1.setParameter("monitorPointId", 0);
		query1.setParameter("startTimestamp", new Timestamp(
				1234567890123456789L));
		query1.setParameter("stopTimestamp",
				new Timestamp(1234567890123456791L));

		MonitorData md = (MonitorData) query1.getSingleResult();
		assert md.getId().getMonitorPointId() == 1;
		assert md.getMonitorClob().equals("kfebvjebvejb");

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoMonitorDataTest" })
	public void getMaxRowResultsMonitorData() {
		Query query1 = entityManager
				.createNamedQuery("getMaxRowResultsMonitorData");
		query1.setParameter("monitorPointId", 0);
		query1.setParameter("startTimestamp", new Timestamp(
				1234567890123456789L));
		query1.setParameter("stopTimestamp",
				new Timestamp(1234567890123456791L));
		Long result = (Long) query1.getSingleResult();
		assert (result.equals(1L));

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoMonitorDataTest" })
	public void getMaxSampleResultsMonitorData() {
		Query query1 = entityManager
				.createNamedQuery("getMaxSampleResultsMonitorData");
		query1.setParameter("monitorPointId", 0);
		query1.setParameter("startTimestamp", new Timestamp(
				1234567890123456789L));
		query1.setParameter("stopTimestamp",
				new Timestamp(1234567890123456791L));
		Long result = (Long) query1.getSingleResult();
		assert (result.equals(10L));

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = {
			"pojoAssemblyTypeTest", "pojoComponentTypeTest" })
	public void pojoDefaultComponentTest() {
		int defaultComponentId = 1;
		Integer componentTypeId = null;
		log.info("    About to write to DB AssemblyType 1...");

		ComponentType componentType = null;
		try {
			log.info("     Reading from database ComponentType 2...");
			Query query = entityManager
					.createNamedQuery("findComponentTypeBylikeIDL");
			query.setParameter("IDL", "%PSA%");
			componentType = (ComponentType) query
					.getSingleResult();

		} finally {
		}
		String assemblyTypeName = "PSA";
		Boolean realTime = true;
		String code = "PSAImpl";
		String path = "This is the path";
		Boolean isAutoStart = true;
		Boolean isDefault = true;
		Boolean isStandaloneDefined = true;
		int keepAliveTime = 10;
		int minLogLevel = 0;
		int minLogLevelLocal = 1;
		String xmlDoc = "here goes the xmlDoc :)";
		// String idl="IDL:alma/Control/PSA:1.0";

		log.info("    About to write defaultComponent");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			AssemblyType assemblyType = new AssemblyType();
			assemblyType.setAssemblyTypeName(assemblyTypeName);
			DefaultComponent DefaultComponentWrite = new DefaultComponent();
			DefaultComponentWrite.setDefaultComponentId(defaultComponentId);
			DefaultComponentWrite.setComponentType(componentType);
			DefaultComponentWrite.setAssemblyType(assemblyType);
			DefaultComponentWrite.setImplLang(ImplLangEnum.CPP);
			DefaultComponentWrite.setRealTime(realTime);
			DefaultComponentWrite.setCode(code);
			DefaultComponentWrite.setPath(path);
			DefaultComponentWrite.setIsAutostart(isAutoStart);
			DefaultComponentWrite.setIsDefault(isDefault);
			DefaultComponentWrite.setIsStandaloneDefined(isStandaloneDefined);
			DefaultComponentWrite.setKeepAliveTime(keepAliveTime);
			DefaultComponentWrite.setMinLogLevel((byte) minLogLevel);
			DefaultComponentWrite.setMinLogLevelLocal((byte) minLogLevelLocal);
			DefaultComponentWrite.setXMLDoc(xmlDoc);
			// DefaultComponentWrite.setIdl(idl);

			entityManager.persist(DefaultComponentWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database MonitorData 1...");

			DefaultComponent DefaultComponentRead = entityManager.find(
					DefaultComponent.class, defaultComponentId);
			assert DefaultComponentRead.getComponentType().getComponentTypeId()
					.equals(1);
			assert DefaultComponentRead.getAssemblyType().getAssemblyTypeName().equals("PSA");

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoDefaultComponentTest" })
	public void findDefaultComponentByLikeAssemblyTypeName() {

		Query query = entityManager
				.createNamedQuery("findDefaultComponentByLikeAssemblyTypeName");
		query.setParameter("assemblyTypeName", "%PS%");
		DefaultComponent defaultComponent = (DefaultComponent) query
				.getSingleResult();
		assert defaultComponent.getAssemblyType().getAssemblyTypeName().equals("PSA");
		assert defaultComponent.getComponentType().getComponentTypeId().equals(1);

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoComponentTypeTest" })
	public void findComponentTypeBylikeIDL() {
		Integer componentTypeId = 1;
		String idl = "IDL:alma/Control/PSA:1.0";
		// String urn = "urn:schemas-cosylab-com:PSA:1.0";
		Query query = entityManager
				.createNamedQuery("findComponentTypeBylikeIDL");
		query.setParameter("IDL", "%PSA%");
		ComponentType componentType = (ComponentType) query.getSingleResult();
		assert componentType.getComponentTypeId() == componentTypeId;
		assert componentType.getIDL().equals(idl);
		// assert componentType.getUrn().equals(urn);
	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoAssemblyTest" })
	public void findAssemblyByAssemblyIdAndConfigurationIdTest() {
		
		Query query = entityManager
				.createNamedQuery("findAssemblyByAssemblyIdAndConfigurationId");
		query.setParameter("assemblyId", 0);
		query.setParameter("hwConfigurationId", 0);
		Assembly assembly = (Assembly) query.getSingleResult();
		assert assembly.getAssemblyType().getAssemblyTypeName().equals("PSA");
	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoDefaultComponentTest" })
	public void pojoDefaultBACIPropertyTest() {
		Integer defaultbaciPropId = new Integer(1);
		Integer defaultComponentId = new Integer(1);
		String propertyName = "VOLTAGE_MID_1";
		String description = "This read line 1";
		String format = "this is the form";
		String units = "volt";
		Integer resolution = new Integer(2324);
		Double archiveDelta = new Double(2.2);
		Double archiveMinInt = new Double(0.564);
		Double archiveMaxInt = new Double(0.23);
		Double defaultTimerTrig = new Double(10.3);
		Double minTimerTrig = new Double(3.3);
		Boolean initializeDEVIO = true;
		Double minDeltaTrig = new Double(0.5);
		String defaultValue = "0.0";
		Integer archivePriority = new Integer(2);

		log.info("    About to write to DB  Default BACIProperty 1...");
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			entityTransaction.begin();
			DefaultComponent defaultComp = new DefaultComponent();
			defaultComp.setDefaultComponentId(defaultComponentId);
			DefaultBaciProperty defaultBACIPropertyWrite = new DefaultBaciProperty();
			defaultBACIPropertyWrite.setDefaultBaciPropId(defaultbaciPropId);
			defaultBACIPropertyWrite.setDefaultComponent(defaultComp);
			defaultBACIPropertyWrite.setPropertyName(propertyName);
			defaultBACIPropertyWrite.setDescription(description);
			// defaultBACIPropertyWrite.setIsSequence(isSequence);
			defaultBACIPropertyWrite.setFormat(format);
			defaultBACIPropertyWrite.setUnits(units);
			defaultBACIPropertyWrite.setResolution(resolution.toString());
			defaultBACIPropertyWrite.setArchive_min_int(archiveMinInt);
			defaultBACIPropertyWrite.setArchive_max_int(archiveMaxInt);
			defaultBACIPropertyWrite.setDefault_timer_trig(defaultTimerTrig);
			defaultBACIPropertyWrite.setMin_timer_trig(minTimerTrig);
			defaultBACIPropertyWrite.setArchive_delta(archiveDelta);
			defaultBACIPropertyWrite.setInitialize_devio(initializeDEVIO);
			defaultBACIPropertyWrite.setMin_delta_trig(minDeltaTrig);
			defaultBACIPropertyWrite.setDefault_value(defaultValue);
			defaultBACIPropertyWrite.setArchive_priority(archivePriority);
			defaultBACIPropertyWrite.setArchive_mechanism("notification_channel");
			defaultBACIPropertyWrite.setArchive_suppress(false);

			entityManager.persist(defaultBACIPropertyWrite);
			entityTransaction.commit();
		} finally {
		}
		try {
			log.info("     Reading from database BACIProperty 1...");

			DefaultBaciProperty defaultBACIPropertyRead = entityManager.find(
					DefaultBaciProperty.class, defaultbaciPropId);
			assert defaultBACIPropertyRead.getPropertyName().equals(propertyName);
			assert defaultBACIPropertyRead.getFormat().equals(format);
			assert defaultBACIPropertyRead.getArchive_min_int().equals(archiveMinInt);
			// assert
			// defaultBACIPropertyRead.getIsSequence().equals(isSequence);

		} finally {
		}

	}

	@Test(groups = { "persistence2database" }, dependsOnMethods = { "pojoDefaultBACIPropertyTest" })
	public void findDefaultBACIPropertyByDefaultComponentIdTest() {
		Query query = entityManager
				.createNamedQuery("findDefaultBACIPropertyByDefaultComponentId");
		query.setParameter("defaultComponentId", 1);
		query.setParameter("propertyName", "VOLTAGE_MID_1");
		DefaultBaciProperty defaultBACIProperty = (DefaultBaciProperty) query
				.getSingleResult();
		assert defaultBACIProperty.getPropertyName().equals("VOLTAGE_MID_1");
	}
}
