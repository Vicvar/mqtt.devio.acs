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
 *    Created on Jun 10, 2009
 *
 */
package alma.archive.database.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.MissingPropertiesException;

/**
 * 
 * A class holding all configuration parameters handed over by the config file.
 * Parameters are set when InternalIFFactory is called for the first time, or
 * when a new configuration is handed over by ArchiveAdministration.
 * 
 * Other classes should use the method get or the public variable configParams
 * to access the properties. Some of them are also stored in dedicated variables
 * for convenience (eg. testMode).
 * 
 * The parameters are taken from Java system properties. The only support
 * default location is:
 * 
 * The file archiveConfig.properties located in $ACSDATA/config
 * 
 * any other location has to be explicitely specified on the command line.
 * 
 * Command line properties overwrite properties from the properties file.
 * 
 * <b>Only</b> properties with prefix archive. and obops. are considered, all
 * others are ignored!!!
 * 
 * 
 * The idea and some code are taken from ObsPreps PropertyHandler
 * 
 * The properties used are the following (others may also be used): MANDATORY: -
 * archive.db.mode
 * 
 * Other properties can be added and used without changing the code. These
 * properties must start with the prefix alma.archive., otherwise they won't be
 * recognized.
 * 
 * @author awicenec, hmeuss
 * 
 */
public final class ArchiveConfiguration extends DBConfiguration {
	private static Log LOG;
	
	/**
	 * singleton methods are left for backwards compatability, but will attempt to move away
	 * from this singleton nightmare. 
	 */
	private static ArchiveConfiguration instance;

	/**
	 * default name of the config file
	 */
	public static String defaultConfigFileName = "archiveConfig.properties";

	/*
	 * default values will be overwritten if a corresponding parameter is set in
	 * the config file. The default here configures a test mode archive using
	 * eXist and file system for storage, just to make sure that nothing bad
	 * happens to the operational archive with hard-coded parameters.
	 */

	public boolean storeInNgas = false;

	/* NGAS buffer directory */
	public String ngasBufferDir;

	/* NGAS archive client parameters */
	public String ngasClientParams;
//
//	/**
//	 * Actual location where the archiveConfig file was found.
//	 */
//	public String fileLocation;

	private Properties props = new Properties(); // loaded properties, so

	// that they can be deleted
	// afterwards.

	/**
	 * Singleton accessor
	 */
	public static synchronized ArchiveConfiguration instance(Log logger)
		throws DatabaseException {
		
		if (logger == null) logger = LogFactory.getLog("Anonymous");
		if (instance == null) {
			LOG = logger;
			// if archive.configFile is defined, we use the new class:
			if ((System.getProperty("archive.configFile") != null && !System.getProperty("archive.configFile").equals(""))
					|| (System.getProperty("ACS.data") != null && new File(System.getProperty("ACS.data")+"/config/"+defaultConfigFileName).exists())) {
				instance = new ArchiveConfiguration(Logger.getLogger(ArchiveConfiguration.class.getSimpleName()));
			}
			else {
				throw new MissingPropertiesException("dbConfig.properties was deprecated and is now no longer supported. Please use archiveConfig.properties instead, specifying either -Darchive.configFile or -DACS.data.");
				// instance = new ArchiveConfigurationOld(logger);
			}
		} 
		return instance;
	}
	
	/**
	 * Singleton accessor
	 */
	public static synchronized ArchiveConfiguration instance(Logger logger)
		throws DatabaseException {
		
		return ArchiveConfiguration.instance(LogFactory.getLog(logger.toString()));
	}

