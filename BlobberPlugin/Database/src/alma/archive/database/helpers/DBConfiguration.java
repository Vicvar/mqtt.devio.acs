/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 *
 *    Created on Jan 21, 2004
 *
 */
package alma.archive.database.helpers;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.jdom.Element;

import alma.archive.exceptions.general.DatabaseException;

/**
 * 
 * A class holding all configuration parameters handed over by the config file. Parameters are 
 * set when InternalIFFactory is called for the first time, or when a new configuration is handed over by ArchiveAdministration.
 * 
 * Other classes should use the method get or the public variable configParams to access the properties. Some of them are also 
 * stored in dedicated variables for convenience (eg. testMode).
 * 
 * The parameters are taken from Java system properties. These are taken from (in this order): 
 * 1) The file dbConfig.properties located in the classpath (a fallback is provided in archive_database.jar)
 * 2) The file dbConfig.properties.jar located in $ACSDATA/config
 * 2) The file dbConfig.properties located in the current working directory
 * 3) Java command line properties passed with -DpropName=propValue
 * 
 * Command line properties overwrite properties from the properties file in the current directory, which themselves 
 * overwrite properties taken from the properties file in $ACSDATA/config and the classpath, resp.
 * 
 * <b>Only</b> properties with prefix archive. are considered, all others are ignored!!!
 *  
 * 
 * The idea and some code are taken from ObsPreps PropertyHandler 
 * 
 * The properties used are the following (others may also be used):
 *  MANDATORY:
 * - archive.db.backend
 * OPTIONAL: 
 * - archive.db.testStart
 * - archive.db.testEnd
 * - archive.db.mode
 * - archive.db.visibility
 * - archive.db.idStart
 * BACKEND SPECIFIC (SEMI-OPTIONAL)
 * - archive.db.oracleLocation
 * - archive.db.xindiceLocation
 * 
 * Other properties can be added and used without changing the code. These properties must 
 * start with the prefix alma.archive., otherwise they won't be recognized.
 * 
 * @Deprecated - use ArchiveConfiguration instead
 * @author hmeuss
 *
 */
@Deprecated
public abstract class DBConfiguration {

	private static DBConfiguration instance;

	/** 
	 * default name of the config file
	 */
	public static String defaultConfigFileName = "archiveConfig.properties";

	/* default values will be overwritten if a correspinding parameter is set in the config file */

	/* database backend used, eg. "db2" or "xindice" */
	public String dbBackend;
	/* running in test mode? */
	public boolean testMode = false;
	/* all parameters from the config file in a HashMap. In addition, all 
	 * Java properties are also stored here.   */
	public HashMap<String, String> configParams = new HashMap<String, String>();
	/**
	 * Actual location where the dbConfig file was found.
	 */
	public String fileLocation;
	
	/**
	 * re-reads information from config file
	 */
	public abstract void reinit(Logger logger) throws DatabaseException;
	
	/**
	 * Singleton accessor
	 */
	public static synchronized DBConfiguration instance(Logger logger)
		throws DatabaseException {
		
		// TODO: check whether we use the new ArchiveConfiguration implementation class, or the old ArchiveConfigurationOld class.
		// This is determined by the existence of the archiveConfig file.
		
		if (instance == null) {

			// if archive.configFile is defined, we use the new class:
			if (System.getProperty("archive.configFile")!=null&&!System.getProperty("archive.configFile").equals("")) {
				instance = new ArchiveConfiguration(logger);
			} else
			
			// then check, whether archiveConfig.properties exists in $ACSDATA:
			if (System.getProperty("ACS.data")!=null&&new File(System.getProperty("ACS.data")+"/config/"+defaultConfigFileName).exists()) {
				instance = new ArchiveConfiguration(logger);
			}
			
			// else, we take the old one:
			if (instance == null) {
				instance = new ArchiveConfigurationOld(logger);
			}
		} 
		return instance;
	}

	/* returns string representation of the configuration, i.e. the parameter name/value pairs */
	public abstract String toString();
	
	/*
	 * <dbconfiguration>
	 * 	<config name="" value="">
	 * 	...
	 * </dbconfiguration>
	 */
	public abstract Element toElement();
	
	public abstract String toXmlString();
	
	/* returns value of parameter, if defined. Otherwise returns null */
	public abstract String get(String paramName);


	// Oracle case: returns a JDBC URL based on the service alias specified in property name
	public abstract String getConnectionURL(String propertyName);

	
}
