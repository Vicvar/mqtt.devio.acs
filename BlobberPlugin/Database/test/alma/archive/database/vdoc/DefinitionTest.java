/*
 * 	  Created on 24-Feb-2005
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.lite.LiteInternalIF;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.EntityExistsException;
import alma.archive.exceptions.general.UnknownSchemaException;
import alma.archive.exceptions.general.VDocException;
import alma.archive.exceptions.syntax.MalformedPermissionsException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.syntax.MalformedXMLException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.wrappers.Permissions;
import alma.archive.wrappers.UniqueIdentifier;
import junit.framework.TestCase;

/**
 * @author simon
 */
public class DefinitionTest extends TestCase
{

	InternalIF internal = null;
	HashMap content = null;
	URI contextid = null;
	
	public DefinitionTest()
	{
		super("Definition Test");
	}
	
	public void setUp() throws Exception
	{
		internal = new LiteInternalIF();
		content = new HashMap();
		contextid = new URI("uid://X0000000000000000/X00000000");
		addContent(content,0,10);
		storeContent(content,internal);
	}
	
	private void addContent(HashMap content,int start, int count)
	{
		String xml = "<simple></simple>";
		for (int x = start; x < count; x++)
		{
			UniqueIdentifier uid = new UniqueIdentifier(0,x);
			content.put(uid.toURI(),xml);
		}
	}
	
	private void storeContent(HashMap content, InternalIF internal)
		throws
			Exception
	{
		Iterator iter = content.keySet().iterator();
		while (iter.hasNext())
		{
			URI uri = (URI)iter.next();
			String xml = (String)content.get(uri);
			internal.store(uri,
				xml,uri,"schema","owner",
				new Permissions(),"user",true);
		}
	}
	
	public void tearDown() throws Exception
	{
		internal.cleanTestArea("user");
		content = null;
	}
	
	/*
	public void testDefinitionTypes() throws VDocException
	{
		Element types = Definition.getSourceTypes();
		List list = types.getChildren();
		if (list.size() == 0){fail("There were: " + list.size() 
			+ " types found");}
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(types));
	}
	*/
	
	public void testXPathDefinitionParams() 
		throws 
			VDocException
	{
		Definition def = Definition.instance("XPath",contextid);
		
		//Set up some values without it falling over
		def.setParameter("XPath","/simple");
		def.setParameter("Schema","uid://X0000000000000000/X00000000");
		def.setParameter("User","user");
		
		HashMap map = new HashMap();
		map.put("XPath","/simple");
		map.put("Schema","uid://X0000000000000000/X00000000");
		map.put("User","user");
		
		//Check that the values are correct
		Element parameters = def.getElement();
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(parameters));
		List list = parameters.getChildren("parameter");
		Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			Element parameter = (Element)iter.next();
			String name = parameter.getAttributeValue("name");
			String val = parameter.getAttributeValue("val");
			
			if (map.containsKey(name)){
				if (!map.get(name).equals(val)){
					fail();
				}
			}
			else {fail();}
		}
	}
	
	public void testXPathDefinitionList() 
		throws 
			VDocException, URISyntaxException
	{
		Definition def = Definition.instance("XPath",contextid);
		
		//Set up some values without it falling over
		def.setParameter("XPath","/simple");
		def.setParameter("Schema","uid://X0000000000000000/X00000000");
		def.setParameter("User","user");
		
		Element uriList = def.createList(internal);
		List list = uriList.getChildren("uri");
		Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			Element uri = (Element)iter.next();
			URI location = new URI(uri.getAttributeValue("location"));
			if (!content.containsKey(location)) {fail();}
		}
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(uriList));
	}
}
