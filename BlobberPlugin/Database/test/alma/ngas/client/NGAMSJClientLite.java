/*
 * 	  Created on 21-Apr-2005
 * 
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
 */
package alma.ngas.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author simon
 */
public class NGAMSJClientLite extends JClient
{

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public NGAMSJClientLite(
			List<InetSocketAddress> serverList)
	{
		super(serverList);
	}
	
	

	/* (non-Javadoc)
	 * @see org.eso.dfs.ngamsJClient.NGAMSJClient#retrieve(java.lang.String, java.lang.String)
	 */
	public Status retrieve(String file_id, String fileNameDestination)
	{
		//Do nothing because the file shoud already be there for testing
		File file = new File(fileNameDestination);
		if (file.exists()){
			return new Status(true);
		}
		else{
			return new Status(false);
		}
	}
}
