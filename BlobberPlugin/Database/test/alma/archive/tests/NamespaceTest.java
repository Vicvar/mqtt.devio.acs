/*
 * 	  Created on 26-Nov-2003
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

import org.jdom.Namespace;

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
import junit.framework.TestCase;

/**
 * @author simon
 */
public class NamespaceTest extends TestCase
{
	
	InternalIF iface = null;
	SchemaManager smanager = null;
	IdentifierManager imanager = null;
	
	String schemaName = "testSchema";
	String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<xs:schema targetNamespace=\"Alma/testSchema\" " +
						"xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
						"elementFormDefault=\"qualified\" " +
						"attributeFormDefault=\"unqualified\">" +
						
						"<xs:element name=\"simple\"/>" +
					"</xs:schema>";
	String namespace ="Alma/testSchema"; 

	public NamespaceTest()
	{
		super();
	}
	
	public void setUp() throws ModuleCriticalException
	{
		try
		{
			iface = InternalIFFactory.getInternalIF(Logger.getAnonymousLogger());
			iface.init();
			imanager = InternalIFFactory.getIdentifierManager(Logger.getAnonymousLogger());
			smanager = iface.getSchemaManager("user");
			register(schema);
		}
		catch (DatabaseException e){fail(e.getMessage());} 
		catch (PermissionDeniedException e) {fail(e.getMessage());}
		catch (UserDoesNotExistException e) {fail(e.getMessage());}
		catch (ArchiveException e) {fail(e.getMessage());}
	}
	
	public void tearDown() throws Exception
	{
		try
		{
			smanager.removeNamespace(schemaName);
			
			smanager.close();
			imanager.close();
			iface.cleanTestArea("user");
			iface.close();
		}
		catch (DatabaseException e){fail(e.getMessage());}
		catch (MalformedURIException e) {fail(e.getMessage());}
		catch (ArchiveException e) {fail(e.getMessage());}
	}
	
	private void register(String schema) throws ModuleCriticalException
	{
		try
		{
			smanager.registerNamespace(schemaName,new URI(namespace));
			URI schemaLocation = imanager.getIdNamespace();
			smanager.addSchema(schemaName,schema,"",schemaLocation,"owner",new Permissions());
			smanager.assignNamespace(schemaName,schemaLocation);
		}
		catch (DatabaseException e) {/*can be ignored*/}
		catch (MalformedURIException e) {fail(e.getMessage());}
		catch (ArchiveException e) {fail(e.getMessage());}
		catch (URISyntaxException e) {fail(e.getMessage());}
	}
	
	public void testStoreGetRemove() throws URISyntaxException, DatabaseException, UnknownSchemaException, ModuleCriticalException, ArchiveException
	{
		URI uid = imanager.getIdNamespace();
		URI schemaLocation = smanager.getSchemaURI(schemaName);
		
		Namespace ns = Namespace.getNamespace(schemaName,namespace);
		DatabaseSimpleEntity entity = new DatabaseSimpleEntity("content",uid.toASCIIString(),"attribute",ns);
		Permissions permissions = new Permissions();
		
		try
		{
			iface.store(uid,entity.toXmlString(),schemaLocation,schemaName,"owner",permissions,"user",true);
			
			String xml = iface.get(uid,"user");
			
			entity = new DatabaseSimpleEntity(xml);
			
			assertEquals("They match","content",entity.getValue());
			
			iface.remove(uid,false,"user");
		}
		catch (UnknownSchemaException e) {fail(e.getMessage());}
		catch (ArchiveException e) {fail(e.getMessage());}
	}
	
	public void testIteration() throws URISyntaxException, PermissionDeniedException, DatabaseException, MalformedQueryException, UnderspecifiedQueryException, UnknownSchemaException, UserDoesNotExistException, UndefinedNamespaceException, ModuleCriticalException, ArchiveException
	{
		URI uid = imanager.getIdNamespace();
		URI schemaLocation = smanager.getSchemaURI(schemaName);
		
		Namespace ns = Namespace.getNamespace(schemaName,namespace);
		DatabaseSimpleEntity entity = new DatabaseSimpleEntity("content",uid.toASCIIString(),"attribute",ns);
		Permissions permissions = new Permissions();
		
		iface.store(uid,entity.toXmlString(),schemaLocation,schemaName,"owner",permissions,"user",true);
		
		HashMap namespaces = smanager.getSchemaNamespaces(schemaLocation);
		DBCursor cursor = iface.query("/testSchema:simple",schemaName,namespaces,false,"user");
		int count = 0;
		while (cursor.hasNext())
		{
			ResultStruct res = cursor.next();
			count++;
		}
		assertEquals(count,1);
		iface.close();
	}
}
