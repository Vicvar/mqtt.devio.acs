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
package alma.acs.monitoring.blobber;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import alma.ACSErrTypeCommon.wrappers.AcsJCouldntCreateObjectEx;
import alma.acs.container.ContainerServices;
import alma.acs.monitoring.DAO.MonitorDAO;
import alma.acs.monitoring.blobber.mpexpert.MonitorPointExpertImpl;
import alma.archive.tmcdb.MQDAO.MQDAOImpl;
import alma.archive.tmcdb.INFLUXDAO.INFLUXDAOImpl;
import alma.archive.tmcdb.persistence.TMCDBConfig;

/**
 * This code will get used by the blobber component 
 * (see {@link alma.acs.monitoring.blobber.BlobberImpl}
 * to access the TMCDB configuration file data and to create the 
 * <code>MonitorDAO</code> and <code>MonitorPointExpert</code>.
 * @author hsommer
 * @since ACS 9.1
 */
public class BlobberPluginAlmaImpl extends BlobberPlugin 
{
	private List<MonitorDAO> myDaoList;
	private MonitorPointExpert mpExpert;
	private BlobberWatchDogAlmaImpl myWatchDog;
	private Thread myWatchDogThread;

	public BlobberPluginAlmaImpl(ContainerServices containerServices) {
		super(containerServices);
	}

	@Override
	public int getCollectorIntervalSec() {
		return TMCDBConfig.getInstance(logger).getCollectorInterval();
	}

	@Override
	public boolean isProfilingEnabled() {
		return TMCDBConfig.getInstance(logger).isProfilingEnabled();
	}
	
	/** 
	 * Create the watchdog and DAO objects.
	 */
	@Override
	public void init() throws AcsJCouldntCreateObjectEx {
		myWatchDog = new BlobberWatchDogAlmaImpl(containerServices);
		createMonitorDAOs();
		mpExpert = new MonitorPointExpertImpl(logger);
		
		myWatchDog.init();
		if (myWatchDogThread == null) {
			myWatchDogThread = this.containerServices.getThreadFactory().newThread(myWatchDog);
			myWatchDogThread.start();
		}
	}

	/** 
	 * Stop and clean up watchdog and DAO objects.
	 */
	@Override
	public void cleanUp() {
		myWatchDog.cleanUp();
		myWatchDogThread = null;

		for (MonitorDAO monitorDAO : myDaoList) {
			try {
				monitorDAO.close();
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Failure closing DAO of type " + monitorDAO.getClass().getSimpleName(), ex);
			}
		}
	}

	@Override
	public List<MonitorDAO> getMonitorDAOs() {
		return myDaoList;
	}

	@Override
	public BlobberWatchDog getBlobberWatchDog() {
		return myWatchDog;
	}


	private void createMonitorDAOs() throws AcsJCouldntCreateObjectEx {
		myDaoList = new ArrayList<MonitorDAO>();
		try {
			long t0 = System.currentTimeMillis();
// HSO: Disabled storing data in Oracle as discussed at http://ictjira.alma.cl/browse/ICT-462?focusedCommentId=44849 
//			MonitorDAO dbDAO = new MonitorDAOImpl(containerServices, myWatchDog);
//			myDaoList.add(dbDAO);
			System.out.println("Se va a llamar a INFLUX");
			MonitorDAO mqDAO = new INFLUXDAOImpl(containerServices, myWatchDog);
			myDaoList.add(mqDAO);
			//MonitorDAO mqDAO = new MQDAOImpl(containerServices, myWatchDog);
			//myDaoList.add(mqDAO);

			if ( logger.isLoggable(Level.FINER) )
				logger.finer("Instantiated blobber plugin (" + mqDAO.getClass().getName()
					+ ") in " + (System.currentTimeMillis() - t0) + " ms.");
		} catch (Throwable thr) {
			throw new AcsJCouldntCreateObjectEx(thr);
		}
	}

	@Override
	public MonitorPointExpert getMonitorPointExpert() {
		return mpExpert;
	}
}
