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
package alma.archive.tmcdb.DAO.queries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import alma.acs.tmcdb.Assembly;
import alma.acs.tmcdb.BACIProperty;
import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.HWConfiguration;
import alma.acs.tmcdb.MonitorPoint;
import alma.archive.tmcdb.persistence.TMCDBConfig;
import alma.archive.tmcdb.persistence.TMCDBPersistence;

public class QueryDAOImpl implements QueryDAO {
	private Logger log;

	private final TMCDBPersistence myPersistenceLayer;

	private final String myConfigName;

	public QueryDAOImpl(Logger inLogger) {
		this.log = inLogger;
		myPersistenceLayer = new TMCDBPersistence(inLogger);
		myConfigName = TMCDBConfig.getInstance(log).getConfigurationName();
	}

	/** =================== **/

	public List getMonitorDataList(Integer monitorPointId,
			Timestamp startTimestamp, Timestamp stopTimestamp) {
		List resultList = null;
		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query6 = entityManager
					.createNamedQuery("findMonitorDataByMonitorPointIdAndTimestampRange");
			query6.setParameter("monitorPointId", monitorPointId);
			query6.setParameter("startTimestamp", startTimestamp);
			query6.setParameter("stopTimestamp", stopTimestamp);
			try {
				resultList = query6.getResultList();
			} catch (NoResultException e) {
			}
		} finally {
			entityManager.close();
		}
		return resultList;
	}

	/** ==================== **/

	public ArrayList<String> getLocations() {
		ArrayList<String> locations = new ArrayList<String>();
		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query1 = entityManager
					.createNamedQuery("findConfigurationByName");
			query1.setParameter("configurationName", this.myConfigName);
			Configuration conf = (Configuration) query1.getSingleResult();
			Integer configurationId = conf.getConfigurationId();

			Query query2 = entityManager
					.createNamedQuery("findAllComponentsByConfigurationId");
			query2.setParameter("configurationId", configurationId);
			List componentList = query2.getResultList();

			for (Object component : componentList) {
				if (((Component) component).getComponentName().indexOf(
						"CONTROL") != -1) {
					int firstSlash = ((Component) component).getComponentName()
							.indexOf("/");
					int secondSlash = ((Component) component)
							.getComponentName().indexOf("/", firstSlash + 1);
					log.info("ComponentName="
							+ ((Component) component).getComponentName());
					log.info("firstSlash=" + firstSlash);
					log.info("secondSlash=" + secondSlash);
					if (firstSlash != -1 && secondSlash != -1) {
						String location = ((Component) component)
								.getComponentName().substring(firstSlash + 1,
										secondSlash);
						log.info("location=" + location);
						if (!locations.contains(location)) {
							locations.add(location);
						}

					}
				} else if (((Component) component).getComponentName().indexOf(
						"AOSTiming") != -1) {
					if (!locations.contains("AOSTiming")) {
						locations.add("AOSTiming");
					}

				} else if (((Component) component).getComponentName().indexOf(
						"CentralLO") != -1) {
					if (!locations.contains("CentralLO")) {
						locations.add("CentralLO");
					}
				}
			}
		} finally {
			entityManager.close();
		}
		return locations;
	}

	public String getComponentName(String serialNumber, String configurationName) {

		String outValue = "";
		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();

		try {
			Query query0 = entityManager
					.createNamedQuery("findConfigurationByName");
			query0.setParameter("configurationName", configurationName);
			Configuration configuration = (Configuration) query0
					.getSingleResult();
			Integer configurationId = configuration.getConfigurationId();

			Query query1 = entityManager
					.createNamedQuery("findAssemblyBySerialNumberAndConfigurationId");
			query1.setParameter("serialNumber", serialNumber);
			query1.setParameter("configurationId", configurationId);
			Assembly assembly = (Assembly) query1.getSingleResult();

			Integer assemblyId = assembly.getAssemblyId();

			Query query2 = entityManager
					.createNamedQuery("findMonitorPointByAssemblyId");
			query2.setParameter("assemblyId", assemblyId);
			List monitorPoints = query2.getResultList();

			Integer baciPropertyId = ((MonitorPoint) monitorPoints.get(0))
					.getBACIProperty().getBACIPropertyId();

			Query query3 = entityManager
					.createNamedQuery("findBACIPropertyByBaciPropertyId");
			query3.setParameter("baciPropertyId", baciPropertyId);
			BACIProperty baciProperty = (BACIProperty) query3.getSingleResult();

			Integer componentId = baciProperty.getComponent().getComponentId();

			Query query4 = entityManager
					.createNamedQuery("findComponentByComponentId");
			query4.setParameter("componentId", componentId);
			Component component = (Component) query4.getSingleResult();

			outValue = component.getComponentName();
		} finally {
			entityManager.close();
		}
		return outValue;
	}

	public String getComponentName(String serialNumber) {
		return getComponentName(serialNumber, this.myConfigName);
	}

	public String getSerialNumber(String componentName, String configurationName) {
		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();

		String outValue = "";
		try {
			Query query0 = entityManager
					.createNamedQuery("findConfigurationByName");
			query0.setParameter("configurationName", configurationName);
			Configuration configuration = (Configuration) query0
					.getSingleResult();
			Integer configurationId = configuration.getConfigurationId();
			log.info("getSerialNumber---> configurationId=" + configurationId);

			query0 = entityManager.createNamedQuery("findHwConfBySwConfigId");
			query0.setParameter("swConfigurationId", configurationId);
			HWConfiguration hwConf = (HWConfiguration) query0.getSingleResult();

			Query query1 = entityManager
					.createNamedQuery("findComponentByComponentName");
			query1.setParameter("componentName", componentName);
			query1.setParameter("configurationId", configurationId);
			Component component = (Component) query1.getSingleResult();

			Integer componentId = component.getComponentId();
			log.info("getSerialNumber---> componentId=" + componentId);

			Query query2 = entityManager
					.createNamedQuery("findBACIPropertyByComponentId");
			query2.setParameter("componentId", componentId);
			List baciPropertyList = query2.getResultList();

			Integer baciPropertyId = ((BACIProperty) baciPropertyList.get(0))
					.getBACIPropertyId();
			log.info("getSerialNumber---> baciPropertyId=" + baciPropertyId);

			Query query3 = entityManager
					.createNamedQuery("findMonitorPointByBACIPropertyId");
			query3.setParameter("baciPropertyId", baciPropertyId);
			List monitorPointList = query3.getResultList();

			Integer assemblyId = ((MonitorPoint) monitorPointList.get(0))
					.getAssembly().getAssemblyId();
			log.info("getSerialNumber---> assemblyId=" + assemblyId);

			Query query4 = entityManager
					.createNamedQuery("findAssemblyByAssemblyIdAndConfigurationId");
			query4.setParameter("assemblyId", assemblyId);
			query4.setParameter("hwConfigurationId", hwConf.getConfigurationId());
			Assembly assembly = (Assembly) query4.getSingleResult();

			outValue = assembly.getSerialNumber();
			log.info("getSerialNumber---> serialNumber="
					+ assembly.getSerialNumber());
		} finally {
			entityManager.close();
		}
		return outValue;
	}

	public String getSerialNumber(String componentName) {
		return getSerialNumber(componentName, this.myConfigName);
	}

	public ArrayList<String> getAllSerialNumbers(String configurationName) {
		ArrayList<String> allSerialNumberArrayList = new ArrayList<String>();

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();

		try {
			Query query0 = entityManager
					.createNamedQuery("findConfigurationByName");
			query0.setParameter("configurationName", configurationName);
			Configuration configuration = (Configuration) query0
					.getSingleResult();
			Integer configurationId = configuration.getConfigurationId();

			Query query1 = entityManager
					.createNamedQuery("findAssemblyByConfigurationId");
			query1.setParameter("configurationId", configurationId);
			List assemblyList = query1.getResultList();

			for (Object assembly : assemblyList) {
				allSerialNumberArrayList.add(((Assembly) assembly)
						.getSerialNumber());
			}
		} finally {
			entityManager.close();
		}
		return allSerialNumberArrayList;

	}

	public ArrayList<String> getAllSerialNumbers() {
		return getAllSerialNumbers(this.myConfigName);
	}

	public TimeValuePager getMonitorData(Integer monitorPointId,
			Timestamp startTimestamp, Timestamp stopTimestamp) {
		TimeValuePager tvp = new TimeValuePagerImpl(monitorPointId,
				startTimestamp, stopTimestamp, log);
		return tvp;
	}

}
