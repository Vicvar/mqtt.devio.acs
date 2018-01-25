/*
 * 	  Created on 29-Sep-2003
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
import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.archive.database.interfaces.DBCursor;
import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.UndefinedNamespaceException;
import alma.archive.exceptions.general.UnknownSchemaException;
import alma.archive.exceptions.syntax.MalformedQueryException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.syntax.UnderspecifiedQueryException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.utils.DatabaseSimpleEntity;
import alma.archive.wrappers.Permissions;
import alma.archive.wrappers.ResultStruct;

/**
 * @author simon
 */
public class DBCursorTest extends TestCase
{
    private static final String namespaceName = "ns1";
    
    private static final String schemaName = "testSchema";
    private static final String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<xs:schema targetNamespace=\"Alma/testSchema\" " +
                        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
                        "elementFormDefault=\"qualified\" " +
                        "attributeFormDefault=\"unqualified\">" +
                        
                        "<xs:element name=\"simple\"/>" +
                    "</xs:schema>";

    private InternalIF iface;
	private SchemaManager smanager;
	private IdentifierManager imanager;

	private URI schemaLocation;
	
	@Override
	public void setUp() throws URISyntaxException, PermissionDeniedException, UserDoesNotExistException, ModuleCriticalException, ArchiveException
	{
	    final Logger internalLog = Logger.getAnonymousLogger();
	    //internalLog.setLevel(Level.ALL);
	    //internalLog.getParent().getHandlers()[0].setLevel(Level.ALL);
		iface = InternalIFFactory.getInternalIF(internalLog);
		iface.init();
		smanager = iface.getSchemaManager("user");
		imanager = InternalIFFactory.getIdentifierManager(Logger.getAnonymousLogger());
		
		register();
		addTestData();
	}
	
	private void register() throws ModuleCriticalException
	{
		try
		{
			schemaLocation = imanager.getIdNamespace();
			//smanager.registerNamespace(namespaceName,new URI(namespace));
			smanager.addSchema(schemaName,schema,"",schemaLocation,"owner",new Permissions());
			//smanager.assignNamespace(namespaceName,schemaLocation);
		}
		catch (DatabaseException e) {fail(e.getMessage());}
		catch (MalformedURIException e) {fail(e.getMessage());}
		catch (ArchiveException e) {fail(e.getMessage());}
		//catch (URISyntaxException e) {fail(e.getMessage());}
	}
	
	@Override
	public void tearDown() throws Exception
	{
		iface.cleanTestArea("user");
		smanager.removeNamespace(namespaceName);
		
		smanager.close();
		imanager.close();
		iface.close();
	}
	
	private void create(String content, String attribute) throws ModuleCriticalException
	{
		try
		{		
			URI uid = imanager.getIdNamespace();
			
			DatabaseSimpleEntity entity = new DatabaseSimpleEntity(content,uid.toASCIIString(),attribute);
			Permissions permissions = new Permissions();
			
//			vector.add(uid);
			iface.store(uid,entity.toXmlString(),schemaLocation, schemaName,"owner",permissions,"user",true);
		}
		catch (ArchiveException e) {fail();} 
	}
	
	private void addTestData() throws ModuleCriticalException
	{
		create("content0","attribute0");
		create("content1","attribute1");
		create("content2","attribute2");
	}
	
	public void testIteration() throws PermissionDeniedException, DatabaseException, MalformedQueryException, UnderspecifiedQueryException, UnknownSchemaException, UserDoesNotExistException, UndefinedNamespaceException, ModuleCriticalException, ArchiveException
	{
		DBCursor cursor = iface.query("/simple",schemaName,null,false,"user");
		int count = 0;
		while (cursor.hasNext())
		{
			cursor.next();
			count++;
		}
		assertEquals(count, 3);
		cursor.close();
	}
	
	public void testBlock()  throws PermissionDeniedException, DatabaseException, MalformedQueryException, UnderspecifiedQueryException, UnknownSchemaException, UserDoesNotExistException, UndefinedNamespaceException, ModuleCriticalException, ArchiveException
	{
		DBCursor cursor = iface.query("/simple",schemaName,null,false,"user");
		
		ResultStruct[] results = cursor.nextBlock(2);
		assertEquals(results.length,2);
		assertNotNull(results[0]);
		assertNotNull(results[1]);
		
		
		results = cursor.nextBlock(2);
		assertEquals(results.length,2);
		assertNotNull(results[0]);
		assertNull(results[1]);
		cursor.close();
	}
}
