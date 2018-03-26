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
package com.cosylab.cdb.jdal.hibernate.plugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Criterion;

import alma.TMCDB.baci.AmbDevice;
import alma.TMCDB.baci.BACIPropertyType;
import alma.TMCDB.baci.EmptyStringHandlerBACIPropertyType;
import alma.acs.logging.AcsLogLevel;
import alma.acs.tmcdb.AOSTiming;
import alma.acs.tmcdb.AssemblyStartup;
import alma.acs.tmcdb.AssemblyType;
import alma.acs.tmcdb.BEType;
import alma.acs.tmcdb.BaseElement;
import alma.acs.tmcdb.BaseElementStartup;
import alma.acs.tmcdb.CentralLO;
import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.DefaultCanAddress;
import alma.acs.tmcdb.HWConfiguration;
import alma.acs.tmcdb.PhotonicReference;
import alma.acs.tmcdb.Startup;
import alma.acs.tmcdb.WeatherStationController;
import alma.cdbErrType.CDBRecordDoesNotExistEx;

import com.cosylab.CDB.DAO;
import com.cosylab.cdb.client.CDBAccess;
import com.cosylab.cdb.jdal.hibernate.DBUtil;

/**
 * @author msekoranja
 *
 */
public class HibernateWDALPluginImpl implements HibernateWDALPlugin {

	private Logger m_logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#getName()
	 */
	public String getName() {
		return "ALMA HW section plugin";
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#initialize(java.util.logging.Logger)
	 */
	public void initialize(Logger logger) {
		this.m_logger = logger;
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#importEpilogue(org.hibernate.Session, alma.acs.tmcdb.Configuration, com.cosylab.cdb.client.CDBAccess)
	 */
	public void importEpilogue(Session session, Configuration config, CDBAccess cdbAccess) {
		
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#importPrologue(org.hibernate.Session, alma.acs.tmcdb.Configuration, com.cosylab.cdb.client.CDBAccess)
	 */
	public void importPrologue(Session session, Configuration config, CDBAccess cdbAccess) {
		HWConfiguration hwconfig = new HWConfiguration();
		hwconfig.setTelescopeName("ALMA");
		hwconfig.setArrayReferenceX(.0);
		hwconfig.setArrayReferenceY(.0);
		hwconfig.setArrayReferenceZ(.0);
		hwconfig.setConfiguration(config);
		session.persist(hwconfig);
		m_logger.info("Created HwConfiguration record for Configuration '" + config.getConfigurationName() + "'");

		// Default hardcoded initial CentralLO and AOSTiming base elements

		// CentralLO
		BaseElement baseElementCentralLO = new BaseElement();
		baseElementCentralLO.setBaseType(BEType.CENTRALLO);
		baseElementCentralLO.setBaseElementName("CentralLO");
		baseElementCentralLO.setHWConfiguration(hwconfig);
		session.persist(baseElementCentralLO);

		CentralLO centralLO = new CentralLO();
		centralLO.setBaseElement(baseElementCentralLO);
		centralLO.setCommissionDate(new Date().getTime());
		session.persist(centralLO);

		// AOSTiming
		BaseElement baseElementAOSTiming = new BaseElement();
		baseElementAOSTiming.setBaseType(BEType.AOSTIMING);
		baseElementAOSTiming.setBaseElementName("AOSTiming");
		baseElementAOSTiming.setHWConfiguration(hwconfig);
		session.persist(baseElementAOSTiming);

		AOSTiming aosTiming = new AOSTiming();
		aosTiming.setBaseElement(baseElementAOSTiming);
		aosTiming.setCommissionDate(new Date().getTime());
		session.persist(aosTiming);

		// WeatherStation
		BaseElement baseElementWeatherStation = new BaseElement();
		baseElementWeatherStation.setBaseType(BEType.WEATHERSTATIONCONTROLLER);
		baseElementWeatherStation.setBaseElementName("WeatherStationController");
		baseElementWeatherStation.setHWConfiguration(hwconfig);
		session.persist(baseElementWeatherStation);

		WeatherStationController ws = new WeatherStationController();
		ws.setCommissionDate(new Date().getTime());
		ws.setBaseElement(baseElementWeatherStation);
		session.persist(ws);

		// PhotonicReference1
		BaseElement baseElementPhotonicReference1 = new BaseElement();
		baseElementPhotonicReference1.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference1.setBaseElementName("PhotonicReference1");
		baseElementPhotonicReference1.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference1);

		PhotonicReference pr1 = new PhotonicReference();
		pr1.setCommissionDate(new Date().getTime());
		pr1.setBaseElement(baseElementPhotonicReference1);
		session.persist(pr1);

		// PhotonicReference2
		BaseElement baseElementPhotonicReference2 = new BaseElement();
		baseElementPhotonicReference2.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference2.setBaseElementName("PhotonicReference2");
		baseElementPhotonicReference2.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference2);

		PhotonicReference pr2 = new PhotonicReference();
		pr2.setCommissionDate(new Date().getTime());
		pr2.setBaseElement(baseElementPhotonicReference2);
		session.persist(pr2);

		// PhotonicReference3
		BaseElement baseElementPhotonicReference3 = new BaseElement();
		baseElementPhotonicReference3.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference3.setBaseElementName("PhotonicReference3");
		baseElementPhotonicReference3.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference3);

		PhotonicReference pr3 = new PhotonicReference();
		pr3.setCommissionDate(new Date().getTime());
		pr3.setBaseElement(baseElementPhotonicReference3);
		session.persist(pr3);

		// PhotonicReference4
		BaseElement baseElementPhotonicReference4 = new BaseElement();
		baseElementPhotonicReference4.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference4.setBaseElementName("PhotonicReference4");
		baseElementPhotonicReference4.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference4);

