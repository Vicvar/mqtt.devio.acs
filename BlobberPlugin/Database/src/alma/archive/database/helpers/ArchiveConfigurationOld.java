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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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
 * BACKEND SPECIFIC (SEMI-OPTIONAL)
 * - archive.db.oracleLocation
 * - archive.db.xindiceLocation
 * 
 * Other properties can be added and used without changing the code. These properties must 
 * start with the prefix alma.archive., otherwise they won't be recognized.
 * 
 * This class is deprecated as of ARCHIVE-12_4-B, for ALMA 10.6. Please use ArchiveConfiguration instead (I need to simplify the 
 * codebase).
 * 
 * @author hmeuss
 *
 */
@Deprecated
public final class ArchiveConfigurationOld extends DBConfiguration {

	/** 
	 * default name of the config file
	 */
	public static String defaultConfigFileName = "dbConfig.properties";

	/* default values will be overwritten if a correspinding parameter is set in the config file */
	
	private Properties props=new Properties(); // loaded properties, so that they can get deleted afterwards.

	/* constructs new DBConfiguration from parameters in the config file. */
	protected ArchiveConfigurationOld(Logger logger) throws DatabaseException {
		logger.info("Constructing Archive configuration file as instance of ArchiveConfigurationOld (ie. deprecated implementation).");
		// for the time being we do the same as for a re-init
		reinit(logger);
	}

	/**
	 * re-reads information from config file
	 */
	public void reinit(Logger logger) throws DatabaseException {
		// delete already loaded properties:
		for (Enumeration<Object> propIt = props.keys(); propIt.hasMoreElements(); ) {
			String key=(String) propIt.nextElement();
			if (key.startsWith("archive.")) {
				System.clearProperty(key);
			}
		}
		
		// read config file and store properties in Java system properties
		try {
			readConfig(logger);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Problems while reading " + defaultConfigFileName, e);
			// We don't let this fail yet because the required properties will be checked for in createConfig() 
		}
		// fill variables of this with system properties
		createConfig(logger);
		logger.info(this.toString());
	}
	
	/**
	 * Reads configuration parameters from properties files and stores them as system properties. Three files are examined: 
	 * First dbConfig.properties in the current directory, then  $ACS.data/config/dbConfig.properties, 
	 * then dbConfig.properties in the classpath (a fallback properties file that is provided in archive_database.jar). 
	 * 
	 * As soon as the first properties file is found, the others are no longer read! This means, only properties from *one* file
	 * are taken. (With this new specification, the implementaion could be simplified, but will be kept due to time constraints.
	 *  
	* @param logger
	*/
	private void readConfig(Logger logger) throws IOException {
		InputStream propIn;

		props = new Properties();
		
		// read dbConfig.properties from current working directory
		try {
			propIn = new FileInputStream(defaultConfigFileName);
			logger.info(
				"----------- Loading "
					+ defaultConfigFileName
					+ " from current working directory: "+System.getProperty("user.dir"));
			fileLocation=System.getProperty("user.dir")+"/"+defaultConfigFileName;
			props = readProps(propIn);
			propIn.close();
			// store props
			storeProps(props, logger);
			return;
		} catch (FileNotFoundException e) {
			logger.info(
				"No properties file "
					+ defaultConfigFileName
					+ " in current working directory.  Now looking in $ACSDATA (defined by Java property ACS.data).");
		}

		//	read dbConfig.properties from ACS.data/config
		String acsdata=System.getProperty("ACS.data");
		//System.out.println(acsdata);
		if ( acsdata != null) {
			acsdata=acsdata+"/config/";
			try {
				propIn = new FileInputStream(acsdata+defaultConfigFileName);
				fileLocation=acsdata+defaultConfigFileName;
				logger.info(
					"----------- Loading "
						+ defaultConfigFileName
						+ " from "+acsdata);
				props = readProps(propIn);
				propIn.close();
				// store props
				storeProps(props, logger);
				return;
			} catch (FileNotFoundException e) {
				logger.info(
					"No properties file "
						+ defaultConfigFileName
						+" " +acsdata+ ".  Now looking in classpath.");
			}
		}

		// read dbConfig.properties from classpath
		ClassLoader loader = DBConfiguration.class.getClassLoader();
		URL propsFile = loader.getResource(defaultConfigFileName);
		if (propsFile == null) {
			logger.warning(
				"No file " + defaultConfigFileName + " found in classpath.");
		} else {
			logger.info("-------- Loading " + propsFile);
			fileLocation=propsFile.toString();
			propIn = propsFile.openStream();
			props = readProps(propIn);
			propIn.close();
			// store props
			storeProps(props, logger);
		}
	}