	/* constructs new ArchiveConfiguration from parameters in the config file. */
	protected ArchiveConfiguration(Logger logger) throws DatabaseException {
		logger
				.info("Constructing Archive configuration file as instance of ArchiveConfiguration.");
		// for the time being we do the same as for a re-init
		reinit(logger);
		
		// set oracle.net.tns_admin here, needed for JDBC to work with tnsnames.ora.
		String tnsDir = get(
				"archive.db.tnsFileDirectory");
		if ((null == tnsDir || tnsDir.equals(""))  && !get("archive.db.connection").startsWith("xmldb")) {
			String oraHome = System.getenv("ORACLE_HOME");
			if (oraHome != null && !oraHome.equals("")) {
				tnsDir = oraHome + "/network/admin";
			} else {
				throw new DatabaseException(
						"archiveConfig.properties does not contain value for archive.db.tnsFileDirectory and $ORACLE_HOME not defined. Cannot read tnsnames.ora, aborting...");
			}
		}
		if (!get("archive.db.connection").startsWith("xmldb")) {
			logger.info("Using this tnsnames.ora for DB connection: " + tnsDir+". Setting system property oracle.net.tns_admin accordingly.");
			System.setProperty("oracle.net.tns_admin", tnsDir);
			} 
	}

	/**
	 * re-reads information from config file
	 */
	public void reinit(Logger logger) throws DatabaseException {
		// read config file and store properties in Java system properties
		try {
			readConfig(logger);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Problems while reading "
					+ defaultConfigFileName, e);
			// We don't let this fail yet because the required properties will
			// be checked for in createConfig()
		}
		// fill variables of this with system properties
		createConfig(logger);
		logger.info(this.toString());
	}

	/**
	 * Reads configuration parameters from properties files and stores them as
	 * system properties. Only one file is examined:
	 * $ACS.data/config/archiveConfig.properties,
	 * 
	 * @param logger
	 */
	private void readConfig(Logger logger) throws IOException {
		InputStream propIn;

		props = new Properties();

		// read archiveConfig.properties from Java property
		fileLocation = System.getProperty("archive.configFile");
		if (fileLocation != null && !fileLocation.equals("")) {
			logger.info("----------- Loading archive configuration from: "
					+ System.getProperty("archive.configFile"));
			propIn = new FileInputStream(fileLocation);
			props = readProps(propIn);
			propIn.close();
			// store props
		} else {

			// read archiveConfig.properties from ACS.data/config
			String acsdata = System.getProperty("ACS.data");
			// System.out.println(acsdata);
			if (acsdata != null) {
				acsdata = acsdata + "/config/";
				try {
					propIn = new FileInputStream(acsdata
							+ defaultConfigFileName);
					fileLocation = acsdata + defaultConfigFileName;
					logger.info("----------- Loading " + defaultConfigFileName
							+ " from " + acsdata);
					props = readProps(propIn);
					propIn.close();
				} catch (FileNotFoundException e) {
					logger.severe("No properties file " + defaultConfigFileName
							+ " found in: " + acsdata + "! Bailing out...");
					throw new IOException(
							"No archiveConfig.properties file found!");
				}
			} else {
				throw new IOException(
						"$ACSDATA/ACS.data not defined, cannot read database configuration file.");
			}
		}
	}

	/**
	 * Reads properties from an input stream. Property values are expanded (@see
	 * expand)
	 * 
	 * @param propIn
	 * @return Properties defined in the input stream
	 */
	private Properties readProps(InputStream propIn) throws IOException {
		Properties props = new Properties();
		props.load(propIn);

		// Expand all values
		// -----------------------------
		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String val = props.getProperty(key);
			val = expand(val);
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

		// important: remove whitespace
		s=s.trim();
		
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
	 * Sets up a map for {@link #configParams} that describes the mapping
	 * defnied in archiveConfig.properties which start with "archive." or
	 * "obops.".
	 */
	private void createConfig(Logger logger) throws DatabaseException {
		configParams = new HashMap<String, String>();
		// Now all properties are stored in props
		// They can be copied to configParams:
		for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.startsWith("archive.") || key.startsWith("obops.")) {
				configParams.put(key, (String) props.get(key));
				logger.finest("Stored config param: " + key + "="
						+ configParams.get(key));
			} else {
				logger.finest("Ignored system property: " + key + "="
						+ configParams.get(key));
			}
		}

		// set variables from properties, for convenience

		/* Now verify settings of properties */
		logger.info("Verifying properties in archiveConfig.properties.");

		// connectionString and simulate original dbBackend
		String dbConnection = get("archive.db.connection");
		if (dbConnection == null) {
			logger
					.severe("Property archive.db.connection undefined! Check archiveConfig.properties.");
			throw new DatabaseException(
					"No Database backend specified (property archive.db.connection undefined).");
		} else {
			if (dbConnection.startsWith("xmldb:")) {
				dbBackend = "xmldb";
				configParams.put("archive.xmldb.location",dbConnection);
				configParams.put("archive.xmldb.cache", "100");
				configParams.put("archive.xmldb.name", "db");
				configParams.put("archive.xmldb.driver","org.exist.xmldb.DatabaseImpl");
			} else {
				// in case of oracle, the connection string contains the service
				// alias
				dbBackend = "oracle";
			}
		}

		// dbBackend just left here to cover the case that the connectionString
		// above is wrong
		if (dbBackend == null) {
			logger
					.severe("db.backend undefined! Check archiveConfig.properties: "
							+ "archive.db.connection is not properly defined.");
			throw new DatabaseException(
					"Property archive.db.connection not properly defined.");
		}

		if ("operational".equals(get("archive.db.mode"))) {
			testMode = false;
			/*
			 * if we run in operational mode then some additional checks are
			 * carried out
			 */
			if (dbBackend.equals("xmldb")) {
				logger
						.severe("When running in operational mode, Oracle must be used. Check archiveConfig file.");
				throw new DatabaseException(
						"Only Oracle DB can be used in operational mode");
			}
			if (get("archive.oracle.user").equals("almatest")) {
				/* In operational mode we must not use almatest user */
				logger
						.severe("When running in operational mode, user almatest is not allowed. Check archiveConfig file.");
				throw new DatabaseException(
						"Permission denied: user almatest can not be used in operational mode");
			}
			// In operational mode, the values of archive.statearchive.* must
			// not
			// be specified, otherwise ArchiveConfiguration will throw an error.
			// Nonetheless, three properties will be displayed to the outside:
			//archive.statearchive.user --> archive.oracle.user
			//archive.statearchive.passwd --> archive.oracle.passwd
			//archive.statearchive.connection --> archive.db.connection
			if ((get("archive.statearchive.user")!=null && !"".equals(get("archive.statearchive.user"))) 
					|| (get("archive.statearchive.passwd")!=null && !"".equals(get("archive.statearchive.passwd")))
					|| (get("archive.statearchive.connection")!=null && !"".equals(get("archive.statearchive.connection")))) {
				logger
				.severe("When running in operational mode, specification of archive.statearchive.* is not allowed (they will be set internally). Check archiveConfig file.");
		throw new DatabaseException(
		"When running in operational mode, specification of archive.statearchive.* is not allowed (they will be set internally). Check archiveConfig file.");
			}
			// set archive.statearchive.* properties:
			configParams.put("archive.statearchive.user", get("archive.oracle.user"));
			configParams.put("archive.statearchive.passwd", get("archive.oracle.passwd"));
			configParams.put("archive.statearchive.connection", get("archive.db.connection"));
		} else {
			testMode=true;
		}

		/*
		 * The property archive.ngas.interface can either contain a directory
		 * name or a command line for the ngasArchiveClient including all
		 * required parameters.
		 */
		if (get("archive.ngast.interface") == null) {
			/* In operational mode we must store files on NGAS! */
			logger
					.severe("Property archive.ngast.interface undefined, but must be either ngamsArchiveClient cmd or test:...! Check archiveConfig.properties.");
			throw new DatabaseException(
					"No NGAS interface specified.");
		}

		if (get("archive.ngast.interface").indexOf("ngamsArchiveClient") < 0) {
			if (!get("archive.ngast.interface").startsWith("test:")) {
				logger
						.severe("Property archive.ngast.interface must specify ngamsArchiveClient cmd or test:...! Check archiveConfig.properties.");
				throw new DatabaseException(
						"No ngamsArchiveClient or test mode specified.");
			}
			configParams.put("archive.ngast.storeInNgast", "False");
			configParams.put("archive.ngast.testDir", get(
					"archive.ngast.interface").substring(5,
					get("archive.ngast.interface").length()));
		} else {
			// operational mode for NGAS!!!

			// operational mode if not set will default delayed
			if (get("archive.ngast.storeInNgast") == null) {
		 		configParams.put("archive.ngast.storeInNgast", "Delayed");
			}

			// BulkStore stuff, everything is triggered by the availability of
			// archive.ngast.servers
			if (get("archive.ngast.servers") != null) {
				if (get("archive.ngast.servers").indexOf(':')<1) {
					throw new DatabaseException(
					"The value of archive.ngast.servers must be a comma-separated list of server:port pairs. Please check archiveConfig.properties.");
				}
				ngasBufferDir = get("archive.ngast.bufferDir");
				if (ngasBufferDir == null) {
					logger
							.severe("NGAS archiving requested but archive.ngast.bufferDir undefined! Check archiveConfig.properties.");
					throw new DatabaseException(
							"No NGAS buffer directory specified in archive configuration.");
				}
				configParams.put("archive.ngast.testDir", ngasBufferDir
						+ "/NGAMS_ARCHIVE_CLIENT/queue"); // used by
				// bulkreceiver
				// we will later construct ngamsArchiveCommand from interface,
				// servers and bufferDir
			} else {
				logger
						.severe("Operational mode but archive.ngast.servers undefined! Check archiveConfig.properties.");
				throw new DatabaseException(
						"No NGAS servers specified in operational archive configuration.");
			}

			// now we construct the value of archive.ngast.clientParams, which
			// is used to start the archiveNgamsClient:
			// first check whether host is specified. This must not be the case:
			if (get("archive.ngast.interface").indexOf("-host") >= 0
					|| get("archive.ngast.interface").indexOf("-rootDir") >= 0
					|| get("archive.ngast.interface").indexOf("-port") >= 0
					|| get("archive.ngast.interface").indexOf("-servers") >= 0) {
				logger
						.severe("host, port, rootDir or servers MUST not be specified in archive.ngast.interface property. Please check archiveConfig.properties.");
				throw new DatabaseException(
						"Inconsistency in archiveConfig.properties: host, port, rootDir or servers overspecified in archive.ngast.interface.");
			}
			configParams.put("archive.ngast.clientParams",
					get("archive.ngast.interface") + " -servers "
							+ get("archive.ngast.servers") + " -rootDir "
							+ ngasBufferDir);

			if (get("archive.bulkreceiver.schema") == null) {
				/* In operational mode the bulkreceiver has to be configured */
				logger
						.severe("Operational mode but bulkreceiver.schema not configured! Check archiveConfig file.");
				throw new DatabaseException(
						"bulkreceiver.schema not configured for operational mode.");
			} else {
				// if (get("archive.bulkreceiver.DataBufferRetry") == null) {
				// logger
				// .severe("Operational mode but bulkreceiver.DataBufferRetry
				// not configured! Check archiveConfig file.");
				// throw new DatabaseException(
				// "bulkreceiver.DataBufferRetry not configured for operational
				// mode.");
				// }
				if (get("archive.bulkreceiver.DataBufferMax") == null) {
					logger
							.severe("Operational mode but bulkreceiver.DataBufferMax not configured! Check archiveConfig file.");
					throw new DatabaseException(
							"bulkreceiver.DataBufferMax not configured for operational mode.");
				}
				if (get("archive.bulkreceiver.BufferThreadNumber") == null) {
					logger
							.severe("Operational mode but bulkreceiver.BufferThreadNumber not configured! Check archiveConfig file.");
					throw new DatabaseException(
							"bulkreceiver.BufferThreadNumber not configured for operational mode.");
				}
				// if (get("archive.bulkreceiver.BufferThreadWaitSleep") ==
				// null) {
				// logger
				// .severe("Operational mode but
				// bulkreceiver.BufferThreadWaitSleep not configured! Check
				// archiveConfig file.");
				// throw new DatabaseException(
				// "bulkreceiver.BufferThreadWaitSleep not configured for
				// operational mode.");
				// }
				if (get("archive.bulkreceiver.FetchThreadRetry") == null) {
					logger
							.severe("Operational mode but bulkreceiver.FetchThreadRetry not configured! Check archiveConfig file.");
					throw new DatabaseException(
							"bulkreceiver.FetchThreadRetry not configured for operational mode.");
				}
				if (get("archive.bulkreceiver.FetchThreadRetrySleep") == null) {
					logger
							.severe("Operational mode but bulkreceiver.FetchThreadRetrySleep not configured! Check archiveConfig file.");
					throw new DatabaseException(
							"bulkreceiver.FetchThreadRetrySleep not configured for operational mode.");
				}
			}
		}

		// TODO check for other variables? Ie. archive.bulkstore...?

		if (dbBackend.equals("oracle") && testMode
				&& !get("archive.oracle.user").equals("almatest")) {
			logger
					.severe("When running in test mode, user must be almatest. Check archiveConfig file.");
			throw new DatabaseException(
					"Permission denied: only user almatest can run test mode");
		}
	}

	/*
	 * returns string representation of the configuration, i.e. the parameter
	 * name/value pairs
	 */
	public String toString() {
		// TODO put each property into a new line.
		StringBuffer out = new StringBuffer("Archive configuration: \n");
		Vector<String> mykeys = new Vector<String>(configParams.keySet());
		Collections.sort(mykeys);
		for (Iterator<String> it = mykeys.iterator(); it
				.hasNext();) {
			String name = it.next();
			if (name.endsWith("passwd")) {
				out.append("   - " + name + "= [HIDDEN]\n");
			} else {
				out.append("   - " + name + "=" + configParams.get(name) + "\n");
			}
		}
		return out.toString();
	}

	/*
	 * <archiveconfiguration> <config name="" value=""> ...
	 * </archiveconfiguration>
	 */
	public Element toElement() {
		Element root = new Element("archiveconfiguration");
		Iterator<String> iter = configParams.keySet().iterator();
		while (iter.hasNext()) {
			Element config = new Element("config");
			String name = iter.next();
			config.setAttribute("name", name);
			config.setAttribute("value", configParams.get(name));
			root.addContent(config);
		}
		return root;
	}

	public String toXmlString() {
		Element element = this.toElement();

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		// XMLOutputter out = new XMLOutputter(" ",true,"UTF-8");
		String xml = out.outputString(element);
		return xml;
	}

	/* returns value of parameter, if defined. Otherwise returns null */
	public String get(String paramName) {
		return configParams.get(paramName);
	}
	
	// Oracle case: returns a JDBC URL based on the service alias specified in property name
	public String getConnectionURL(String propertyName) {
		return "jdbc:oracle:thin:@"+get(propertyName);
	}

	/**
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public List<InetSocketAddress> getNasgServers() throws DatabaseException {
		final String server = get("archive.ngast.servers");
		if (server == null || server.trim().isEmpty()) {
			throw new DatabaseException("Property archive.ngast.servers not defined in archiveConfig.properties");
		}

		String[] servers = server.split(",");
		List<InetSocketAddress> serverList = new ArrayList<>();
		for (String nextServer : servers) {
			String[] addressParts = nextServer.split(":");
			String serverName = addressParts[0];
			String port = addressParts[1];
			LOG.info("NGAMS mode: using server " + serverName + ":" + port);
			serverList.add(new InetSocketAddress(serverName, Integer.parseInt(port)));
		}
		
		return serverList;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean storeInNgas() {
		return "true".equalsIgnoreCase(get("archive.ngast.storeInNgast"))
				// delayed? Your guess is as good as mine...
			|| "delayed".equalsIgnoreCase(get("archive.ngast.storeInNgast"));
	}	
}
