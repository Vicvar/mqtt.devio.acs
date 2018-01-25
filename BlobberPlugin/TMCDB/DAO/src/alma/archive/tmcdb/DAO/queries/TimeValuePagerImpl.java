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
 * @author Pablo Burgos
 * @version %I%,%G%
 * @since ACS-8_0_0-B
 *
 */

package alma.archive.tmcdb.DAO.queries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import alma.TMCDB.legacy.TimeValue;
import alma.acs.tmcdb.MonitorData;
import alma.archive.tmcdb.persistence.TMCDBPersistence;

public class TimeValuePagerImpl implements TimeValuePager {
	private long monitorPointId;
	private Timestamp startTimestamp;
	private Timestamp stopTimestamp;
	private long maxSampleResults; // todo: currently never read...
	private long maxRowResults;
	private int currentPage;
	private int totalPages;
	private int pageSize;
	private String monitorPointName;
	private List clobResultList;
	private String datatype;
	private TMCDBPersistence myPersistenceLayer;

	public TimeValuePagerImpl() { }

	public DataType getDataType() {
		if (this.datatype.equalsIgnoreCase("integer"))
			return DataType.INTEGER;
		else if (this.datatype.equalsIgnoreCase("float"))
			return DataType.FLOAT;
		else if (this.datatype.equalsIgnoreCase("string"))
			return DataType.STRING;
		else if (this.datatype.equalsIgnoreCase("boolean"))
			return DataType.BOOLEAN;
		else if (this.datatype.equalsIgnoreCase("double"))
			return DataType.DOUBLE;
		else
			return DataType.UNKNOWN;
	}

	public ArrayList<TimeValue> getValues() {
		return parseCurrentClobResultList();
	}

	public int getCurrentPageNumber() {
		return currentPage;
	}

	public void setCurrentPageNumber(int pageNumber) {
		this.currentPage = pageNumber;
	}

	public int getTotalNumberOfPages() {
		return totalPages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getMonitorPointName() {
		return monitorPointName;
	}

	public boolean hasNextPage() {
		if (totalPages - 1 - currentPage == 0)
			return false;
		else
			return true;
	}

	public boolean hasPreviousPage() {
		if (currentPage == 0)
			return false;
		else
			return true;
	}

	public TimeValuePager getNextPage() {
		/**
		 * This method change the state of TimeValuePager Object in terms on the
		 * currentPage
		 */
		if (hasNextPage()) {
			this.currentPage++;
			this.clobResultList = getCurrentResults();
			return this;
		} else {
			return null;
		}
	}

	public TimeValuePager getPreviousPage() {
		/**
		 * This method change the state of TimeValuePager Object in terms on the
		 * currentPage
		 */
		this.currentPage--;
		this.clobResultList = getCurrentResults();
		return this;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public TimeValuePagerImpl(long monitorPointId, Timestamp startTimestamp, Timestamp stopTimestamp, Logger logger) {
		myPersistenceLayer = new TMCDBPersistence(logger);
		this.startTimestamp = startTimestamp;
		this.monitorPointId = monitorPointId;
		this.stopTimestamp = stopTimestamp;
		this.currentPage = 0;
		this.pageSize = 50;

		this.maxSampleResults = retrieveMaxSampleResultsMonitorData();
		this.maxRowResults = retrieveMaxRowResultsMonitorData();

		this.totalPages = (int) Math.floor(this.maxRowResults / this.pageSize + 1);

		this.monitorPointName = retrieveMonitorPointName();

		this.datatype = retrieveDataType();

		this.clobResultList = getCurrentResults();
	}

	private ArrayList<TimeValue> parseCurrentClobResultList() {
		ArrayList<TimeValue> sampleResultList = new ArrayList<TimeValue>();
		/**
		 * For a given currentPage, we will iterate through the clobResultList,
		 * parsing each Clob and adding the sample values pairs (timestamp,
		 * value) to an ArrayList of TimeValue.
		 */

		for (Object clob : clobResultList) {
			String sampleClob = ((MonitorData) clob).getMonitorClob();
			String[] temp = null;
			temp = sampleClob.split("\\|");
			for (int i = 0; i < temp.length; i = i + 2) {
				TimeValue tv = new TimeValue(Long.parseLong(temp[i].trim()),
						temp[i + 1]);
				sampleResultList.add(tv);
			}
		}
		return sampleResultList;
	}

	private long retrieveMaxRowResultsMonitorData() {
		long result = 0;

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query = entityManager
					.createNamedQuery("getMaxRowResultsMonitorData");
			query.setParameter("monitorPointId", this.monitorPointId);
			query.setParameter("startTimestamp", this.startTimestamp);
			query.setParameter("stopTimestamp", this.stopTimestamp);
			try {
				result = ((Long) query.getSingleResult()).intValue();
			} catch (NoResultException e) {
			}
		} finally {
			entityManager.close();
		}
		return result;
	}

	private long retrieveMaxSampleResultsMonitorData() {
		int result = 0;

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query = entityManager
					.createNamedQuery("getMaxSampleResultsMonitorData");
			query.setParameter("monitorPointId", this.monitorPointId);
			query.setParameter("startTimestamp", this.startTimestamp);
			query.setParameter("stopTimestamp", this.stopTimestamp);
			try {
				result = ((Long) query.getSingleResult()).intValue();

			} catch (NoResultException e) {

			}
		} finally {
			entityManager.close();
		}
		return result;
	}

	private String retrieveMonitorPointName() {
		String result = "";

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query = entityManager
					.createNamedQuery("findMonitorPointNameGivenMonitorPointId");
			query.setParameter("monitorPointId", monitorPointId);
			try {
				result = (query.getSingleResult()).toString();

			} catch (NoResultException e) {
				result = "no name";

			}
		} finally {
			entityManager.close();
		}
		return result;
	}

	private String retrieveDataType() {
		String result = "";

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query = entityManager
					.createNamedQuery("findDatatypeGivenMonitorPointId");
			query.setParameter("monitorPointId", monitorPointId);

			try {
				result = (String) query.getSingleResult();

			} catch (NoResultException e) {
				result = "no datatype defined";
			}
		} finally {
			entityManager.close();
		}

		return result;
	}

	private List getCurrentResults() {
		List currentResultList = null;

		EntityManager entityManager = this.myPersistenceLayer
				.getEntityManager();
		try {
			Query query = entityManager
					.createNamedQuery("findMonitorDataByMonitorPointIdAndTimestampRange");
			query.setFirstResult(currentPage * pageSize);
			query.setMaxResults(pageSize);
			query.setParameter("monitorPointId", this.monitorPointId);
			query.setParameter("startTimestamp", this.startTimestamp);
			query.setParameter("stopTimestamp", this.stopTimestamp);
			try {
				currentResultList = query.getResultList();
			} catch (NoResultException e) {
			}
		} finally {
			entityManager.close();
		}
		return currentResultList;
	}

}
