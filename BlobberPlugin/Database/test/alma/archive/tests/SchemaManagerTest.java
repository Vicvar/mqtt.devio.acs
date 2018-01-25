/*
 * 	  Created on 24-Sep-2003
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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.NamespaceDefinedException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.utils.DatabaseSimpleEntity;
import alma.archive.wrappers.Permissions;

/**
 * @author simon
 */
public class SchemaManagerTest extends TestCase
{
	private SchemaManager smanager;
	private InternalIF iface = null;
	private IdentifierManager imanager = null;
	
	public void setUp() throws PermissionDeniedException, UserDoesNotExistException, ModuleCriticalException, ArchiveException
	{
		iface = InternalIFFactory.getInternalIF(Logger.getAnonymousLogger());
		iface.init();
		imanager = InternalIFFactory.getIdentifierManager(Logger.getAnonymousLogger());
		smanager = iface.getSchemaManager("user");
	}
	
	public void tearDown() throws Exception
	{
		smanager.close();
		imanager.close();
		iface.cleanTestArea("user");
		iface.close();
	}
	
	/**
	 * Test the registration and managment of namespaces
	 * @throws URISyntaxException
	 * @throws ArchiveException
	 */
	public void testNamespace() throws URISyntaxException, DatabaseException, MalformedURIException, ModuleCriticalException, ArchiveException
	{
		URI namespace0 = new URI("http://www.alma.org/archive_0");
		URI namespace1 = new URI("http://www.alma.org/archive_1");
		URI namespace2 = new URI("http://www.alma.org/archive_2");
		
		smanager.registerNamespace("ns0",namespace0);
		assertTrue(smanager.namespaceExists(namespace0));
		
		smanager.registerNamespace("ns1",namespace1);
		smanager.registerNamespace("ns2",namespace2);
		
		HashMap namespaces = smanager.namespaces();
		assertTrue(namespaces.containsKey("ns0"));
		assertTrue(namespaces.containsKey("ns1"));
		assertTrue(namespaces.containsKey("ns2"));
		
		//re add a namespace to cause an exception
		boolean thrown = false;
		try 
		{
			smanager.registerNamespace("ns0",namespace1);
		}
		catch (NamespaceDefinedException e)
		{
			thrown = true;
		}
		assertTrue(thrown);
		//check it exists, should return a true
		assertTrue(smanager.namespaceExists(namespace0));
		
		String res = (String)namespaces.get("ns2");
		assertTrue(namespace2.toASCIIString().equalsIgnoreCase(res));
		
		smanager.removeNamespace("ns0");
		smanager.removeNamespace("ns1");
		smanager.removeNamespace("ns2");
		
		//re add a namespace make sure there is no exception
		thrown = false;
		try 
		{
			smanager.registerNamespace("ns0",namespace0);
		}
		catch (NamespaceDefinedException e)
		{
			thrown = true;
		}
		assertFalse(thrown);
		smanager.removeNamespace("ns0");
		
		assertFalse(smanager.namespaceExists(namespace0));
		assertFalse(smanager.namespaceExists(namespace1));
		assertFalse(smanager.namespaceExists(namespace2));
	}
	
	
	public void testSchema() throws URISyntaxException, NamespaceDefinedException, DatabaseException, MalformedURIException, ModuleCriticalException, ArchiveException
	{
		URI namespace0 = new URI("http://www.alma.org/archive_0");
		URI namespace1 = new URI("http://www.alma.org/archive_1");
		URI namespace2 = new URI("http://www.alma.org/archive_2");
		
		URI uid0 = imanager.getIdNamespace();
		URI uid1 = imanager.getIdNamespace();
		
		DatabaseSimpleEntity zero = new DatabaseSimpleEntity("zero",namespace0.toASCIIString());
		DatabaseSimpleEntity one = new DatabaseSimpleEntity("one",namespace0.toASCIIString());
		
		smanager.registerNamespace("ns0",namespace0);	
		smanager.registerNamespace("ns1",namespace1);
		smanager.registerNamespace("ns2",namespace2);
		
		Permissions permissions = new Permissions();
		
		smanager.addSchema("name0",zero.toXmlString(),"",uid0,"owner",permissions);
		
		String res = smanager.getSchemaName(uid0);
		assertTrue(res.equalsIgnoreCase("name0"));
		
		smanager.assignNamespace("ns0",uid0);
		smanager.assignNamespace("ns2",uid0);
		
		HashMap namespaces = smanager.getSchemaNamespaces(uid0);
		assertTrue(namespaces.containsKey("ns0"));
		assertTrue(namespaces.containsKey("ns2"));
		res = (String)namespaces.get("ns2");
		assertTrue(namespace2.toASCIIString().equalsIgnoreCase(res));
		
		//remove one of the namespaces, make sure it is removed from the schema
		smanager.removeNamespace("ns0");
		namespaces = smanager.getSchemaNamespaces(uid0);
		assertFalse(namespaces.containsKey("ns0"));
		
		
		//update the schema
		smanager.updateSchema("name0",one.toXmlString(),"",uid0,uid1,"owner",permissions);
		//todo get the version of most recent
		URI resv = smanager.getSchemaURI("name0");
		assertTrue(resv.equals(uid1));
		assertTrue(smanager.getSchemaVersion(resv) == 2);
		
		resv = smanager.getSchemaURI("name0",2);
		assertTrue(resv.equals(uid1));
		resv = smanager.getSchemaURI("name0",1);
		assertTrue(resv.equals(uid0));
		
		smanager.removeSchema("name0");
		
		smanager.removeNamespace("ns1");
		smanager.removeNamespace("ns2");
	}
}
