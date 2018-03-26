/**
 * 
 */
package alma.archive.database.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO - use spring to make the DB access more robust.
 * 
 * @author am
 *
 */
public class TestSupportDao {
	private static final Log LOG = LogFactory.getLog(TestSupportDao.class);

	private DataSource dataSource;
	
	/**
	 * 
	 * @param dataSource
	 * @throws SQLException 
	 */
	public void setDataSource(DataSource dataSource) throws SQLException {
		String username = ((BasicDataSource)dataSource).getUsername();
		if (username != null && !username.toLowerCase().endsWith("_ut")) throw new RuntimeException("only execute against a UT DB - this will empty tables");
		this.dataSource = dataSource;
		this.scrubContents();
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public void scrubContents() throws SQLException {
		if (LOG.isDebugEnabled()) LOG.debug("-> scrubContents");

		Connection connection = null;
		Statement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			
			statement.execute("delete from obs_project_status");
			statement.execute("delete from sched_block_status");
			statement.execute("delete from obs_unit_set_status");
			statement.execute("delete from xml_obsproject_entities");
			statement.execute("delete from bmmv_obsproposal");
			statement.execute("delete from bmmv_obsproposal_authors");
		}
		finally {
			if (statement != null) statement.close();
			if (connection != null) connection.close();
		}

		if (LOG.isDebugEnabled()) LOG.debug("<- scrubContents");
	}

	/**
	 * 
	 * @param string
	 * @throws IOException 
	 */
	public void createTestData(String filename) throws Exception {
		if (LOG.isDebugEnabled()) LOG.debug("-> createTestData(" + filename + ")");

		Connection connection = null;
		Statement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			
			URL classpathUrl = this.getClass().getClassLoader().getResource(filename);
			final File f = new File(classpathUrl.toURI());
			if (!f.exists()) {
				LOG.debug("no database population file found");
			}
			else {
				final BufferedReader br = new BufferedReader(new FileReader(f));
				String nextLine = br.readLine();
				StringBuilder buffer = new StringBuilder();
				while (nextLine != null) {
					System.out.println(nextLine);
					// LOG.debug("next line: " + nextLine);
					nextLine = nextLine.trim();
					buffer.append(" ").append(nextLine);
					if (nextLine.endsWith(";")) {
						String sql = buffer.toString();
						sql = sql.replace(";", "");
						
						statement.execute(sql);
						
						buffer = new StringBuilder();
					}
					else if (nextLine.startsWith("--")) {
						// throw comment lines away
						buffer = new StringBuilder();
					}
					
					nextLine = br.readLine();
				}
				br.close();			
			}
		}
		finally {
			if (statement != null) statement.close();
			if (connection != null) connection.close();
		}

		if (LOG.isDebugEnabled()) LOG.debug("<- createTestData");
	}

}