		PhotonicReference pr4 = new PhotonicReference();
		pr4.setCommissionDate(new Date().getTime());
		pr4.setBaseElement(baseElementPhotonicReference4);
		session.persist(pr4);

		// PhotonicReference5
		BaseElement baseElementPhotonicReference5 = new BaseElement();
		baseElementPhotonicReference5.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference5.setBaseElementName("PhotonicReference5");
		baseElementPhotonicReference5.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference5);

		PhotonicReference pr5 = new PhotonicReference();
		pr5.setCommissionDate(new Date().getTime());
		pr5.setBaseElement(baseElementPhotonicReference5);
		session.persist(pr5);

		// PhotonicReference6
		BaseElement baseElementPhotonicReference6 = new BaseElement();
		baseElementPhotonicReference6.setBaseType(BEType.PHOTONICREFERENCE);
		baseElementPhotonicReference6.setBaseElementName("PhotonicReference6");
		baseElementPhotonicReference6.setHWConfiguration(hwconfig);
		session.persist(baseElementPhotonicReference6);

		PhotonicReference pr6 = new PhotonicReference();
		pr6.setCommissionDate(new Date().getTime());
		pr6.setBaseElement(baseElementPhotonicReference6);
		session.persist(pr6);

		m_logger.info("Created: (1) CentralLO, (1) AOSTiming, (6) PhotonicReference, and (1) WeatherStationController records for Configuration '" 
                      + config.getConfigurationName() + "'");

	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#loadControlDevices(org.hibernate.Session, alma.acs.tmcdb.Configuration, com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin.ControlDeviceBindCallback)
	 */
	public void loadControlDevices(Session session, Configuration config, ControlDeviceBindCallback bindCallback) {
		m_logger.finer("About to query all components with isControl==true");
        List compList = session.createCriteria(Component.class)
                               .add(Restrictions.eq("isControl", true))
                               .add(Restrictions.eq("configuration", config)).list();
		m_logger.fine("Done with query for all components with isControl==true. Got a list of " + compList.size() + " control device components.");
		
	    for (Iterator iter = compList.iterator(); iter.hasNext(); ) {
	        Component component = (Component) iter.next();
	        m_logger.fine("About to handle device component " + component.getComponentName());
	        
	        String query = "FROM " + BACIPropertyType.class.getName() + " WHERE ComponentId = " + component.getComponentId();
	        List propList = session.createQuery(query).list();
	        if (propList.size() > 0) {
	            AmbDevice ambDevice = new AmbDevice();
	 			try {
					ambDevice.setData(component.getXMLDoc());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            for (Iterator iter2 = propList.iterator(); iter2.hasNext(); ) {
	                BACIPropertyType baciProperty = (BACIPropertyType) iter2.next();
	                //ambDevice._.put(baciProperty.PropertyName, baciProperty);
	                ambDevice._.put(baciProperty.PropertyName, new EmptyStringHandlerBACIPropertyType(baciProperty));
	            }
	            boolean isEthernet = false;
	            String nodeAddress = "0";
	            String baseAddress = "0";
	            int channelNumber = 0;
	        	String hostname = "";
	        	int port = 0;
	        	String macAddress = "";
	        	int retries = 0;
	        	double timeoutRxTx = 0.0;
	        	int lingerTime = 0;
		        List addressList = session.createCriteria(DefaultCanAddress.class)
		                .add(Restrictions.eq("componentId", component.getComponentId())).list();
	            if (addressList.size() > 0) {
	                DefaultCanAddress address = (DefaultCanAddress) addressList.get(0);
	                isEthernet = address.getIsEthernet();
	                nodeAddress = address.getNodeAddress();
	                channelNumber = address.getChannelNumber();
	                hostname = address.getHostname();
	                port = address.getPort();
	                macAddress = address.getMacAddress();
	                retries = address.getRetries();
	                timeoutRxTx = address.getTimeOutRxTx();
	                lingerTime = address.getLingerTime();
	            }
	            if (!isEthernet) {
		            AmbDevice.AmbAddress ambAddress = new AmbDevice.AmbAddress();
		            ambAddress.setNodeNumber(Integer.parseInt(nodeAddress));
		            ambAddress.setBaseAddress(Integer.parseInt(baseAddress));
		            ambAddress.setChannelNumber(channelNumber);
		            ambDevice.setAddress(ambAddress);
	            } else {
		            AmbDevice.EthernetAddress ethAddress = new AmbDevice.EthernetAddress();
		            ethAddress.setHostname(hostname);
		            ethAddress.setPort(port);
		            ethAddress.setMacAddress(macAddress);
		            ethAddress.setRetries(retries);
		            ethAddress.setTimeoutRxTx(timeoutRxTx);
		            ethAddress.setLingerTime(lingerTime);
		            ambDevice.setEthernetConfig(ethAddress);
	            }
	            if (component.getXMLDoc() != null) {
		            try {
						ambDevice.setControlCdbExtraData(component.getXMLDoc());
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
	            }	            
	            bindCallback.bindToComponentBranch(
	                             component.getComponentName(),
	                             component.getPath(),
	                             ambDevice);
	        } else if (component.getXMLDoc() != null) {
	            bindCallback.bindNonExpandedXMLToComponentBranch(session, component);
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#controlDeviceImportEpilogue(org.hibernate.Session, alma.acs.tmcdb.Configuration, com.cosylab.cdb.client.CDBAccess, java.lang.String, alma.TMCDB.generated.Component)
	 */
	public void controlDeviceImportEpilogue(Session session, Configuration config,
			CDBAccess cdbAccess, String componentName, Component component) {
		m_logger.info("Creating DAO for CONTROL device " + componentName);
	    Boolean isEthernet = false;
	    String nodeAddress = "-1";
	    Byte channelNumber = -1;
	    String hostname = "not set";
	    Integer port = -1;
	    String macAddress = "not set";
	    Short retries = -1;
	    Double timeOutRxTx = -1.0;
	    Integer lingerTime = -1;
		try {
			try {
				DAO deviceAddressDAO = cdbAccess.getDAL().get_DAO_Servant("alma/" + componentName + "/Address");
				nodeAddress = deviceAddressDAO.get_string("NodeNumber");
				channelNumber = (byte) deviceAddressDAO.get_long("ChannelNumber");
			} catch (CDBRecordDoesNotExistEx e) {
				isEthernet = true;
				DAO devEtherConfigDAO = cdbAccess.getDAL().get_DAO_Servant("alma/" + componentName + "/EthernetConfig");
				hostname = devEtherConfigDAO.get_string("hostname");
				port = devEtherConfigDAO.get_long("port");
				macAddress = devEtherConfigDAO.get_string("macAddress");
				retries = (short) devEtherConfigDAO.get_long("retries");
				timeOutRxTx = devEtherConfigDAO.get_double("timeoutRxTx");
				lingerTime = devEtherConfigDAO.get_long("lingerTime");
			} 
		} catch( Exception ex ) {
			m_logger.finer("Failed to read 'alma/" + componentName + "/Address|EthernetConfig'");
		}

		DefaultCanAddress defAdd = new DefaultCanAddress();
		defAdd.setIsEthernet(isEthernet);
		defAdd.setComponent(component);
		defAdd.setNodeAddress(nodeAddress);
		defAdd.setChannelNumber((byte)channelNumber);
		defAdd.setHostname(hostname);
		defAdd.setPort(port);
		defAdd.setMacAddress(macAddress);
		defAdd.setRetries(retries);
		defAdd.setTimeOutRxTx(timeOutRxTx);
		defAdd.setLingerTime(lingerTime);
		session.persist(defAdd);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#loadEpilogue(org.hibernate.Session, alma.acs.tmcdb.Configuration, java.util.Map)
	 */
	public void loadEpilogue(Session session, Configuration config, Map<String, Object> rootMap) {
		// noop
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#loadPrologue(org.hibernate.Session, alma.acs.tmcdb.Configuration, java.util.Map)
	 */
	public void loadPrologue(Session session, Configuration config, Map<String, Object> rootMap) {

		// Fix for COMP-4990
		// We need to update the "code" field for all Components on this configuration,
		// depending on the current startup scenario and the corresponding AssemblyType's
		// simulation and production code.
		// This needs to happen before the hDAL constructs the map of paths and objects,
		// so once it loads the info, this is already corrected.
		long initialTime = System.currentTimeMillis();
		int compsUpdated = 0;

		String activeStartupScenario = System.getenv("TMCDB_STARTUP_NAME");
		if( activeStartupScenario == null || activeStartupScenario.trim().length() == 0 ) {
			m_logger.log(AcsLogLevel.NOTICE, "TMCDB_STARTUP_NAME variable not defined or empty, no startup scenario preferences will be applied to components");
			return;
		}

		
		m_logger.info("Will update components information with '" + activeStartupScenario + "' startup scenario preferences");

		Transaction tx = session.beginTransaction();

		try {

			// Find HwConfiguration, and startup scenario
			HWConfiguration hwConfig = (HWConfiguration)session.createCriteria(HWConfiguration.class)
			.add( Restrictions.eq("configuration", config) )
			.uniqueResult();
			if( hwConfig == null ) {
				m_logger.log(AcsLogLevel.ERROR, "No HwConfiguration for configuration name '" + config.getConfigurationName() + "'");
				return;
			}

			Startup startup = (Startup)session.createCriteria(Startup.class)
			.add( Restrictions.eq("HWConfiguration", hwConfig) )
			.add( Restrictions.eq("startupName", activeStartupScenario) )
			.uniqueResult();

			if( startup == null ) {
				m_logger.log(AcsLogLevel.NOTICE, "No '" + activeStartupScenario + "' startup scenario found for configuration '" + config.getConfigurationName() + "', no components will be updated");
				tx.commit();
				return; 
			}

			// For all elements on startup scenario, get their names, translate into component names,
			// and check their codes depending on their IDL types.
			for (BaseElementStartup bes: startup.getBaseElementStartups())
				compsUpdated += checkUpdateBaseElement(bes, "", config, session);

			tx.commit();

			long msecs = System.currentTimeMillis() - initialTime;
			m_logger.info( compsUpdated + " components updated with startup scenario information in " + msecs + " [msec]");

		} catch(Exception e) {
			m_logger.log(AcsLogLevel.ERROR, "Error while updating components with startup scenario information, can't apply startup scenario preferences");
			tx.rollback();
			throw new RuntimeException(e);
		}

	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#updateControlDevices(org.hibernate.Session, alma.acs.tmcdb.Configuration, com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin.ControlDeviceBindCallback)
	 */
	public void updateControlDevices(Session session, Configuration config, ControlDeviceBindCallback bindCallback, String curl) {
		if(curl.matches("")) {
			loadControlDevices(session, config, bindCallback);
			return;
		}
		String els[] = curl.split("/");
		String rpath = "^/*";
		String rsubpath = "^/*";
		String rcpath = "^/*";
		String rcname = els[els.length - 1];
		for (int i = 0; i < els.length; i++) {
		   rpath    += els[i];
		   rsubpath += els[i];
		   if (i < els.length - 1) {
		      rpath    += "/+";
		      rsubpath += "/+";
		      rcpath   += els[i];
		      if( i < els.length - 2)
		         rcpath += "/+";
		   }
		}
		rpath    += "/*$";
		rsubpath += "/+.*";
		rcpath   += "/*$";
		
		System.out.println(rpath);
		System.out.println(rsubpath);
		System.out.println(rcpath+"|"+rcname);
		
		//Consider the cases where the curl matches exactly the Path, where
		//it is part of the path and when it matches exactly the path and
		//the component name.
		Criterion cr = Restrictions.disjunction()
						.add(getRegularExpressionRestriction("Path", rpath))
						.add(getRegularExpressionRestriction("Path", rsubpath))
						.add(Restrictions.and(getRegularExpressionRestriction("Path", rcpath), Restrictions.eq("componentName",rcname)));

		m_logger.finer("About to query all components with isControl==true");
        List compList = session.createCriteria(Component.class)
                               .add(Restrictions.eq("isControl", true))
                               .add(Restrictions.eq("configuration", config)).add(cr).list();
		m_logger.fine("Done with query for all components with isControl==true. Got a list of " + compList.size() + " control device components.");

		System.out.println("\nFound the following Components");
		for (Iterator iter = compList.iterator(); iter.hasNext(); ) {
			Object data = iter.next();
			System.out.println(((Component)data).getPath()+"/"+((Component)data).getComponentName());
		}
	        
		
	    for (Iterator iter = compList.iterator(); iter.hasNext(); ) {
	        Component component = (Component) iter.next();
	        m_logger.fine("About to handle device component " + component.getComponentName());

	        String query = "FROM " + BACIPropertyType.class.getName() + " WHERE ComponentId = " + component.getComponentId();
	        List propList = session.createQuery(query).list();
	        if (propList.size() > 0) {
	            AmbDevice ambDevice = new AmbDevice();
	 			try {
					ambDevice.setData(component.getXMLDoc());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            for (Iterator iter2 = propList.iterator(); iter2.hasNext(); ) {
	                BACIPropertyType baciProperty = (BACIPropertyType) iter2.next();
	                //ambDevice._.put(baciProperty.PropertyName, baciProperty);
	                ambDevice._.put(baciProperty.PropertyName, new EmptyStringHandlerBACIPropertyType(baciProperty));
	            }
	            boolean isEthernet = false;
	            String nodeAddress = "0";
	            String baseAddress = "0";
	            int channelNumber = 0;
	        	String hostname = "";
	        	int port = 0;
	        	String macAddress = "";
	        	int retries = 0;
	        	double timeoutRxTx = 0.0;
	        	int lingerTime = 0;
		        List addressList = session.createCriteria(DefaultCanAddress.class)
		                .add(Restrictions.eq("componentId", component.getComponentId())).list();
	            if (addressList.size() > 0) {
	                DefaultCanAddress address = (DefaultCanAddress) addressList.get(0);
	                isEthernet = address.getIsEthernet();
	                nodeAddress = address.getNodeAddress();
	                channelNumber = address.getChannelNumber();
	                hostname = address.getHostname();
	                port = address.getPort();
	                macAddress = address.getMacAddress();
	                retries = address.getRetries();
	                timeoutRxTx = address.getTimeOutRxTx();
	                lingerTime = address.getLingerTime();
	            }
	            if (!isEthernet) {
		            AmbDevice.AmbAddress ambAddress = new AmbDevice.AmbAddress();
		            ambAddress.setNodeNumber(Integer.parseInt(nodeAddress));
		            ambAddress.setBaseAddress(Integer.parseInt(baseAddress));
		            ambAddress.setChannelNumber(channelNumber);
		            ambDevice.setAddress(ambAddress);
	            } else {
		            AmbDevice.EthernetAddress ethAddress = new AmbDevice.EthernetAddress();
		            ethAddress.setHostname(hostname);
		            ethAddress.setPort(port);
		            ethAddress.setMacAddress(macAddress);
		            ethAddress.setRetries(retries);
		            ethAddress.setTimeoutRxTx(timeoutRxTx);
		            ethAddress.setLingerTime(lingerTime);
		            ambDevice.setEthernetConfig(ethAddress);
	            }
	            if (component.getXMLDoc() != null) {
		            try {
						ambDevice.setControlCdbExtraData(component.getXMLDoc());
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
	            }	            
	            bindCallback.bindToComponentBranch(
	                             component.getComponentName(),
	                             component.getPath(),
	                             ambDevice);
	        } else if (component.getXMLDoc() != null) {
	            bindCallback.bindNonExpandedXMLToComponentBranch(session, component);
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#updateEpilogue(org.hibernate.Session, alma.acs.tmcdb.Configuration, java.util.Map)
	 */
	public void updateEpilogue(Session session, Configuration config, Map<String, Object> rootMap, String curl) {
		// noop
	}

	/* (non-Javadoc)
	 * @see com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin#updatePrologue(org.hibernate.Session, alma.acs.tmcdb.Configuration, java.util.Map)
	 */
	public void updatePrologue(Session session, Configuration config, Map<String, Object> rootMap, String curl) {
		if(!curl.startsWith("alma/CONTROL"))
			return;
		else if(curl.matches("alma/CONTROL")) {
			loadPrologue(session, config, rootMap);
			return;
		}
		String c = curl.replaceFirst("alma/CONTROL/", "");

		// Fix for COMP-4990
		// We need to update the "code" field for all Components on this configuration,
		// depending on the current startup scenario and the corresponding AssemblyType's
		// simulation and production code.
		// This needs to happen before the hDAL constructs the map of paths and objects,
		// so once it loads the info, this is already corrected.
		long initialTime = System.currentTimeMillis();
		int compsUpdated = 0;

		String activeStartupScenario = System.getenv("TMCDB_STARTUP_NAME");
		if( activeStartupScenario == null || activeStartupScenario.trim().length() == 0 ) {
			m_logger.log(AcsLogLevel.NOTICE, "TMCDB_STARTUP_NAME variable not defined or empty, no startup scenario preferences will be applied to components");
			return;
		}

		
		m_logger.info("Will update components information with '" + activeStartupScenario + "' startup scenario preferences");

		Transaction tx = session.beginTransaction();

		try {

			// Find HwConfiguration, and startup scenario
			HWConfiguration hwConfig = (HWConfiguration)session.createCriteria(HWConfiguration.class)
			.add( Restrictions.eq("configuration", config) )
			.uniqueResult();
			if( hwConfig == null ) {
				m_logger.log(AcsLogLevel.ERROR, "No HwConfiguration for configuration name '" + config.getConfigurationName() + "'");
				return;
			}

			Startup startup = (Startup)session.createCriteria(Startup.class)
			.add( Restrictions.eq("HWConfiguration", hwConfig) )
			.add( Restrictions.eq("startupName", activeStartupScenario) )
			.uniqueResult();

			if( startup == null ) {
				m_logger.log(AcsLogLevel.NOTICE, "No '" + activeStartupScenario + "' startup scenario found for configuration '" + config.getConfigurationName() + "', no components will be updated");
				tx.commit();
				return; 
			}

			// For all elements on startup scenario, get their names, translate into component names,
			// and check their codes depending on their IDL types.
			for (BaseElementStartup bes: startup.getBaseElementStartups())
				compsUpdated += checkUpdateBaseElement(bes, "", config, session, c);

			tx.commit();

			long msecs = System.currentTimeMillis() - initialTime;
			m_logger.info( compsUpdated + " components updated with startup scenario information in " + msecs + " [msec]");

		} catch(Exception e) {
			m_logger.log(AcsLogLevel.ERROR, "Error while updating components with startup scenario information, can't apply startup scenario preferences");
			tx.rollback();
			throw new RuntimeException(e);
		}

	}

	private int checkUpdateBaseElement(BaseElementStartup bes, String path, Configuration config, Session session) {

		int compsUpdated = 0;

		// Check Base Element's component
		String nextPathElement = null;
		if( bes.getBaseElement() == null )
			nextPathElement = bes.getBaseElementType().toString();
		else
			nextPathElement = bes.getBaseElement().getBaseElementName();

		m_logger.log(AcsLogLevel.DEBUG, "Checking BaseElementStartup " + path + nextPathElement);
		compsUpdated += checkUpdateBaseElementComponent(path + nextPathElement, bes, config, session);

		// Check Assemblies' components
		for(AssemblyStartup as: bes.getAssemblyStartups())
			compsUpdated += checkUpdateAssemblyComponent(path + nextPathElement + "/", as, config, session);

		// Recurse down to embedded base elements
		for(BaseElementStartup bes2: bes.getBaseElementStartups())
			compsUpdated += checkUpdateBaseElement(bes2, path + nextPathElement + "/", config, session);

		return compsUpdated;
	}

	private int checkUpdateAssemblyComponent(String path, AssemblyStartup as, Configuration config, Session session) {

		String fullName = path + as.getAssemblyRole().getRoleName();
		Component comp = getCONTROLComponent(fullName, config, session);

		if( comp == null )
			return 0;

		AssemblyType at = (AssemblyType)session.createCriteria(AssemblyType.class)
		.add( Restrictions.eq("componentType", comp.getComponentType()) )
		.uniqueResult();
		if( at == null ) {
			m_logger.severe("No AssemblyType for component type '" + comp.getComponentType().getIDL() + "', component CONTROL/" + fullName + " will not get updated");
			return 0;//throw new RuntimeException("No AssemblyType for component type '" + comp.getComponentType().getIDL() + "'");
		}

		return checkUpdateComponent(comp, at.getSimulatedCode(), at.getProductionCode(), as.getSimulated(), session);
	}

	private int checkUpdateBaseElementComponent(String fullName, BaseElementStartup bes, Configuration config, Session session) {

		// Check Base Element's component
		Component comp = getCONTROLComponent(fullName, config, session);
		if( comp == null ) {
			return 0;
		}

		return checkUpdateComponent(comp,
				 simulationCodeForBaseElements.get(bes.getBaseElementType()),
				 productionCodeForBaseElements.get(bes.getBaseElementType()),
				 bes.getSimulated(),
				 session);
	}

	private int checkUpdateComponent(Component comp, String simulatedCode, String productionCode, boolean isSimulated, Session session) {

		String currentCode = comp.getCode();

		// Fix for COMP-5036: if the current component code doesn't correspond either to the
		//    production code nor to the simulation code, then we don't update the component
		//    This allows to set custom codes into the components, which is needed, for example,
		//    in the case of the CentralLO
		if( !currentCode.equals(simulatedCode) && !currentCode.equals(productionCode) )
			return 0;

		// If simulated, then component's code should match AssemblyType's simulatedCode
		// If non simulated, then component's code should match AssemblyType's productionCode
		// Otherwise, we need to replace and update
		if( isSimulated && !currentCode.equals(simulatedCode) ) {
			comp.setCode(simulatedCode);
			session.update(comp);
			session.flush();
			return 1;
		}
		else if ( !isSimulated && !currentCode.equals(productionCode) ) {
			comp.setCode(productionCode);
			session.update(comp);
			session.flush();
			return 1;
		}
		return 0;
	}

	private int checkUpdateBaseElement(BaseElementStartup bes, String path, Configuration config, Session session, String curl) {

		int compsUpdated = 0;

		// Check Base Element's component
		String nextPathElement = null;
		if( bes.getBaseElement() == null )
			nextPathElement = bes.getBaseElementType().toString();
		else
			nextPathElement = bes.getBaseElement().getBaseElementName();

		if(!curl.startsWith(nextPathElement))
			return compsUpdated;
		else if(curl.matches(nextPathElement))
			return checkUpdateBaseElement(bes, path, config, session);

		m_logger.log(AcsLogLevel.DEBUG, "Checking BaseElementStartup " + path + nextPathElement);
		String c = curl.replaceFirst(nextPathElement+"/", "");

		// Check Assemblies' components
		for(AssemblyStartup as: bes.getAssemblyStartups())
			compsUpdated += checkUpdateAssemblyComponent(path + nextPathElement + "/", as, config, session, c);
	
		// Recurse down to embedded base elements
		for(BaseElementStartup bes2: bes.getBaseElementStartups())
			compsUpdated += checkUpdateBaseElement(bes2, path + nextPathElement + "/", config, session, c);

		return compsUpdated;
	}

	private int checkUpdateAssemblyComponent(String path, AssemblyStartup as, Configuration config, Session session, String curl) {
		if(curl.matches(as.getAssemblyRole().getRoleName()))
			return checkUpdateAssemblyComponent(path, as, config, session);
		return 0;
	}

	private Map<String, String> simulationCodeForBaseElements = new HashMap<String, String>() {
		private static final long serialVersionUID = -5728140397028834674L;
	{
		put("Antenna", "antennaSim");
		put("AOSTiming", "AOSTimingSim");
		put("CentralLO", "CentralLOSim");
		put("FrontEnd", "FrontEndImpl");
		put("WeatherStationController", "WeatherStationController");
		put("PhotonicReference1", "PhotonicReference");
		put("PhotonicReference2", "PhotonicReference");
		put("PhotonicReference3", "PhotonicReference");
		put("PhotonicReference4", "PhotonicReference");
		put("PhotonicReference5", "PhotonicReference");
		put("PhotonicReference6", "PhotonicReference");
	}};

	private Map<String, String> productionCodeForBaseElements = new HashMap<String, String>() {
		private static final long serialVersionUID = -5728140397028834674L;
	{
		put("Antenna", "antenna");
		put("AOSTiming", "AOSTiming");
		put("CentralLO", "CentralLO");
		put("FrontEnd", "FrontEndImpl");
		put("WeatherStationController", "WeatherStationController");
		put("PhotonicReference1", "PhotonicReference");
		put("PhotonicReference2", "PhotonicReference");
		put("PhotonicReference3", "PhotonicReference");
		put("PhotonicReference4", "PhotonicReference");
		put("PhotonicReference5", "PhotonicReference");
		put("PhotonicReference6", "PhotonicReference");
	}};

	private Component getCONTROLComponent(String fullName, Configuration config, Session session) {

		String[] pathAndName = getPathAndName("CONTROL/" + fullName);
		m_logger.log(AcsLogLevel.FINE, "Looking for component " + pathAndName[0] + "/" + pathAndName[1]);
		Component comp = (Component)session.createCriteria(Component.class)
		                   .add( Restrictions.eq("path", pathAndName[0]) )
		                   .add( Restrictions.eq("componentName", pathAndName[1]) )
		                   .add( Restrictions.eq("configuration", config) )
		                   .uniqueResult();
		return comp;
	}

	private String[] getPathAndName(String longname) {

		String[] tokens = longname.split("/");

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<= tokens.length - 2; i++) {
			sb.append(tokens[i]);
			if( i != tokens.length - 2)
				sb.append("/");
		}
		
		String name = tokens[tokens.length - 1];
		return new String[] { sb.toString().replaceAll("^/", ""), name };
	}

	public String[] getCreateTablesScriptList(String backend) {
		
		String ddlDir = System.getProperty("ACS.ddlpath");
		if (ddlDir == null)
			ddlDir = ".";
		
		if (backend.equals(DBUtil.ORACLE_BACKEND_NAME)) { 
			return new String[] {
				ddlDir + "/oracle/TMCDB_hwconfigmonitoring/CreateOracleTables.sql"
			};
		}
		else if (backend.equals(DBUtil.HSQLDB_BACKEND_NAME)) { 
			return new String[] {
				ddlDir + "/hsqldb/TMCDB_hwconfigmonitoring/CreateHsqldbTables.sql"
			};
	
		}
		else
			return null;
	}

   protected Criterion getRegularExpressionRestriction(String columnName, String re) {
		//if(forceInMemory) { //HSQLDB
		//   return Restrictions.sqlRestriction("REGEXP_MATCHES("+columnName+", ?)", re, org.hibernate.type.StringType.INSTANCE);
		//} else {
			//return Restrictions.sqlRestriction(columnName+" rlike ?", re, org.hibernate.type.StringType.INSTANCE); //MySQL
		return Restrictions.sqlRestriction("REGEXP_LIKE("+columnName+", ?)", re, org.hibernate.type.StringType.INSTANCE); //Oracle
		//}
   }
}
