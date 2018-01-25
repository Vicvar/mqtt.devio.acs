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
 * This is a helper class that is in charge of reading the configuration file (archiveConfig.properties)
 * Information about schema name, username, database url, etc. is located in this file.
 * 
 * @author Pablo Burgos
 * @version %I%,%G%
 * @since ACS-8_0_0-B
 *
 */
package alma.archive.tmcdb.persistence;

import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import alma.archive.database.helpers.DBConfiguration;

public class TMCDBConfig {

	private static TMCDBConfig instance = null;

	public static enum DBType {
		ORACLE, HSQLDB;
	}

	private DBType dbType;
	private String dbUser;
	private String dbPassword;
	private String dbUrl;
	private String confName;
	private String dbConnectionEnabled="true";
	private String monitoringOnlyEnabled = "false";
	private String simulatedAntennas = null;
	private boolean profilingEnabled = false;
	private int collector_interval = 60;
	private final Logger logger;
	private static String brokerEnabled = "true";
	private static String broker_url = "tcp://127.0.0.1:61616";

	private TMCDBConfig(Logger logger) {
		this.logger = logger;
		//fetchConfiguration();
	}

	public static TMCDBConfig getInstance(Logger logger) {
		if (instance == null) {
			synchronized (TMCDBConfig.class) {
				if (instance == null) {
					instance = new TMCDBConfig(logger);
				}
			}
		}
		return instance;
	}

	public String getDbUser() {
		return dbUser;
	}
	
	public String getDbPassword() {
		return dbPassword;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public DBType getDbType() {
		return dbType;
	}

	public String getConfigurationName() {
		return confName;
	}

	public String getBrokerURL() {
		return broker_url;
	}

	public boolean isDBConnectionEnabled(){
		if (dbConnectionEnabled.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	public HashSet<String> getAntennaSimulatedSet() {
		HashSet<String> antennaSimulatedSet = null;
		if (simulatedAntennas != null) {
			antennaSimulatedSet = new HashSet<String>(10);
			StringTokenizer st = new StringTokenizer(simulatedAntennas, ",");
			while (st.hasMoreTokens()) {
				antennaSimulatedSet.add(st.nextToken());
			}
		}
		return antennaSimulatedSet;
	}

	public boolean isMonitoringOnlyEnabled() {
		if (monitoringOnlyEnabled.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isBrokerEnabled() {
		if (brokerEnabled.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Merged from ACS-9_0_0-B, assuming it's needed also later.
	 */
	public boolean isProfilingEnabled() {
		return profilingEnabled;
	}

	/**
	 * Merged from ACS-9_0_0-B, assuming it's needed also later.
	 */
	public int getCollectorInterval() {
		return collector_interval;
	}

	private void fetchConfiguration() {
		DBConfiguration dbConfig = null;
		
		try {
			dbConfig = DBConfiguration.instance(logger);
		} catch(Exception e) {
			throw new RuntimeException("Cannot start TMCDBConfig: ", e);
		}

		String mode = dbConfig.get("archive.db.mode");
		if( mode.equals("operational") )
			dbType = DBType.ORACLE;
		else if( mode.equals("test") )
			dbType = DBType.HSQLDB;
		else
			throw new RuntimeException("Cannot start TMCDBConfig, invalid 'archive.db.mode' value: " + mode);

		dbUser     = dbConfig.get("archive.tmcdb.user");
		dbPassword = dbConfig.get("archive.tmcdb.passwd");
		dbUrl      = dbConfig.get("archive.tmcdb.connection");

		// RTO: Maybe the property is useless and the only mechanism should be the env variable?
		String envConfiguration = System.getenv("TMCDB_CONFIGURATION_NAME");
		if( envConfiguration != null && envConfiguration.length() > 0 )
			confName = envConfiguration;
		else
			confName = dbConfig.get("archive.tmcdb.configuration");

		dbConnectionEnabled = dbConfig.get("archive.tmcdb.monitoring.enabled");
		if (dbConnectionEnabled == null) {
			dbConnectionEnabled = "true";
		}
		//Next option allow for mixed SQL solution.
		brokerEnabled = dbConfig.get("archive.tmcdb.monitoring.broker_enable");
		if (brokerEnabled == null) {
			brokerEnabled = "false";
		}
		broker_url = dbConfig.get("archive.tmcdb.monitoring.broker_url");
		if (broker_url == null) {
			broker_url = "tcp://localhost:61616";
		}
		monitoringOnlyEnabled = dbConfig.get("archive.tmcdb.monitoring.only");
		if (monitoringOnlyEnabled == null) {
			monitoringOnlyEnabled = "false";
		}
		simulatedAntennas = dbConfig.get("archive.tmcdb.monitoring.simulatedantenna");
		
		profilingEnabled = Boolean.valueOf(dbConfig.get("archive.tmcdb.monitoring.profiling"));

		try {
			collector_interval = Integer.parseInt(dbConfig.get("archive.tmcdb.monitoring.interval"));
		} catch (Exception e) {
			collector_interval = 60;
		}
		if (collector_interval < 10 || collector_interval > 300) {
			logger.info("Adjusted 'archive.tmcdb.monitoring.interval' from illegal value " + collector_interval + " to 60 seconds.");
			collector_interval = 60;
		}
	}

}
