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
 *    Created on Nov 5, 2003
 *
 */

// $Author: hmeuss $
// $Date: 2012/06/11 09:00:13 $
// $Log: OracleInternalIfImplTest.java,v $
// Revision 1.26  2012/06/11 09:00:13  hmeuss
// re-added test for uniqueness of UIDs (in testStore).
//
// Revision 1.25  2012/05/24 15:14:33  hmeuss
// added test for uniqueness of UIDs
//
// Revision 1.24  2011/06/14 14:02:57  mbauhofe
// Replaced XML_HISTORY concept with UID_LOOKUP (view).
// Some clean up/core reorganization.
//
// Revision 1.23  2011/05/20 16:22:37  mbauhofe
// COMP-5433 fix. (missing WHERE)
//
// Revision 1.22  2010/09/20 15:25:43  hmeuss
// added interface for transaction handling
//
// Revision 1.21  2009/04/30 15:31:31  hmeuss
// imrpoved test regarding namespace handling
//
// Revision 1.20  2009/04/22 11:26:50  hmeuss
// added test for incremental updates for XMLstore
//
// Revision 1.19  2008/08/20 10:01:17  hmeuss
// Had to update the test due to the new reinit functionality
//
// Revision 1.18  2008/07/16 14:55:28  hmeuss
// Changed test setup:
// - removed test for restore, which is unused
// - changed timestamp test logic, which was wrong
//
// Revision 1.17  2007/07/05 13:27:36  hmeuss
// Changed ID storage in Oracle: Now sequences are used
//
// Revision 1.16  2007/04/20 09:24:13  hmeuss
// improved sanity check and the test for it
//
// Revision 1.15  2007/04/16 13:32:39  hmeuss
// added test for sanity check
//
// Revision 1.14  2006/09/15 14:17:43  hsommer
// fixed exceptions because of new Range class.
//
// Revision 1.13  2006/04/21 14:33:01  hmeuss
// New version, with test for new UIDLibrary
//
// Revision 1.12  2006/04/07 14:54:29  hmeuss
// First version using the new UID scheme
//
// Revision 1.11  2006/03/31 09:13:10  hmeuss
// removed unused variable
//
// Revision 1.10  2005/09/12 11:55:23  hmeuss
// Adapted tests to new internal IF
//
// Revision 1.9  2005/07/21 12:29:48  hmeuss
// Changed design of test area
//
// Revision 1.8  2005/01/13 10:45:50  hmeuss
// - Added call to init() in all tests.
//
// Revision 1.7  2004/09/17 08:50:38  hmeuss
// Added ModuleCriticalException that will be propagated upwards through all classes
//
// Revision 1.6  2004/08/19 14:58:08  hmeuss
// Changed behaviour of get in Oracle implementation
//
// Revision 1.5  2004/06/02 09:55:19  hmeuss
// Modification of ArchiveTimestamp conversion.
//
// Revision 1.4  2004/06/02 08:09:30  hmeuss
// *** empty log message ***
//
// Revision 1.3  2004/05/07 12:52:22  hmeuss
// removal of schema is no longer necessary due to test mode
//
// Revision 1.2  2004/04/07 09:32:36  hmeuss
// Implemented namespaces test
//
// Revision 1.1  2004/04/05 13:59:09  hmeuss
// Internal IF implementation adapted to Oracle
//
// Revision 1.6  2004/02/11 16:21:25  hmeuss
// DB2 tests are now integrated into TAT
//
// Revision 1.5  2004/01/29 14:31:19  hmeuss
// Adapted to the new interface
//
// Revision 1.4  2004/01/19 16:32:14  hmeuss
// Added test that works with obsproject docs and a real DAD file. Use with care, configuration management is a bit sloppy.
//
// Revision 1.3  2004/01/14 14:13:32  hmeuss
// *** empty log message ***
//
// Revision 1.2  2003/12/18 13:20:05  hmeuss
// *** empty log message ***
//
// Revision 1.1  2003/11/26 13:26:43  hmeuss
// Migrated to ACS 3.0
// 

