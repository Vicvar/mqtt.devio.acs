/*
 * 	  Created on 21-Oct-2005
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
package alma.archive.database.xmldb;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.xmldb.api.base.XMLDBException;

import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.wrappers.Permissions;

/**
 * @author simon
 */
public class DatabaseStressTest extends TestCase
{
	private String xml = "";

	/**
	 * 
	 */
	public DatabaseStressTest()
	{
		super("DatabaseStressTest");
	}
	
	public void setUp() throws IOException
	{
		xml = readFromFile("../examples/testOProj1.xml"); 
	}
	
	private String readFromFile(String path) throws IOException
	{
		FileInputStream fis = new FileInputStream(path);
		int x= fis.available();
		byte b[]= new byte[x];
		fis.read(b);
		String content = new String(b);
		return content;
	}

	public void testMultipleConnections() 
		throws 
			XMLDBException, 
			DatabaseException, 
			ArchiveException, 
			ModuleCriticalException, URISyntaxException
	{	
		ArrayList identManagers = new ArrayList();
		ArrayList ids = new ArrayList();
		
		URI schema = new URI("uid://test");
		/*
		 * Open up a load of connections to the database and see what happens
		 */
		Logger logger = Logger.getAnonymousLogger();
		
		int numConnections = 500;
		for (int x = 0; x < numConnections; x++)
		{	
			XmldbConnector conn = 
				new XmldbConnector(logger,true);
			
			XmldbDatabase database = new XmldbDatabase(conn,logger);
			
			XmldbIdentifierManager im = new XmldbIdentifierManager(
				database,logger);
			identManagers.add(im);
		}
		
		XmldbConnector conn = 
			new XmldbConnector(logger,true);
		XmldbDatabase database = new XmldbDatabase(conn,logger);
		XmldbInternalIF internal = new XmldbInternalIF(database,logger);
		
		Iterator iter = identManagers.iterator();
		while (iter.hasNext())
		{
			XmldbIdentifierManager im = (XmldbIdentifierManager)iter.next();
			URI id = im.getIdNamespace();
			ids.add(id);
			internal.store(id,xml,schema,"test","owner",new Permissions(),"user",true);
//			System.out.println("Storing: " + id.toASCIIString());
		}
		
		iter = ids.iterator();
		while (iter.hasNext())
		{
			URI id = (URI)iter.next();
			String result = internal.get(id,"user");
//			System.out.println("Fetching: " + id.toASCIIString());
		}
		
		//wont work just causes a -1
		/*
		iter = identManagers.iterator();
		while (iter.hasNext())
		{
			XmldbIdentifierManager im = (XmldbIdentifierManager)iter.next();
			im.close();
		}
		*/
	}
}
