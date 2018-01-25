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
package alma.hibernate.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.jdbc.Work;

import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.ImplLangEnum;
import alma.acs.tmcdb.ComponentType;
import alma.acs.tmcdb.Configuration;

import com.cosylab.cdb.jdal.hibernate.HibernateUtil;
import com.cosylab.cdb.jdal.hibernate.HibernateUtil.HibernateUtilException;

public class HibernateXmlTypeTest extends TestCase {

    private final static int DEFAULT_BUF_LEN = 64 * 1024;
	private final String CREATE_HSQLDB_TMCDB_SWCORE = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_swconfigcore/CreateHsqldbTables.sql";
	private final String CREATE_HSQLDB_TMCDB_SWEXT  = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_swconfigext/CreateHsqldbTables.sql";
	private final String CREATE_HSQLDB_TMCDB_HWMON  = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_hwconfigmonitoring/CreateHsqldbTables.sql";
	private final String DROP_HSQLDB_TMCDB_SWCORE = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_swconfigcore/DropAllTables.sql";
	private final String DROP_HSQLDB_TMCDB_SWEXT  = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_swconfigext/DropAllTables.sql";
	private final String DROP_HSQLDB_TMCDB_HWMON  = System.getenv("ACSDATA") + "/config/DDL/hsqldb/TMCDB_hwconfigmonitoring/DropAllTables.sql";
    
	private SessionFactory factory;
	private AnnotationConfiguration configuration;

	enum TestCase {
		HSQLDB,
		ORACLE,
		C3P0_HSQLDB,
		C3P0_ORACLE
	};

	public void setUp() {
		if( System.getenv("ACSDATA") == null )
			fail("ACSDATA environment variable is not set, will not continue");
	}

	public void tearDown() {
		HibernateUtil.clearInstance();
	}

	public void testXMLTypeWithHSQLDB() throws Exception {
		trySeveralThingsWithComponent(TestCase.HSQLDB);
	}

	public void testXMLTypeWithOracle() throws Exception {
		trySeveralThingsWithComponent(TestCase.ORACLE);
	}

	public void testXMLTypeWithC3P0AndHSQLDB() throws Exception {
		trySeveralThingsWithComponent(TestCase.C3P0_HSQLDB);
	}

	public void testXMLTypeWithC3P0AndOracle() throws Exception {
		trySeveralThingsWithComponent(TestCase.C3P0_ORACLE);
	}

	void trySeveralThingsWithComponent(TestCase testCase) throws Exception {

		initHibernate(testCase);
		Session session = null;
		Transaction tx = null;

		try {
			
			// Create DB (HSQLDB) and initial objects
			createDB();

			session = factory.openSession();
			tx = session.beginTransaction();

			Configuration conf = new Configuration();
			conf.setConfigurationName("Configuration");
			conf.setFullName("Full name");
			conf.setDescription("Description");
			conf.setActive(true);
			conf.setCreationTime(new Date());
			session.save(conf);

			ComponentType ct = new ComponentType();
			ct.setIDL("IDL");
			session.save(ct);

			// Start testing the XMLType getters/setters
			Component c = new Component();
			c.setComponentName("Component");
			c.setComponentType(ct);
			c.setConfiguration(conf);
			c.setCode("code");
			c.setPath("path");
			c.setImplLang(ImplLangEnum.CPP);
			c.setIsAutostart(true);
			c.setIsControl(false);
			c.setIsDefault(true);
			c.setKeepAliveTime(-1);
			c.setRealTime(false);
			c.setXMLDoc(null);            // nullSafeSet is working with null data
			session.saveOrUpdate(c);
			tx.commit();
			session.close();

			session = factory.openSession();
			tx = session.beginTransaction();
			c = (Component)session.createCriteria(Component.class).uniqueResult();
			assertNotNull(c);
			assertNull(c.getXMLDoc());    // nullSafeGet is working with null data
			c.setXMLDoc("<doc/>");        // nullSafeSet is working with not-null data
			tx.commit();
			session.close();

			session = factory.openSession();
			tx = session.beginTransaction();
			c = (Component)session.createCriteria(Component.class).uniqueResult();
			assertNotNull(c);
			assertNotNull(c.getXMLDoc()); // nullSafeGet is working with not-null data
			tx.commit();

		} catch (Exception e) {
			tx.rollback();
			fail("Something failed, had to roll back");
		} finally {
			session.close();
			session = factory.openSession();
			tx = session.beginTransaction();
			dropDB(session);
			session.close();
		}
	}

