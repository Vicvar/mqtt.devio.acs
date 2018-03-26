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
 * This is helper class selects the EntityManager factory on runtime,
 * according to information retrieved from dbConfig.properties files.
 * Supported databases are HSQLDB and ORACLE.
 * @author Pablo Burgos
 * @version %I%,%G%
 * @since ACS-8_0_0-B
 *
 *
 */
package alma.archive.tmcdb.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TMCDBPersistence {

	private static EntityManagerFactory entityManagerFactory = null;
	private final Logger logger;

	public TMCDBPersistence(Logger logger) {
		this.logger = logger;
		entityManagerFactory = getEntityManagerFactory();
	}

	public EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

	public void close() {
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}
	}

	private EntityManagerFactory getEntityManagerFactory() {

		TMCDBConfig config = TMCDBConfig.getInstance(logger);

		Map<String, String> properties = new HashMap<String, String>();
		// TODO Encrypt password in archiveConfig.properties with Jasypt
		properties.put("hibernate.connection.username", config.getDbUser());
		properties.put("hibernate.connection.password", config.getDbPassword());
		properties.put("hibernate.connection.url", config.getDbUrl());

		if (config.getDbType() == TMCDBConfig.DBType.ORACLE) {
			return Persistence.createEntityManagerFactory(
					"TMCDBOwlDaemonOracle", properties);

		} else {
			// If it's not oracle, assume we are using HSQLDB as backend
			return Persistence.createEntityManagerFactory(
					"TMCDBOwlDaemonHSQLDB", properties);
		}
	}
}