	/**
	 * Stores the properties defined in props in the Java system properties. If
	 * a property is already defined it is <b>not</b> overwritten.
	 * @param props
	 */
	private void storeProps(Properties props, Logger logger) {
		for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (System.getProperty(key) == null) {
				// add property
				System.setProperty(key, props.getProperty(key));				
				logger.finest("Added property: " + key + " = " + props.getProperty(key));
			} else {
				// property already defined, ignore
				logger.finest("Property " + key + " already defined, ignoring.");
			}
		}
	}

	/**
	 * Reads properties from an input stream. Property values are expanded (@see expand) 
	 * @param propIn
	 * @return Properties defined in the input stream
	 */
	private Properties readProps(InputStream propIn) throws IOException {
		Properties props = new Properties();
		props.load(propIn);

		// Expand all values
		//-----------------------------
		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String val = props.getProperty(key);
			val = expand(val);
			if (key.equalsIgnoreCase("archive.ngast.clientParams") && !val.startsWith("ngamsArchiveClient")) {
// workaround for backward cpmpatibility
			val="ngamsArchiveClient "+val;
			}
			props.setProperty(key, val);
		}
		return props;
	}

	/**
	 * Expands properties embedded in a string, if any, substituting the
	 * properties' value. Properties can be embedded with a syntax like that of
	 * Ant build files, so that <BR>
	 * <code>&nbsp;&nbsp;&nbsp;abc${user.home}xyz</code><BR>
	 * expands to <BR>
	 * <code>&nbsp;&nbsp;&nbsp;abc/home/johnny/xyz</code><BR>
	 * 
	 * Multiple properties can be embedded in the same string.
	 * <P>
	 * Note: If the embedded property has no value it is resolved to be the
	 * empty string.
	 * <p>
	 * 
	 * Note: Recursion (that is embedded vars within embedded vars) is not
	 * allowed.
	 * 
	 * @author mschilli
	 */
	static String expand(String s) {

		// See if input string contains a valid $(prop) pattern
		int markerPos = s.indexOf("${");
		if (markerPos == -1) // anything to do?
			return s; // NO, return input value.

		int markerEnd = s.indexOf("}", markerPos);
		if (markerEnd == -1) // anything to do?
			return s; // NO, return input value.

		// Input string contains a valid $(prop) pattern
		// Split it into 3 parts
		String preVarName = s.substring(0, markerPos);
		String embeddedVarName = s.substring(markerPos + 2, markerEnd);
		String postVarName = s.substring(markerEnd + 1);

		// replace middle part, if possible
		String embeddedVarValue = System.getProperty(embeddedVarName, "");

		s = preVarName + embeddedVarValue + postVarName;
		return expand(s); // on to the next expansion.
	}

	/**
	 * Sets up a map for {@link #configParams} that contains those system properties which start with "archive.".
	 * Some properties are redundantly stored in instance variables.
	 */
	private void createConfig(Logger logger) throws DatabaseException {
		configParams = new HashMap<String, String>();
		// Now all properties are stored as System properties
		// They can be copied to configParams:
		for (Enumeration<Object> e = System.getProperties().keys();
			e.hasMoreElements();
			) {
			String key = (String) e.nextElement();
			if (key.startsWith("archive.")) {
				configParams.put(key, System.getProperty(key));
			}
		}

		// set variables from properties, for convenience

		// dbBackend
		dbBackend = get("archive.db.backend");
		if (dbBackend == null) {
			logger.severe(
				"Property archive.db.backend undefined! Check dbConfig.properties in classpath and working directory.");
			throw new DatabaseException("No Database backend specified (property archive.db.backend undefined).");
		}

		// mode
		if ("test".equals(get("archive.db.mode"))) {
			testMode = true;
			// might be overwritten in the next if clause
		}
		
		if (dbBackend.equals("oracle") && testMode && get(
		"archive.oracle.user").equals("alma")) {
			logger.severe("When running in test mode, user must be almatest. Check dbConfig file.");
			throw new DatabaseException("Permission denied: only user almatest can run in test mode");
		}


	}

	/* returns string representation of the configuration, i.e. the parameter name/value pairs */
	public String toString() {
		StringBuffer out = new StringBuffer("Database configuration: ");
		for (Iterator<String> it = configParams.keySet().iterator(); it.hasNext();) {
			String name = it.next();
			out.append(name + "=" + configParams.get(name) + ",  ");
		}
		return out.toString();
	}
	
	/*
	 * <dbconfiguration>
	 * 	<config name="" value="">
	 * 	...
	 * </dbconfiguration>
	 */
	public Element toElement()
	{
		Element root = new Element("dbconfiguration");
		Iterator<String> iter = configParams.keySet().iterator();
		while (iter.hasNext())
		{
			Element config = new Element("config");
			String name = iter.next();
			config.setAttribute("name",name);
			config.setAttribute("value", configParams.get(name));
			root.addContent(config);
		}
		return root;
	}
	
	public String toXmlString()
	{
		Element element = this.toElement(); 

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		// XMLOutputter out = new XMLOutputter("  ",true,"UTF-8");	
		String xml = out.outputString(element);
		return xml;
	}

	/* returns value of parameter, if defined. Otherwise returns null */
	public String get(String paramName) {
		return configParams.get(paramName);
	}

	// Oracle case: returns a JDBC URL based on the service alias specified in property name
	// unused here
	public String getConnectionURL(String propertyName) {
		return "";
	}

}
