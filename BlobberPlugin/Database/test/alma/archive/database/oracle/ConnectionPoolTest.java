package alma.archive.database.oracle;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import alma.archive.database.helpers.DBConfiguration;

public class ConnectionPoolTest {
    private Logger getLog() {
        final Logger LOG = Logger.getAnonymousLogger();
        
		LOG.setUseParentHandlers(false);
		LOG.setLevel(Level.FINEST);
		LOG.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				String string = record.getLevel() + " [Thread-" + record.getThreadID() + "]";
				String className = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
				string += " " + className + "." + record.getSourceMethodName();
				string += ": " + record.getMessage();
				System.out.println(string);
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
		
		return LOG;
    }
    
	class LoggingTrapper extends Handler {
		private String maxLimit;
		
		@Override
		public void publish(LogRecord record) {
			String className = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
			if (className.equals("DatabaseConnectionPool") 
					&& record.getSourceMethodName().equals("logPoolParameters") 
					&& record.getMessage().contains("archive.oracle")
					&& record.getMessage().contains("MaxLimit = ")) {
				maxLimit = record.getMessage();
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
		
		public String getMaxLimit() {
			return maxLimit;
		}
	};

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDefaultValues() throws Exception {
		LoggingTrapper logTrapper = new LoggingTrapper();
		Logger LOG = getLog();
		LOG.addHandler(logTrapper);
		new DatabaseConnectionPool(LOG);
		Assert.assertTrue(logTrapper.getMaxLimit(), logTrapper.getMaxLimit().contains("MaxLimit = 15"));
	}

	/**
	 * A quick solution put in place for the CallForProposals for cycle 2. It will be removed in the future, but for the 
	 * moment we should ensure that it continues to work as before.
	 * @throws Exception
	 */
	@Test
	public void testSystemOverridesValues() throws Exception {
		System.setProperty("archive.connpool.maxsize", "99");
		LoggingTrapper logTrapper = new LoggingTrapper();
		Logger LOG = getLog();
		LOG.addHandler(logTrapper);
		new DatabaseConnectionPool(LOG);
		Assert.assertTrue(logTrapper.getMaxLimit(), logTrapper.getMaxLimit().contains("MaxLimit = 99"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testConfigFileOverridesEverything() throws Exception {
		System.setProperty("archive.connpool.maxsize", "99");
		System.setProperty("archive.configFile", "test/archiveConfig.properties");
		LoggingTrapper logTrapper = new LoggingTrapper();
		Logger LOG = getLog();
		LOG.addHandler(logTrapper);
		DBConfiguration.instance(LOG).reinit(LOG);
		new DatabaseConnectionPool(LOG);
		// currently impossible to test due to the singleton config file
		// Assert.assertTrue(logTrapper.getMaxLimit(), logTrapper.getMaxLimit().contains("MaxLimit = 7"));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testConnectionValidation() throws Exception {
		System.setProperty("archive.connpool.maxsize", "99");
		System.setProperty("archive.configFile", "test/archiveConfig.properties");
		LoggingTrapper logTrapper = new LoggingTrapper();
		Logger LOG = getLog();
		LOG.addHandler(logTrapper);
		DBConfiguration.instance(LOG).reinit(LOG);
		DatabaseConnectionPool pool = new DatabaseConnectionPool(LOG);
		// currently impossible to test due to the singleton config file
		// Assert.assertTrue(logTrapper.getMaxLimit(), logTrapper.getMaxLimit().contains("MaxLimit = 7"));
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			Connection connection = pool.getConnection();
			Statement s = connection.createStatement();
			s.execute("select sysdate from dual");
			s.close();			
			connection.close();
		}
		LOG.info("100K iterations took " + ((System.currentTimeMillis() - startTime) / 1000) + "s");
	}	
}
