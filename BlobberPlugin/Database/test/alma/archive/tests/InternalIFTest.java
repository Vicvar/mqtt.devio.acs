/*
 * 	  Created on 23-Sep-2003
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
package alma.archive.tests;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.utils.DatabaseSimpleEntity;
import alma.archive.wrappers.ArchiveTimeStamp;
import alma.archive.wrappers.DocumentData;
import alma.archive.wrappers.Permissions;

/**
 * @author simon
 */
public class InternalIFTest extends TestCase
{
    private static final String USER = "user";
    
	private InternalIF iface = null;
	private IdentifierManager imanager = null;
	
	@Override
	public void setUp() throws Exception
	{
	    final Logger iLogger = Logger.getAnonymousLogger();
	    iLogger.setLevel(Level.ALL);
	    iLogger.getParent().getHandlers()[0].setLevel(Level.ALL);
		iface = InternalIFFactory.getInternalIF(iLogger);
		iface.init();
		imanager = InternalIFFactory.getIdentifierManager(Logger.getAnonymousLogger());
	}
	
    @Override
	public void tearDown() throws Exception
	{
		iface.cleanTestArea(USER);
		iface.close();
		imanager.close();
	}
	
	/**
	 * Its difficult to create separate tests
	 * as it is necessary to clean up after each
	 * operation and also check it has been performed
	 */
	public void testStoreGetRemove() throws Exception
	{
		URI uid = imanager.getIdNamespace();
		URI schema = new URI("uid://test");
		
		DatabaseSimpleEntity entity = new DatabaseSimpleEntity("content",uid.toASCIIString(),"attribute");
		Permissions permissions = new Permissions();
		
		iface.store(uid, entity.toXmlString(), schema, "test", "owner", permissions, USER, true);
		
		String xml = iface.get(uid, USER);
		
		entity = new DatabaseSimpleEntity(xml);
		
		assertEquals(entity.getValue(), "content", entity.getValue());
		
		DocumentData dd = iface.status(uid,USER);
		
		ArchiveTimeStamp timestamp = dd.getTimestamp();
		
		iface.update(uid,timestamp,entity.toXmlString(),schema,false,USER);
		
		DocumentData ddfirst = iface.status(uid,USER);			
	}
	
	/*
	public void testGetDate()
	{
		
	}
	*/
	
	public void testGetSection() throws Exception
	{
		URI uid = imanager.getIdNamespace();
		URI schema = new URI("uid://test");
		
		DatabaseSimpleEntity entity = new DatabaseSimpleEntity("content",uid.toASCIIString(),"attribute");
		Permissions permissions = new Permissions();
		
		iface.store(uid,entity.toXmlString(),schema,"test","owner",permissions,USER,true);		

		String[] xml = iface.get(uid,"/simple/@attr",null,USER);
		
		if (xml == null)
		{
			fail();
		}
		else
		{
			assertEquals("attribute",xml[0]);
		}
	}
	
	/**
	 * This combines tests for multiple update and retieval
	 * of versions.
	 */
	/*
	public void testGetVersion() throws URISyntaxException
	{
		URI uid = new URI("uid://X0000000000000000/X00000000");
		URI schema = new URI("uid://X0000000000000001/X00000000");
		
		DatabaseSimpleEntity entity1 = new DatabaseSimpleEntity("content1",uid.toASCIIString(),"attribute1");
		DatabaseSimpleEntity entity2 = new DatabaseSimpleEntity("content2",uid.toASCIIString(),"attribute2");
		DatabaseSimpleEntity entity3 = new DatabaseSimpleEntity("content3",uid.toASCIIString(),"attribute3");
		Permissions permissions = new Permissions();
		
		try
		{
			iface.store(uid,entity1.toXmlString(),schema,"schemaName","owner",permissions,"user");
			
			iface.update(uid,(short)2,entity2.toXmlString(),schema,"user");
			iface.update(uid,(short)3,entity3.toXmlString(),schema,"user");
			
			String xml = iface.get(uid,(short)2,false,"user");
			DatabaseSimpleEntity entity = new DatabaseSimpleEntity(xml);
			assertEquals("content2",entity.getValue());
			
			iface.remove(uid,false,"user");
		}
		catch (ArchiveException e) {fail(e.getMessage());}
	}
	*/
	
	public void testGetStatus()
	{
		
	}
}
