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
package alma.archive.tmcdb.DAO.queries;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

/**
 * DAO for queries on the monitor database (not used by the blobbers which only store monitor data). 
 * <p>
 * Grepping on all Alma java code on the ESO STE on 2013-07-25,
 * it seems that this interface is used only in ARCHIVE/TMCDB/MDGuiApi, 
 * and only the method 'getMonitorData'. Method 'getMonitorDataList' is used in a test.
 * Commenting out the other methods (not yet in the implementation though).
 */
public interface QueryDAO {
	public List getMonitorDataList(Integer monitorPointId, Timestamp startTimestamp , Timestamp stopTimestamp);
	public TimeValuePager getMonitorData(Integer monitorPointId, Timestamp startTimestamp, Timestamp stopTimestamp);
//	public ArrayList<String> getLocations();
//	public String getComponentName(String serialNumber, String configurationName);
//	public String getSerialNumber(String componentName, String configurationName);
//	public String getComponentName(String serialNumber);
//	public String getSerialNumber(String componentName);
//	public ArrayList<String> getAllSerialNumbers();
//	public ArrayList<String> getAllSerialNumbers(String configurationName);
}
