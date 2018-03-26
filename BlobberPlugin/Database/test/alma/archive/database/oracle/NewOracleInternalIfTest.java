package alma.archive.database.oracle;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * New class as the OracleInternalIfImplTest is too complicated
 */
public class NewOracleInternalIfTest {
    private static final Logger LOG = Logger.getAnonymousLogger();
    
    static {
		LOG.setUseParentHandlers(false);
		LOG.setLevel(Level.FINEST);
		LOG.addHandler(new Handler() {
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
    }
    
	@Test
	public void testConnectionPoolCreatedOnce() throws Exception {
		InternalIfImpl impl = InternalIfImpl.instance(LOG);
	}

}