	private void initHibernate(TestCase testCase) throws HibernateUtilException {

		String fileName = "";

		switch( testCase ) {
			case HSQLDB:
				fileName = "hsqldb-hibernate.cfg.xml";
				break;
			case ORACLE:
				fileName = "oracle-hibernate.cfg.xml";
				break;
			case C3P0_HSQLDB:
				fileName = "c3p0-hsqldb-hibernate.cfg.xml";
				break;
			case C3P0_ORACLE:
				fileName = "c3p0-oracle-hibernate.cfg.xml";
				break;
		}

		configuration = new AnnotationConfiguration();
        factory = configuration.configure(fileName).buildSessionFactory();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility things, copied from cdb_rdb test classes... this should be somewhere else publicly available
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	private void createDB() throws Exception {

		final String url = configuration.getProperty("hibernate.connection.url");

		Session session = factory.openSession();
		Transaction tx  = session.beginTransaction();

		if( url.contains("oracle") )
			dropDB(session);
		else {
			session.doWork( new Work() {
				public void execute(Connection conn) throws SQLException {
					runScriptFile(CREATE_HSQLDB_TMCDB_SWCORE, conn);
					runScriptFile(CREATE_HSQLDB_TMCDB_SWEXT, conn);
					runScriptFile(CREATE_HSQLDB_TMCDB_HWMON, conn);
					return;
				}
			});
		}
		tx.commit();
		session.close();
	}

	private void dropDB(Session session) throws Exception {

		final String url = configuration.getProperty("hibernate.connection.url");

		if( url.contains("oracle") ) {
			for (Class<?> clazz: new Class<?>[]{Component.class, ComponentType.class, Configuration.class}) {
				List<?> res = session.createCriteria(clazz).list();
				for (Object object : res) {
					session.delete(object);
				}
			}
		}
		else {
			session.doWork( new Work() {
				public void execute(Connection conn) throws SQLException {
					runScriptFile(DROP_HSQLDB_TMCDB_HWMON, conn);
					runScriptFile(DROP_HSQLDB_TMCDB_SWEXT, conn);
					runScriptFile(DROP_HSQLDB_TMCDB_SWCORE, conn);
					conn.commit();
				}
			});
		}
	}

    private void runScriptFile( String script, Connection conn ) throws SQLException {

    	String sql = "";
    	
    	try {
			// try to get hold of the script
			InputStream is = getResourceStream(script);
			sql = fileToString(new InputStreamReader(is));
		} catch (IOException e) {
			// convert to runtime, as this is only used for testing
			throw new RuntimeException(e);
		}
		
    	runScript(sql, conn);
    }

    private void runScript( String sql, Connection conn ) throws SQLException {

    	Statement stmt = conn.createStatement();
    	String[] statements = sql.split( ";", -1 );
    	for( int i = 0; i < statements.length; i++ ) {
    		String statement = statements[i].trim();
    		if( statement.length() == 0 ) {
    			// skip empty lines
    			continue;
    		}
    		stmt.execute( statement );
    	}
    }

	private InputStream getResourceStream(String pathname) throws IOException {
        InputStream s = null; // this is the stream we return

        // Look for the resource on the file system
        // -----------------------------------------
        File f = new File( pathname );
        s = new FileInputStream( f );

        return s;
	}

    private String fileToString( Reader reader ) throws IOException {

        BufferedReader br = new BufferedReader( reader );
        StringBuffer sb = new StringBuffer();
        char[] buff = new char[DEFAULT_BUF_LEN];
        while( br.ready() ) {
            int nread = br.read( buff, 0, buff.length );
            if( nread <= 0 ) {
                break;
            }
            sb.append( buff, 0, nread );
        }
        br.close();
        return sb.toString();
    }

}
