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
 *    Created on Mar 9, 2004
 *
 */

// $Author: hmeuss $
// $Date: 2012/03/29 13:24:23 $
// $Log: CommonInternalIFTest.java,v $
// Revision 1.37  2012/03/29 13:24:23  hmeuss
// removed test for storing UID starting with X00
//
// Revision 1.36  2011/06/14 14:02:58  mbauhofe
// Replaced XML_HISTORY concept with UID_LOOKUP (view).
// Some clean up/core reorganization.
//
// Revision 1.35  2010/09/06 13:26:43  hmeuss
// corrected test on archiveId security
//
// Revision 1.34  2010/01/27 15:44:26  hmeuss
// Improved error text.
//
// Revision 1.33  2009/06/19 08:50:55  hmeuss
// archiveId may now be an arbitrary string.
//
// Revision 1.32  2008/10/21 13:40:43  hmeuss
// Parallel UID retrieval is now done only for Oracle, exist doesn't manage, but that's not a requirement.
//
// Revision 1.31  2008/10/17 15:38:10  hmeuss
// Added test that checks uniqueness of retrieved UIDs in parallel threads
//
// Revision 1.30  2007/07/12 14:46:51  hmeuss
// added method updateXML
//
// Revision 1.29  2007/04/18 12:48:51  hmeuss
// added test for retrieveFragment
//
// Revision 1.28  2007/03/07 11:00:14  hmeuss
// Added test for queryGet (get with 4 args) with attribute query in XPath query
//
// Revision 1.27  2007/01/31 18:12:00  hmeuss
// extended test that Uniqueness of UIDs is tested across ranges and getIdNamespace
//
// Revision 1.26  2006/12/17 21:58:40  hmeuss
// added a sleep since eXist became too fast, what made queryRecent fail
//
// Revision 1.25  2006/10/26 15:45:06  hmeuss
// added test in store, so that UIDs with # part cannot be stored.
//
// Revision 1.24  2006/10/26 15:24:25  hmeuss
// Added check, whether archive ID of incoming documents matches archive ID
//
// Revision 1.23  2006/10/12 09:12:41  hmeuss
// Using a test schema now, that validates with the test documents
//
// Revision 1.22  2006/09/21 14:41:30  hsommer
// curs.close()
//
// Revision 1.21  2006/09/21 08:52:38  hmeuss
// Implemented queryContent method, that correctly queries XPath expressions returning a non-node type (eg. ending with an attribute step)
//
// Revision 1.20  2006/09/15 14:16:53  hsommer
// setUp no longer ignores exceptions.
// added close() on all cursors.
// fixed exceptions because of new Range class.
//
// Revision 1.19  2006/04/28 08:39:36  hmeuss
// Changed interface of queryRecent and implemented it for exist
//
// Revision 1.18  2006/04/26 15:52:50  hmeuss
// added test for queryDirty
//
// Revision 1.17  2006/04/25 09:34:12  hmeuss
// changed interface for timestamps of queryRecent
//
// Revision 1.16  2006/04/25 08:59:48  hmeuss
// added test for queryRecent
//
// Revision 1.15  2006/04/21 14:33:01  hmeuss
// New version, with test for new UIDLibrary
//
// Revision 1.14  2005/09/12 11:55:23  hmeuss
// Adapted tests to new internal IF
//
// Revision 1.13  2005/07/26 09:15:30  hmeuss
// moved the call to IdentifierManager and schemaManager behind clean()
//
// Revision 1.12  2005/07/19 13:49:42  hmeuss
// changed again to previous version: first call init(), then cleanTestArea()
//
// Revision 1.11  2005/07/18 10:08:02  sfarrow
// Moved to exist and changed some of the configuration stuff.
//
// Revision 1.10  2005/07/12 16:07:54  sfarrow
// had to reorder the setup as the clean test area after fetching an id was causing problems.
//
// Revision 1.9  2005/01/13 10:45:50  hmeuss
// - Added call to init() in all tests.
//
// Revision 1.8  2004/10/06 14:18:13  hmeuss
// Catched exception from cleanTestArea
//
// Revision 1.7  2004/09/17 08:50:38  hmeuss
// Added ModuleCriticalException that will be propagated upwards through all classes
//
// Revision 1.6  2004/08/19 14:58:08  hmeuss
// Changed behaviour of get in Oracle implementation
//
// Revision 1.5  2004/07/12 14:02:25  hmeuss
// Changed xindice location (using ACS.tmp)
//
// Revision 1.4  2004/05/07 12:51:55  hmeuss
// Works now for all implementations
//
// Revision 1.3  2004/04/15 14:00:00  sfarrow
// Some small changes
//
// Revision 1.2  2004/04/14 11:56:43  hmeuss
// added id manager.close()
//
// Revision 1.1  2004/04/14 11:07:45  hmeuss
// Designed as a test for all implementations of Internal IF
// 
package alma.archive.database.interfaces;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.acs.container.archive.Range;
import alma.archive.database.helpers.DBConfiguration;
import alma.archive.exceptions.access.EntityDirtyException;
import alma.archive.exceptions.general.EntityDoesNotExistException;
import alma.archive.exceptions.general.HistoryInconsistencyException;
import alma.archive.exceptions.general.UnknownSchemaException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.wrappers.ArchiveTimeStamp;
import alma.archive.wrappers.Permissions;
import alma.archive.wrappers.ResultStruct;

