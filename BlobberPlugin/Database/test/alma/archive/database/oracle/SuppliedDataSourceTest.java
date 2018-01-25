package alma.archive.database.oracle;

import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;

import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.exceptions.general.EntityDoesNotExistException;

/**
 * Infrastructure guys want to, sensibly, supply their own DataSource to the Archive classes so that there aren't two
 * different connection pools in play.
 * This will only be done from the SubmissionServer for the moment.
 * 
 * @author almadev
 */
public class SuppliedDataSourceTest extends TestCase {
	private final static Logger logger = Logger.getLogger(SuppliedDataSourceTest.class.getSimpleName());
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testIdentifierManager() throws Exception {
		BasicDataSource dataSource = getDataSource();
		IdentifierManager identifierManager = InternalIFFactory.getIdentifierManager(dataSource, logger);
		String archiveId = identifierManager.getArchiveId();
		assertNotNull(archiveId);
	}

	/**
	 * ICT-607
	 * make sure that no NULL values are returned - otherwise the CORBA return call fails
	 * @throws Exception
	 */
	public void testOtSubmissionServerNoNulls() throws Exception {
		TestSupportDao supportDao = new TestSupportDao();
		DataSource dataSource = getDataSource();
		supportDao.setDataSource(dataSource);
		supportDao.createTestData("alma/archive/database/oracle/testdata/testOtSubmissionServerNoNulls.sql");

		InternalIfImpl internalIf = new InternalIfImpl(dataSource);
		ArrayList<String[]> results = internalIf.querySubmissions(InternalIF.OTfield_PI, "wdent", false, true, "wdent");
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		for (String nextField: results.get(0)) {
			Assert.assertNotNull(nextField);
		}
	}
	
	/**
	 * ICT-607
	 * Don't display Canceled projects (spelling Cancelled with one 'l' instead of two).
	 * @throws Exception
	 */
	public void testOtSubmissionsNoCancelled() throws Exception {
		TestSupportDao supportDao = new TestSupportDao();
		DataSource dataSource = getDataSource();
		supportDao.setDataSource(dataSource);
		supportDao.createTestData("alma/archive/database/oracle/testdata/testOtSubmissionsNoCancelled.sql");

		InternalIfImpl internalIf = new InternalIfImpl(dataSource);
		ArrayList<String[]> results = internalIf.querySubmissions(InternalIF.OTfield_PI, "wdent", false, true, "wdent");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testInternalIfImpl() throws Exception {
		DataSource dataSource = getDataSource();
		InternalIfImpl internalIf = new InternalIfImpl(dataSource);
		DatabaseConnectionPool connectionPool = internalIf.getConnectionPool();
		assertNotNull(connectionPool);
		// this used to return a connection from the source pool if no log pool was available.
		assertNull(connectionPool.getLogConnection());
		try {
			internalIf.getReader().get(new URI("uid://A002/doesnt/exist"), "ASDM", false, "noone");		
			fail("found a non-existant entity");
		}
		catch (EntityDoesNotExistException e) {
			// correct response
		}
	}
	
	/**
	 * @return
	 */
	private BasicDataSource getDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:oracle:thin:@ora02.hq.eso.org:1521/ALMA.ARC.EU");
		dataSource.setUsername("alma_ut");
		dataSource.setPassword("alma_ut$dba");
		return dataSource;
	}
}
