package alma.acs.monitoring.blobber.mpexpert;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import alma.ACSErrTypeCommon.wrappers.AcsJNoResourcesEx;
import alma.acs.monitoring.blobber.MonitorPointExpert;

/**
 * This class contains code that was extracted from BlobberWorker.
 * See http://ictjira.alma.cl/browse/ICT-497
 * 
 * @author hsommer
 */
public class MonitorPointExpertImpl implements MonitorPointExpert
{
	protected final Logger logger;
	private final ACSMonitorPointNameResolver mpResolver;
	private boolean readData = false;

	public MonitorPointExpertImpl(Logger logger) {
		this.logger = logger;
		mpResolver = new ACSMonitorPointNameResolver();
		try {
			mpResolver.loadMonitorPointFromXML();
			readData = true;
		} catch (Exception ex) {
			this.logger.log(Level.WARNING, "Failed to load monitor point info.", ex);
		}
	}
	
	@Override
	public boolean isMultivaluedMonitorPoint(String propertyName) throws AcsJNoResourcesEx {
		if (readData) {
			Hashtable monitorPointCache = mpResolver.getMonitorPointCache();
			return !monitorPointCache.containsKey(propertyName + "_1");
		} 
		else {
			throw new AcsJNoResourcesEx();
		}
	}
}