/**
 * @author hmeuss
 * 
 */
// TODO try the ID stuff with XPath queries, and then also use
// retrieveFragment in the external IF.
public class CommonInternalIFTest extends TestCase {
    // for testing UID retrieval in parallel threads:
    private final static int NUM_THREADS = 10;

    private final static int NUM_ITERATIONS = 1000;

    private static class GetUidThread extends Thread {
        private final List<Long> retrievedUIDs;
        private final IdentifierManager idMgr;
        
        
        public GetUidThread(IdentifierManager idMgr) {
            super();
            retrievedUIDs = new LinkedList<Long>();
            this.idMgr = idMgr;
        }

        @Override
        public void run() {
            for (int i = 0; i < NUM_ITERATIONS; i++) {
                try {
                    retrievedUIDs.add(idMgr.getRangeId());
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

    }

    private static final String SCHEMA_NAME = "testSchema";

    private static final String SCHEMA = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
        "elementFormDefault=\"qualified\" " +
        "attributeFormDefault=\"unqualified\"> " +
        "  <xs:element name=\"a\">" +
        "  <xs:complexType>" +
        "    <xs:sequence>" +
        "    <attribute name=\"id\" minOccurs=\"0\" type=\"ID\" />" +
        "    <xs:any minOccurs=\"0\" processContents=\"skip\"/>" +
        "    </xs:sequence>" +
        "  </xs:complexType>" +
        "  </xs:element>" +
        "</xs:schema>";

    private static final String D_CONTENT = "some text";
    
    private static final String C_ATTR_C = "cc";
    
    private static final String C_ATTR_A = "aa";
    
    /*
     * simpleDoc:
     * 
     * a | b / | \ c c d / | | /\ / cAtt | d e aAtt | \ \ | "cc" aAtt "text"
     * "aa" | "aa"
     */
    private static final String SIMPLE_DOC = 
        "<a id=\"1\">" +
        " <b>" +
        "  <c cAtt=\"" + C_ATTR_C + "\" aAtt=\"" + C_ATTR_A + "\"/>" +
        "  <c aAtt=\"" + C_ATTR_A + "\"></c>" +
        "  <d>" +
        "   <d>" + D_CONTENT + "</d>" +
        "   <e/>" +
        "  </d>" +
        " </b>" +
        "</a>";

    /*
     * nsDoc:
     * 
     * A:a | A:b / | \ C:c A:c C:d /\ d A:e
     * 
     */
    private static final String NS_DOC = 
        "<A:a xmlns:A=\"nsA\">" +
        " <A:b xmlns:C=\"nsC\">" +
        "  <C:c/>" +
        "  <A:c/>" +
        "  <C:d>" +
        "   <d/>" +
        "   <A:e/>" +
        "  </C:d>" +
        " </A:b>" +
        "</A:a>";

    private static final String NEW_DOC = "<foo/>";

    private static final String USER = "testUser";

    private final static Logger LOGGER = Logger.getLogger("ARCHIVE");

    static {
        LOGGER.setLevel(Level.WARNING);
    }
    
	private InternalIF internal;

	private IdentifierManager idManag;

	private SchemaManager schemaMan;

	private Range range;

    private URI testSchemaURI;

    @Override
	public void setUp() throws Exception {
		internal = InternalIFFactory.getInternalIF(LOGGER);
		internal.init();

		internal.cleanTestArea(USER);

		idManag = InternalIFFactory.getIdentifierManager(LOGGER);
		schemaMan = internal.getSchemaManager(USER);

		range = new Range(idManag.getNewRange());

		// check whether SCHEMA is in DB
		// if not: add it
		try {
		    testSchemaURI = schemaMan.getSchemaURI(SCHEMA_NAME);
		} catch (UnknownSchemaException e) {
    		LOGGER.info("Adding schema " + SCHEMA_NAME);
    		testSchemaURI = range.next();
    		schemaMan.addSchema(SCHEMA_NAME, SCHEMA, "indexConfig",
    				testSchemaURI, USER, new Permissions());
		}
	}

    @Override
	public void tearDown() throws Exception {
		idManag.close();
		schemaMan.close();
		internal.close();
	}

	@SuppressWarnings("deprecation")
    public void testStoreGet() throws Exception {
        final URI uid = range.next();
        // store simple doc
        internal.store(uid, SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, USER,
            new Permissions(), USER, true);
        
        // get the document again
        assertEquals(
            2, internal.get(uid, "//*[@aAtt=\"aa\"]", null, USER).length);
        assertEquals(
            2, 
            internal.get(
                uid, SCHEMA_NAME, "//*[@aAtt=\"aa\"]", null, USER
            ).length);

        // test XPath id functionality, the first line does not work
        assertEquals(
            1, internal.get(uid, "//*[@id=\"1\"]", null, USER).length);
	}
	
	public void testQuery() throws Exception {
        // store simple doc
        internal.store(range.next(), SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, 
            USER, new Permissions(), USER, true);

        // 2 results
        DBCursor curs = 
            internal.query("//b//c", SCHEMA_NAME, null, false, USER);
        try {
            assertNotNull(curs.nextBlock(2)[1]);
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }

        // 1 result
        curs = 
            internal.query(
                "//*[@cAtt=\"cc\"]", SCHEMA_NAME, null, false, USER);
        try {
            assertNotNull(curs.nextBlock(1)[0]);
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }

        // 2 results
        curs = internal.query("/a//d", SCHEMA_NAME, null, false, USER);
        try {
            assertNotNull(curs.nextBlock(2)[1]);
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }
	}
	
	// no longer used functionality is being tested.
	// I've disabled this - will be deleted later.
	public void disabledTestQueryContent() throws Exception {
        // store simple doc
        internal.store(range.next(), SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, 
            USER, new Permissions(), USER, true);

        // 3 results
	    DBCursor curs = 
	        internal.queryContent("//c/@*", SCHEMA_NAME, null, false, USER);
	    try {
	        for (ResultStruct rStruct: curs.nextBlock(3)) {
	            assertTrue(
	                C_ATTR_C.equals(rStruct.getXml()) || 
	                C_ATTR_A.equals(rStruct.getXml()));
	        }
            assertFalse(curs.hasNext());
	    } finally {
            curs.close();
	    }

        // 1 result
        curs = 
            internal.queryContent(
                "//d/d/text()", SCHEMA_NAME, null, false, USER);
        try {
            assertTrue(curs.hasNext());
            assertEquals(D_CONTENT, curs.next().getXml());
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }
	}
	
	public void testQueryRecent() throws Exception {
        final URI uid = range.next();
        // store simple doc
        internal.store(uid, SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, USER,
            new Permissions(), USER, true);

        // retrieve status, remember time stamp
        @SuppressWarnings("deprecation")
        final ArchiveTimeStamp timestamp = 
            internal.status(uid, USER).getTimestamp();
        // should return 0 docs!
        assertEquals(0, 
            internal.queryRecent(timestamp, SCHEMA_NAME, USER).length);

        final long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + 1000) {
            // do nothing, we have to have a different timestamp then we had
            // before
        }

        // store new version
        internal.update(uid, timestamp, NEW_DOC, testSchemaURI, false,
                USER);
        // should return 1 doc!
        assertEquals(1, 
            internal.queryRecent(timestamp, SCHEMA_NAME, USER).length);
        // query new doc
        assertEquals(0, 
            internal.queryIDs("//a", SCHEMA_NAME, null, false, USER).length);

        // update doc with wrong timestamp
        try {
            internal.update(uid, timestamp, SIMPLE_DOC, testSchemaURI, false,
                    USER);
            fail("Expected HistoryInconsistencyException");
        } catch (HistoryInconsistencyException e) {
            // expected
        }

        // update doc with wrong timestamp, force parameter set:
        internal.update(uid, timestamp, NS_DOC, testSchemaURI, true, USER);
}
	
	@SuppressWarnings("deprecation")
    public void testDelete() throws Exception {
        final URI uid = range.next();
        // store simple doc
        internal.store(uid, SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, USER,
            new Permissions(), USER, true);

        // delete doc
        internal.delete(uid, USER);
        try {
            internal.get(uid, SCHEMA_NAME, USER);
            fail("Expected EntityDoesNotExistException");
        } catch (EntityDoesNotExistException e) {
            // expected
        }

        // undelete doc
        internal.undelete(uid, USER);
        internal.get(uid, SCHEMA_NAME, USER);
	}
	
	@SuppressWarnings("deprecation")
    public void neverworkedtestDirty() throws Exception {
        final URI uid = range.next();
        // store simple doc
        internal.store(uid, SIMPLE_DOC, testSchemaURI, SCHEMA_NAME, USER,
            new Permissions(), USER, true);

        // check behaviour of dirty:
        internal.dirty(uid, USER);
        try {
            internal.get(uid, SCHEMA_NAME, USER);
            fail("expected EntityDirtyException");
        } catch (EntityDirtyException e) {
            // expected
        }

        // test queryDirty:
        final DBCursor curs = 
            internal.query("/*", SCHEMA_NAME, null, true, USER);
        try {
            assertTrue(curs.hasNext());
        } finally {
            curs.close();
        }

        // check query on dirty doc
        // dirty param false
        assertEquals(0, 
            internal.queryIDs("/*", SCHEMA_NAME, null, false, USER).length);
        // dirty param true
        assertTrue(
            internal.queryIDs("/*", SCHEMA_NAME, null, true, USER).length > 0);
        
        internal.clean(uid, USER);
	}
	
	public void testNamespaces() throws Exception {
        // store simple doc
        internal.store(range.next(), NS_DOC, testSchemaURI, SCHEMA_NAME, USER, 
            new Permissions(), USER, true);

        // namespace query things:
        final Map<String, String> namespaces = namespaces();
        
        // 1 result
        DBCursor curs = 
            internal.query("//d", SCHEMA_NAME, namespaces, false, USER);
        try {
            assertNotNull(curs.nextBlock(1)[0]);
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }

        // 1 result
        curs = internal.query("//C:c", SCHEMA_NAME, namespaces, false, USER);
        try {
            assertNotNull(curs.nextBlock(1)[0]);
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }

        // 0 results
        curs = internal.query("//a", SCHEMA_NAME, namespaces, false, USER);
        try {
            assertFalse(curs.hasNext());
        } finally {
            curs.close();
        }
	}

	public void testUpdate() throws Exception {
        // test updateXML
        final URI uid = range.next();
        // store simple doc
        internal.store(uid, NS_DOC, testSchemaURI, SCHEMA_NAME, USER,
            new Permissions(), USER, true);

        // first store SIMPLE_DOC again
        final ArchiveTimeStamp timestamp = 
            internal.status(uid, SCHEMA_NAME, USER).getTimestamp();
        internal.update(
            uid, timestamp, SIMPLE_DOC, testSchemaURI, true, USER);
        // then update it:
        internal.updateXML(uid, SCHEMA_NAME, "<BLA/>");
        // we now have a second child in the root:
        // namespace query things:
        final DBCursor curs = 
            internal.query("/a/*[2]", SCHEMA_NAME, namespaces(), true, USER);
        assertTrue(curs.hasNext());
        assertEquals("<BLA/>", curs.next().getXml().trim());
        curs.close();

        // try to store UID which contains # part:
        try {
            internal.store(new URI("uid://X00/X1/X1#666"), SIMPLE_DOC,
                    testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
                    USER, true);
            fail("Storage of uid://X00/X1/X1#666 accepted. ");
        } catch (MalformedURIException e) {
            // expected
        }

        /*
         * test removed, does not work on Oracle
        // try to store doc whose UID does not match ArchiveId:
        final IdentifierManager imanager = 
            InternalIFFactory.getIdentifierManager(LOGGER);

        // store Archive ID if we have to change it:
        final String archiveId = imanager.getArchiveId();

        if (archiveId.startsWith("X0")) {
            imanager.setArchiveId("TEST");
            LOGGER.info("CHANGED ARCHIVE ID, MAKE SURE IT IS RESET!");
        }
        try {
            internal.store(new URI("uid://X00/X1/X1"), SIMPLE_DOC,
                    testSchemaURI, SCHEMA_NAME, USER, new Permissions(),
                    USER, true);
            fail("X00 as Archive part of UID accepted for " +
            		"storing to non X00 archive ID.");
        } catch (ArchiveException e) {
            assertTrue(
                e.getMessage().startsWith("Cannot store document with UID"));
        } finally {
            if (archiveId.startsWith("X0")) {
                // reset archive id:
                imanager.setArchiveId("X00");
                LOGGER.info("ARCHIVE ID RESET!");
            }
        }
        */
	}
	
	public void testUID() throws Exception {
        // test UIDs, whether they are really unique

        // fetch 2 ranges, get their UIDs, fetch directly two UIDs, compare all
        // of them to not get any doubles
        URI[] uri = new URI[4];

        uri[0] = new Range(idManag.getNewRange()).rangeId();
        uri[1] = new Range(idManag.getNewRange()).rangeId();

        uri[2] = idManag.getIdNamespace();
        uri[3] = idManag.getIdNamespace();

        assertFalse("UID given out twice", uri[0].equals(uri[1]));
        assertFalse("UID given out twice", uri[0].equals(uri[2]));
        assertFalse("UID given out twice", uri[0].equals(uri[3]));
        assertFalse("UID given out twice", uri[1].equals(uri[2]));
        assertFalse("UID given out twice", uri[1].equals(uri[3]));
        assertFalse("UID given out twice", uri[2].equals(uri[3]));

        // make some tougher test on UID uniqueness, but only in case of oracle:
        if (DBConfiguration.instance(LOGGER).dbBackend.equals("oracle")) {
            // create threads
            final GetUidThread[] uidThreads = new GetUidThread[NUM_THREADS];
            for (int i = 0; i < NUM_THREADS; i++) {
                uidThreads[i] = new GetUidThread(idManag);
                uidThreads[i].start();
            }
            Thread.yield();
            // are we really finished?
            int sum = 0;
            while (sum != NUM_THREADS) {
                Thread.sleep(100);
                sum = 0;
                for (GetUidThread thread: uidThreads) {
                    sum += thread.isAlive() ? 0 : 1;
                }
            }

            // now test whether UID are really disjoint (very simple check):
            boolean disjoint = true;
            List<Long> allUids = new LinkedList<Long>();
            for (GetUidThread thread: uidThreads) {
                final List<Long> threadUids = thread.retrievedUIDs;
                disjoint = 
                    disjoint && Collections.disjoint(allUids, threadUids);
                allUids.addAll(threadUids);
            }
            assertTrue(disjoint);
        }
	}
	
	
	private static Map<String, String> namespaces() {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("A", "nsA");
        namespaces.put("B", "nsB");
        namespaces.put("C", "nsC");
        return namespaces;
    }
    
}
