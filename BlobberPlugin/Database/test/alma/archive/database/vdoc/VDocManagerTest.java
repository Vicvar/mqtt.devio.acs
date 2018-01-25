/*
 * 	  Created on 18-Oct-2004
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
package alma.archive.database.vdoc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.database.lite.LiteIdentifierManager;
import alma.archive.database.lite.LiteInternalIF;
import alma.archive.database.oracle.DBConfig;
import alma.archive.database.vdoc.VDocManager;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.VDocException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.wrappers.Permissions;
import junit.framework.TestCase;

/**
 * @author simon
 */
public class VDocManagerTest extends TestCase
{
	InternalIF internal = null;
	IdentifierManager identifier = null;
	SchemaManager smanager = null;
	Logger logger = Logger.global;
	
	String defContext = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
			"<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>" +
			"<xsl:template match=\"/\">" +
				"<vdoc>" +
					"<xsl:apply-templates select=\"/uriList/uri\"/>" +
				"</vdoc>" +
			"</xsl:template>" +
			
			"<xsl:template match=\"uri\">" +
				"<xsl:copy-of select=\"document(@location)\"></xsl:copy-of>" +
			"</xsl:template>" +
		"</xsl:stylesheet>";
	
	URI defContextId = null;
	
	String vdocContext = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
			"<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>" +
				"<xsl:template match=\"/\">" + 
					"<xsl:copy-of select=\".\"></xsl:copy-of>" +
				"</xsl:template>" +
		"</xsl:stylesheet>";
	
	URI vdocContextId = null;
	
	String contextSchema = "<contextSchema/>";
	URI contextSchemaId = null;
	
	public void setUp() 
		throws 
			URISyntaxException, 
			PermissionDeniedException, 
			UserDoesNotExistException, 
			ModuleCriticalException, 
			ArchiveException
	{
		internal = new LiteInternalIF();
		smanager = internal.getSchemaManager("user");
		identifier = new LiteIdentifierManager();
		
		vdocContextId = identifier.getIdNamespace();
		
		internal.store(vdocContextId,vdocContext,vdocContextId,
			"schema","owner",new Permissions(),"user",true);
		
		defContextId = identifier.getIdNamespace();
		internal.store(defContextId,defContext,defContextId,
			"schema","owner",new Permissions(),"user",true);
		
		contextSchemaId = identifier.getIdNamespace();
		smanager.addSchema("contextSchema",contextSchema,"",
			contextSchemaId,"owner",new Permissions());
	}
	
	
	public void tearDown() 
		throws 
			DatabaseException, 
			MalformedURIException, 
			ModuleCriticalException, 
			ArchiveException
	{
		internal.close();
		internal = null;
		identifier.close();
		identifier = null;
		smanager.close();
		smanager = null;
	}
	
	public void testVDocContexts() 
		throws 
			DatabaseException, 
			ModuleCriticalException, 
			ArchiveException
	{
		VDocManager vdm = VDocManager.instance(internal,identifier,logger);
		String description = "A little test description to see if we can " +
			"break the VDoc Manager";
		
		//Test the adding
		URI uri1 = vdm.addVDocContext("Test VDoc Context One",
			description,vdocContext,"user","owner",contextSchemaId);
		URI uri2 = vdm.addVDocContext("Test VDoc Context Two",
			description,vdocContext,"user","owner",contextSchemaId);
		Element contexts = vdm.getVDocContexts();
		
		List children = contexts.getChildren();
		if (children.size() != 2){
			fail();
		}
		
		//Test the updating
		vdm.updateVDocContext(uri1,vdocContext,"user");
		vdm.updateVDocContext(uri2,vdocContext,"user");
		
		vdm.close();
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(contexts));
	}
	
	public void testDefinitionContexts()
		throws 
			DatabaseException, 
			ModuleCriticalException, 
			ArchiveException
	{
		VDocManager vdm = VDocManager.instance(internal,identifier,logger);
		String description = "A little test description to see if we can " +
			"break the VDoc Manager";
		URI uri1 = vdm.addDefinitionContext("Test Definition One",
			description,defContext,"user","owner",contextSchemaId);
		URI uri2 = vdm.addDefinitionContext("Test Definition Two",
			description,defContext,"user","owner",contextSchemaId);
		Element contexts  = vdm.getDefinitionContexts();
		List children = contexts.getChildren();
		if (children.size() != 2){
			fail();
		}
		
		//test the updating
		vdm.updateDefinitionsContext(uri1,defContext,"user");
		vdm.updateDefinitionsContext(uri2,defContext,"user");
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(contexts));
		
		vdm.close();
	}
	
	public void testDefinitionTypeRegister() 
		throws 
			DatabaseException, 
			ModuleCriticalException, 
			ArchiveException
	{
		VDocManager vdm = VDocManager.instance(internal,identifier,logger);
		vdm.registerDefinitionType("XPath");
		vdm.registerDefinitionType("XPath");
		
		Element defintions = vdm.getDefinitions();
		
		List children = defintions.getChildren();
		if (children.size() != 1){
			fail();
		}
		
		try{
			vdm.registerDefinitionType("BobTheBuilder");
			fail();
		}
		catch (VDocException e){}
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(defintions));
		vdm.close();
	}
	
	public void testDefinitionTypeUnregister()
		throws 
			DatabaseException, 
			ModuleCriticalException, 
			ArchiveException
	{
		VDocManager vdm = VDocManager.instance(internal,identifier,logger);
		vdm.registerDefinitionType("XPath");
		vdm.unregisterDefinitionType("XPath");
		
		Element defintions = vdm.getDefinitions();
		
		List children = defintions.getChildren();
		if (children.size() != 0){
			fail();
		}
		vdm.close();
	}
	
	public void testVDoc() 
		throws 
			VDocException, 
			DatabaseException, 
			ModuleCriticalException, 
			URISyntaxException
	{
		VDocManager vdm = VDocManager.instance(internal,identifier,logger);
		
		URI id = vdm.createVDoc(vdocContextId,"user","owner",contextSchemaId);
		
		Element vdocs = vdm.getVDocLocations();
		Element vdocDesc = vdocs.getChild("vdocDesc");
		URI location = new URI(vdocDesc.getAttributeValue("location"));
		
		VDoc vdoc = vdm.getVDoc(location,"user");
		Definition def = Definition.instance("XPath",defContextId);
		vdoc.addDefinition(def);
		
		vdm.updateVDoc(location,vdoc,"user");
		
		vdm.close();
	}
}