package alma.archive.database.oracle;

import static alma.archive.database.helpers.DatabaseHelper.traceToString;

import java.io.FileReader;
import java.io.StringReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;
import oracle.jdbc.OracleConnection;

import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import alma.acs.container.archive.Range;
import alma.archive.database.interfaces.DBCursor;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.exceptions.cursor.CursorClosedException;
import alma.archive.exceptions.general.EntityDoesNotExistException;
import alma.archive.exceptions.general.EntityExistsException;
import alma.archive.exceptions.general.UnknownSchemaException;
import alma.archive.wrappers.DocumentData;
import alma.archive.wrappers.Permissions;
import alma.archive.wrappers.ResultStruct;

/**
 * @author hmeuss
 *
 */
public class OracleInternalIfImplTest extends TestCase {

    private static final Logger LOGGER = Logger.getAnonymousLogger();
    
    static {
      LOGGER.setLevel(Level.WARNING);
      // 4=Info, 5=Warning
      /*
      System.setProperty("ACS.log.minlevel.namedloggers", "SchemaLoader=5,5");
      LOGGER.setUseParentHandlers(false);
      LOGGER.setLevel(Level.FINEST);
      LOGGER.addHandler(new Handler() {
			private int reinitCalls = 0;
			
			@Override
			public void publish(LogRecord record) {
				String string = record.getLevel() + " [Thread-" + record.getThreadID() + "]";
				String className = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
				string += " " + className + "." + record.getSourceMethodName();
				string += ": " + record.getMessage();
				System.out.println(string);
				if (className.equals("DatabaseConnectionPool") && record.getSourceMethodName().equals("reinit") && record.getMessage().contains("->")) {
					reinitCalls++;
					if (reinitCalls > 1) throw new RuntimeException("initialisation called more than once");
				}
			}

			@Override
			public void flush() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void close() throws SecurityException {
				// TODO Auto-generated method stub
				
			}
			
		});
		*/
    }

    private static final String USER = "testUser";

    private static final String SCHEMA_NAME = "testSchema";
    
    private static final String SCHEMA = "<foo/>";
    
    private static final String TEST_DOC = "<fooFoo/>";
    
    private static final String DOC_OWNER = "docOwner";
    
    private static final String NS_DOC =
        "<nsz:a xmlns:nsz=\"locz\">" +
        " <nsy:a xmlns:nsy=\"locy\">" +
        "  <nsz:a/>" +
        "  <a/>" +
        "  <nsz:b/>" +
        "  <nsy:b/>" +
        " </nsy:a>" +
        "</nsz:a>";
    
    private static final String REAL_DOC_LOC = "../examples/testFITS.xml";
    
    private static final String UPDATE_DOC = "<updated/>";
    
    private static final String OBS_NAME = "testObsProj";

    
    private InternalIfImpl database;
    
    private SchemaManager schemaMan;
    
    private Range range;

    private URI testSchemaURI;

    private URI secondSchemaURI;

    
    @Override
    protected void setUp() throws Exception {
		database = (InternalIfImpl) InternalIFFactory.getInternalIF(LOGGER);

        database.init();
		Connection connection = database.getConnectionPool().getConnection();
        DatabaseMetaData meta = connection.getMetaData();
        LOGGER.severe("JDBC driver version is " + meta.getDriverVersion());
        connection.close();
        

        database.getWriter().cleanTestArea();
        schemaMan = database.getSchemaManager(USER);
        range = new Range(database.getIdentifierManager().getNewRange()); 


        // make sure that testschema exists, set testSchemaURI:
        try {
            testSchemaURI = schemaMan.getSchemaURI(SCHEMA_NAME);
        } catch (UnknownSchemaException e) {
            try {
                final List<Namespace> namespaces = new LinkedList<Namespace>();
                namespaces.add(Namespace.getNamespace("nsz", "locz"));
                namespaces.add(Namespace.getNamespace("nsy", "locy"));
                testSchemaURI = range.next();
                addSchema(
                    SCHEMA_NAME, SCHEMA, testSchemaURI, namespaces);
            } catch (Exception e1) {
                // ignore this exception: 
                // table already exists due to test area inconsistencies...
                LOGGER.warning(traceToString(e1));
            }
        }
        // we need a second schema for UID uniqueness test:
        try {
        	secondSchemaURI = schemaMan.getSchemaURI("secondSchema");
        } catch (UnknownSchemaException e) {
            try {
            	secondSchemaURI = range.next();
                addSchema(
                    "secondSchema", SCHEMA, secondSchemaURI, new LinkedList<Namespace>());
            } catch (Exception e1) {
                // ignore this exception: 
                // table already exists due to test area inconsistencies...
                LOGGER.warning(traceToString(e1));
            }
        }
    }

    /**
     * @throws SQLException 
     *
     */
    public static void setup(Connection connection) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        LOGGER.info("JDBC driver version is " + meta.getDriverVersion());

        try (Statement s = connection.createStatement()) {
    		try (ResultSet rs = s.executeQuery("select count(*) from xml_metainfo where name = 'archiveID'")) {
	    		rs.next();
	    		int numRows = rs.getInt(1);
	    		if (numRows == 0) {
	    			int inserted = s.executeUpdate("insert into xml_metainfo (name, value) values ('archiveID', 'OracleInternalIFImplTest')");
	    			if (inserted != 1) throw new RuntimeException("couldn't insert an archiveID in xml_metainfo");
	    		}
    		}
    		try (ResultSet rs = s.executeQuery("select count(*) from user_tables where table_name = 'XML_SCHEMA_ENTITIES'")) {
    			// no idea who keeps dropping this table, but I patch it for now. I intend to come back to these tests (and the modules)
    			// and rewrite them from scratch. They are so unnecessarily complex.
	    		rs.next();
	    		int numRows = rs.getInt(1);
	    		LOGGER.info("num rows: " + numRows);
	    		if (numRows == 0) {
	    			int inserted = s.executeUpdate("create table XML_SCHEMA_ENTITIES ("
	    					+ " ARCHIVE_UID VARCHAR2(33) NOT NULL,"
	    					+ " SCHEMANAME VARCHAR2(32) NOT NULL,"
	    					+ " VERSION NUMBER(16) NOT NULL,"
	    					+ " TIMESTAMP TIMESTAMP(6) NOT NULL,"
	    					+ " XML CLOB,"
	    					+ " SCHEMAUID VARCHAR2(33),"
	    					+ " OWNER VARCHAR2(128),"
	    					+ " DELETED NUMBER(1),"
	    					+ " READPERMISSIONS VARCHAR2(128),"
	    					+ " WRITEPERMISSIONS VARCHAR2(128),"
	    					+ " HIDDEN NUMBER(1),"
	    					+ " DIRTY NUMBER(1),"
	    					+ " VIRTUAL NUMBER(1),"
	    					+ " CONSTRAINT SCHEMA_UID_PK PRIMARY KEY (ARCHIVE_UID))");
	    			if (inserted != 1) throw new RuntimeException("couldn't create xml_schema_entities");
	    		}
    		}
    	}
    	finally {
    		connection.close();
    	}
	}

	@Override
    protected void tearDown() throws Exception {
        database.close();
    }

    public void testUpdate() throws Exception {
        // store doc
        final URI uid = range.next();
        database.store(
            uid, TEST_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
            USER, true);

        // check schema
        assertEquals(SCHEMA_NAME, database.getSchemaOfUid(uid));
        
        // get doc data with time stamp
        DocumentData ddata = database.status(uid, SCHEMA_NAME, USER);
        // update doc to have a more intersting history
        database.update(
            uid, ddata.getTimestamp(), TEST_DOC, testSchemaURI, false, USER);

        // get doc data with timestamp
        ddata = database.status(uid, SCHEMA_NAME, USER);
        // update doc
        database.update(
            uid, ddata.getTimestamp(), UPDATE_DOC, testSchemaURI, false, USER);

        // compare latest version
        assertEquals(UPDATE_DOC, database.get(uid, SCHEMA_NAME, USER).trim());
        
    }
    
    public void testStore() throws Exception {
        // store doc
        final URI uid = range.next();
        database.store(
            uid, TEST_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
            USER, true);
          // and now we store under the same UID again, but under a different schema table:
        try {
        	database.store(
                uid, TEST_DOC, secondSchemaURI, "secondSchema", USER, new Permissions(),
                USER, true);
        	//System.out.println("yyyy "+database.get(uid, "secondSchema", USER));
            
        	fail("No exception thrown when UID is used twice to store.");
        } catch (EntityExistsException e) {
        	// expected
        }
        
        
    }

    public void testPermission() throws Exception {
        // store doc
        final URI uid = range.next();
        database.store(
            uid, TEST_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
            USER, true);

        // change permissions
        database.setPermission(
            uid, SCHEMA_NAME, new Permissions("a", "b"), USER);
        // verify permission change
        DocumentData ddata = database.status(uid, SCHEMA_NAME, USER);
        assertEquals("a", ddata.getPermissions().getRead());
        assertEquals("b", ddata.getPermissions().getWrite());
    }
    
    public void testDelete() throws Exception {
        // store doc
        final URI uid = range.next();
        database.store(
            uid, TEST_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
            USER, true);

        // test un/delete
        database.delete(uid, SCHEMA_NAME, USER);
        try {
            database.get(uid, SCHEMA_NAME, USER);
            fail("Should have raised EntityDoesNotExistException"); 
        } catch (EntityDoesNotExistException e) {
            // exception thrown as required, everything ok!
        }
        database.undelete(uid, SCHEMA_NAME, USER);
        assertEquals(database.get(uid, SCHEMA_NAME, USER), TEST_DOC);
    }
    
    public void testQueryId() throws Exception {
        // test queryIDs with realTestDoc
        final String realTestDoc = readFile(REAL_DOC_LOC);
        final URI uid = range.next();
        database.store(
            uid, realTestDoc, testSchemaURI, SCHEMA_NAME, USER, 
            new Permissions(), USER, true);
        // store 2nd doc
        final URI uid2 = range.next();
        database.store(
            uid2, realTestDoc, testSchemaURI, SCHEMA_NAME, USER, 
            new Permissions(), USER, true);

        // satisfiable query:
        URI[] uids = database.queryIDs(
            "//SHUT[.//Value=\"0.248\"]/following-sibling::*/FITSMTD",
            SCHEMA_NAME, null, false, USER);
            assertEquals(2, uids.length);
        if (uids[0].equals(uid)) {
            assertEquals(uids[1], uid2);
        } else {
            assertEquals(uids[1], uid);
        }
        // unsatisfiable query: 
        uids = database.queryIDs(
            "//SHUT[.//Value=\"0.248\"]/preceding-sibling::*/FITSMTD",
            SCHEMA_NAME, null, false, USER);
        assertEquals(0, uids.length);

        // COMP-5433 fix test 
        // satisfiable query:
        uids = database.queryIDs(
            "/" + SCHEMA_NAME, SCHEMA_NAME, null, false, USER);
        assertEquals(2, uids.length);
        if (uids[0].equals(uid)) {
            assertEquals(uids[1], uid2);
        } else {
            assertEquals(uids[1], uid);
        }
        // this should result in a 'basic' query as well
        // satisfiable query:
        uids = database.queryIDs("/*", SCHEMA_NAME, null, false, USER);
        assertEquals(2, uids.length);
        if (uids[0].equals(uid)) {
            assertEquals(uids[1], uid2);
        } else {
            assertEquals(uids[1], uid);
        }
    }
    
    public void testQuery() throws Exception {
        // test query with realTestDoc
        final String realTestDoc = readFile(REAL_DOC_LOC);
        final URI uid = range.next();
        database.store(
            uid, realTestDoc, testSchemaURI, SCHEMA_NAME, USER, 
            new Permissions(), USER, true);
        // store 2nd doc
        final URI uid2 = range.next();
        database.store(
            uid2, realTestDoc, testSchemaURI, SCHEMA_NAME, USER, 
            new Permissions(), USER, true);

        DBCursor curs = 
            database.query("//SHUT", SCHEMA_NAME, null, false, USER);
        try {
            int count = 0;
            int uidCount = 0;
            while (curs.hasNext()) {
                count++;
                final ResultStruct rStruct = curs.next(); 
                if (rStruct.getUri().equals(uid2)) {
                    uidCount++;
                }
            }
            assertEquals(4, count);
            // uid2 has to occur 2 times in 
            assertEquals(2, uidCount);
            try {
                curs.next();
                // no exception was thrown:
                fail("Should have trhown CursorClosedException");
            } catch (CursorClosedException e) {
                // ok
            }
        } finally {
            curs.close();
        }

        curs = database.query("//SHUT", SCHEMA_NAME, null, false, USER);
        try {
            ResultStruct[] res = curs.nextBlock(2);
            // the results have to be one of uid and uid2
            assertTrue(
                res[1].getUri().equals(uid) || res[1].getUri().equals(uid2));
            res = curs.nextBlock(6666);
            // the results have to be one of uid and uid2
            assertTrue(
                res[1].getUri().equals(uid) || res[1].getUri().equals(uid2));
            // beginning with the third element, all have to be null
            assertNull(res[2]);
        } finally {
            curs.close();
        }

        assertEquals(2, 
            database.get(uid, SCHEMA_NAME, "//SHUT", null, USER).length);
    }
    
    public void testNamespaces() throws Exception {
        // test namespaces:
        final URI uid = range.next();
        database.store(
            uid, NS_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(), 
            USER, true);
        final HashMap<String, String> namespaces = 
            database.getReader().getSchemaNamespaces(
                database.getCache().getURI(SCHEMA_NAME));
        
        final URI[] uids = database.queryIDs(
            "//nsy:a//nsz:b", SCHEMA_NAME, namespaces, false, USER);
        
        // 1 doc is returned:
        assertEquals(1, uids.length);
        // compare uid
        assertEquals(uid, uids[0]);
        
        DBCursor curs = 
            database.query("//nsy:a", SCHEMA_NAME, namespaces, false, USER);
        try {
            // 1 result:
            checkResult(curs.nextBlock(10), 1);
        } finally {
            curs.close();
        }
        
        curs = database.query("//nsz:a", SCHEMA_NAME, namespaces, false, USER);
        try {
            // 2 results:
            checkResult(curs.nextBlock(10), 2);
            // unfortunately we can not compare the returned docs
        } finally {
            curs.close();
        }
               
        curs = database.query("//a", SCHEMA_NAME, namespaces, false, USER);
        try {
            // 1 result:
            checkResult(curs.nextBlock(10), 1);
        } finally {
            curs.close();
        }
        
        curs = database.query("//nsy:b", SCHEMA_NAME, namespaces, false, USER);
        try {
            //1 result:
            checkResult(curs.nextBlock(10), 1);
        } finally {
            curs.close();
        }
        
        curs = database.query("//nsz:*", SCHEMA_NAME, namespaces, false, USER);
        try {
            // 3 results:
            checkResult(curs.nextBlock(10), 3);
        } finally {
            curs.close();
        }
    }
        
    public void testElement() throws Exception {
        final URI uid = range.next();
        database.store(
            uid, NS_DOC, testSchemaURI, SCHEMA_NAME, USER, new Permissions(), 
            USER, true);
        final HashMap<String, String> namespaces = 
            database.getReader().getSchemaNamespaces(
                database.getCache().getURI(SCHEMA_NAME));

        // now we test the three modification methods: 
        // addElement, updateElement, deleteElement.
        
        database.addElement(
            uid, SCHEMA_NAME, "//nsy:b", 
            "<nsz:newEl xmlns:nsz=\"locz\">NewCont</nsz:newEl>", USER);
        DBCursor curs = 
            database.query(
                "//nsz:newEl", SCHEMA_NAME, namespaces, false, USER);
        //1 result:
        try {
            checkResult(curs.nextBlock(10), 1);
        } finally {
            curs.close();
        }
        
        database.updateElement(
            uid, SCHEMA_NAME, "//nsz:newEl", 
            "<nsy:newerEl xmlns:nsy=\"locy\">EvenNewer</nsy:newerEl>", USER);
        curs = 
            database.query(
                "//nsy:newerEl", SCHEMA_NAME, namespaces, false, USER);
        //1 result:
        try {
            checkResult(curs.nextBlock(10), 1);
        } finally {
            curs.close();
        }
        
        database.deleteElement(uid, SCHEMA_NAME, "//nsy:newerEl", USER);
        curs = 
            database.query(
                "//nsy:newerEl", SCHEMA_NAME, namespaces, false, USER);
        //0 results:
        try {
            assertNull(curs.nextBlock(10)[0]);
        } finally {
            curs.close();
        }
    }
    
    public void testTransaction() throws Exception {
        final URI obsProjUid = range.next();
        final String obsProjSchemaCont = 
            readFile("../examples/testObsProj.xsd");
        addSchema(
            OBS_NAME,
            obsProjSchemaCont,
            obsProjUid, extractNamespaces(obsProjSchemaCont));

        final URI uid2 = range.next();
        final String obsProjDoc = readFile("../examples/testOProj1.xml");
        database.store(
            uid2, obsProjDoc, obsProjUid, OBS_NAME, USER, new Permissions(), 
            USER, true);

        // test transactions:
        final OracleConnection myConn = database.openConnection();
        database.store(
            uid2, TEST_DOC, OBS_NAME, DOC_OWNER, new Permissions(), USER, 
            false, myConn);

        final URI uid = range.next();
        database.store(
            uid, TEST_DOC, SCHEMA_NAME, DOC_OWNER, new Permissions(), USER, 
            true, myConn);
        
        // query: document should not be there
        try {
            database.get(uid, SCHEMA_NAME, USER);
            fail("Exception not thrown.");
        } catch (Exception e) {
            // expected
        }
        
        // commit
        database.commit(myConn);
        
        // query: document should be there.
        database.get(uid, SCHEMA_NAME, USER);
        assertEquals(
            DOC_OWNER, database.status(uid, SCHEMA_NAME, USER).getOwner());
        assertEquals(TEST_DOC, database.get(uid2, OBS_NAME, USER));
        
        // connection cannot be used anymore:
        try {
            database.store(
                uid, TEST_DOC, SCHEMA_NAME, DOC_OWNER, new Permissions(), USER, 
                false, myConn);
                fail("Exception not thrown.");
        } catch (Exception e) {
            // expected
        }
    }
    
    public void testArc() throws Exception {
        final URI obsProjUid = range.next();
        final URI newSchemaURI = range.next();
        final String obsProjSchemaCont = 
            readFile("../examples/testObsProj.xsd");
        addSchema(
            OBS_NAME,
            obsProjSchemaCont,
            obsProjUid, extractNamespaces(obsProjSchemaCont));

        // test archiveSanityCheck: we destroy consistencies and 
        // try to repair them automatically
        final DatabaseConnectionPool ocpds = database.getConnectionPool();
        final Connection conn = ocpds.getConnection();
        conn.setAutoCommit(true);
        try {
            final Statement stmt = conn.createStatement();
            try {
                final DatabaseWriter dbw = database.getWriter();
                
                // first, everything should be fine:
                assertTrue(dbw.checkNamespaces(false));
                assertTrue(dbw.checkSchemaConsistency(false));
        
                // update schema (and name space associations)
                updateSchema(
                    OBS_NAME, obsProjSchemaCont, obsProjUid, newSchemaURI);

                // break name space associations by changing the schema UID
                String sql = 
                    "UPDATE " + DBConfig.tableName_schemaNamespaces + 
                    " SET " + DBConfig.colName_schemaUid + "='" + obsProjUid +
                    "' WHERE " + DBConfig.colName_schemaUid + "='" + 
                    newSchemaURI + "'";
                stmt.executeUpdate(sql);
        
                // checkNamespaces
                assertFalse(dbw.checkNamespaces(false)); // check fails
                dbw.checkNamespaces(true); // auto repair
                assertTrue(dbw.checkNamespaces(false)); // check passes
                
                // checkSchemaConsistency
                // drop one schema table and remove entry in 
                // xml_schema_entities (belonging to a different schema)
                // both schemas must be completely removed
                sql = 
                    "DELETE FROM " + DBConfig.tableName_schemas + " WHERE " +
                    DBConfig.colName_schemaName + "='" + OBS_NAME + "'";
                stmt.executeUpdate(sql);
                
                assertFalse(dbw.checkSchemaConsistency(false));
                dbw.checkSchemaConsistency(true);
                assertTrue(dbw.checkSchemaConsistency(false));
                
                sql = "DROP TABLE " + DBConfig.schemaTabName(SCHEMA_NAME);
                stmt.executeUpdate(sql);
        
                assertFalse(dbw.checkSchemaConsistency(false));
                dbw.checkSchemaConsistency(true);
                
                assertTrue(dbw.checkNamespaces(false));
                assertTrue(dbw.checkSchemaConsistency(false));
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //ignore
                }
            }
        } finally {
            try {
                ocpds.close(conn);
            } catch (Exception e) {
                //ignore
            }
        }
    }
    

    // the following two methods are a stupid workaround, since much functionality needed when adding a schema
    // resides in the schemaLoader itself. We copy the necessary things here.
    private void addSchema(final String name, final String cont, 
            final URI uid, final List<Namespace> namespaces) throws 
            Exception {
        schemaMan.addSchema(name, cont, "", uid, USER, new Permissions());
        for (Namespace namespace: namespaces) {
            schemaMan.registerNamespace(
                namespace.getPrefix(), new URI(namespace.getURI()));
            schemaMan.assignNamespace(namespace.getPrefix(), uid);
        }
    }

    private void updateSchema(final String name, final String cont, 
            final URI oldUid, final URI newUid) throws Exception {        

        final List<Namespace> namespaces = extractNamespaces(cont);

        schemaMan.updateSchema(
            name, cont, "", oldUid, newUid, USER, new Permissions());        

        @SuppressWarnings("deprecation")
        final String oldSchema = database.get(oldUid, USER);
        for (Namespace oldNamespace: extractNamespaces(oldSchema)) {
            schemaMan.withdrawNamespace(oldNamespace.getPrefix(), oldUid);
        }
        for (Namespace namespace: namespaces) {
            schemaMan.registerNamespace(
                namespace.getPrefix(), new URI(namespace.getURI()));
            schemaMan.assignNamespace(namespace.getPrefix(), newUid);
        }
    }

    protected String readFile(final String fileLocation) throws Exception {
        final FileReader freader = new FileReader(fileLocation);
        final StringBuffer content = new StringBuffer("");
        while (freader.ready()) {
            content.append((char) freader.read());
        }
        return content.toString();
    }

    private List<Namespace> extractNamespaces(final String schemaDoc) throws
            Exception {
        final SAXBuilder builder = new SAXBuilder();
        final LinkedList<Namespace> namespaces = new LinkedList<Namespace>(); 
        for (Object namespaceObject :
                builder.build(
                    new StringReader(schemaDoc)
                ).getRootElement().getAdditionalNamespaces()) {
            namespaces.add((Namespace) namespaceObject);
        }
        return namespaces;
    }
    
    private void checkResult(
            final ResultStruct[] rStruct, final int count) {
        int i = 0;
        for (; i < count; i++) {
            assertNotNull(rStruct[i]);
        }
        assertNull(rStruct[i]);
    }
    
}
