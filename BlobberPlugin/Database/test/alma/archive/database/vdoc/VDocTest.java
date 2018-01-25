/*
 * 	  Created on 01-Mar-2005
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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.lite.LiteInternalIF;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.EntityDirtyException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.EntityDoesNotExistException;
import alma.archive.exceptions.general.VDocException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.wrappers.Permissions;
import alma.archive.wrappers.UniqueIdentifier;

/**
 * @author simon
 */
public class VDocTest extends TestCase
{
	InternalIF internal = null;
	HashMap content = null;
	URI contextid = null;
	URI defContextid = null;
	
	String defContext = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
			"<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>" +
			"<xsl:template match=\"/\">" +
				"<simpledoc>" +
					"<xsl:apply-templates select=\"/uriList/uri\"/>" +
				"</simpledoc>" +
			"</xsl:template>" +
			
			"<xsl:template match=\"uri\">" +
				"<xsl:copy-of select=\"document(@location)\"></xsl:copy-of>" +
			"</xsl:template>" +
		"</xsl:stylesheet>";
	
	String vdocContext = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
			"<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>" +
				"<xsl:template match=\"/vdoc\">" +
					"<newdocumenttype>" +
						"<xsl:copy-of select=\"*\"></xsl:copy-of>" +
					"</newdocumenttype>" +
				"</xsl:template>" +
		"</xsl:stylesheet>";

	/**
	 * 
	 */
	public VDocTest() {super("VDoc Test");}

	public void setUp() throws Exception
	{
		internal = new LiteInternalIF();
		content = new HashMap();
		contextid = new URI("uid://X0000000000000001/X00000000");
		addContent(content,0,10);
		storeContent(content,internal);
		
		defContextid = new URI("uid://X0000000000000001/X00000001");
		internal.store(defContextid,
			defContext,defContextid,"schema","owner",
			new Permissions(),"user",true);
		
		internal.store(contextid,
			vdocContext,contextid,"schema","owner",
			new Permissions(),"user",true);
	}
	
	private void addContent(HashMap content,int start, int count)
	{
		for (int x = start; x < count; x++)
		{
			String xml = "<simple id=\"" + x + "\"/>";
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
	
	public void testAddDefinition() throws VDocException
	{
		Definition def = Definition.instance("XPath",contextid);
		VDoc vdoc = new VDoc(contextid);
		vdoc.setDescription("A little test VDoc to see if everything works");
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		Element element = vdoc.getElement();
		List list = element.getChildren("definition");
		if (list.size() != 2) {fail();}
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(element));
	}
	
	public void testRemoveDefinition() throws VDocException
	{
		Definition def = Definition.instance("XPath",contextid);
		VDoc vdoc = new VDoc(contextid);
		vdoc.setDescription("A little test VDoc to see if everything works");
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		vdoc.removeDefinition(0);
		Element element = vdoc.getElement();
		List list = element.getChildren("definition");
		if (list.size() != 1) {fail();}		
	}
	
	public void testInsertDefinition() 
		throws 
			VDocException, 
			URISyntaxException
	{
		Definition def = Definition.instance("XPath",contextid);
		VDoc vdoc = new VDoc(contextid);
		vdoc.setDescription("A little test VDoc to see if everything works");
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		
		URI contextid2 = new URI("uid://X0000000000000000/X00000001");
		Definition def2 = Definition.instance("XPath",contextid);
		def2.setContextid(contextid2);
		vdoc.insertDefinition(def2,1);
		
		Element element = vdoc.getElement();
		List list = element.getChildren("definition");
		Element definition = (Element)list.get(1);
		if (!contextid2.toASCIIString().equalsIgnoreCase(
			definition.getAttributeValue("contextid"))){
			fail();
		}
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(element));
	}

	public void testMoveDefinition()
		throws 
			VDocException, 
			URISyntaxException
	{
		Definition def = Definition.instance("XPath",contextid);
		VDoc vdoc = new VDoc(contextid);
		vdoc.setDescription("A little test VDoc to see if everything works");
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		
		URI contextid2 = new URI("uid://X0000000000000000/X00000001");
		Definition def2 = Definition.instance("XPath",contextid);
		def2.setContextid(contextid2);
		vdoc.insertDefinition(def2,1);
		
		vdoc.moveDefinition(1,2);
		
		Element element = vdoc.getElement();
		List list = element.getChildren("definition");
		Element definition = (Element)list.get(2);
		if (!contextid2.toASCIIString().equalsIgnoreCase(
			definition.getAttributeValue("contextid"))){
			fail();
		}
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(element));
	}
	
	public void testConstructors()
		throws 
			VDocException, 
			URISyntaxException
	{
		Definition def = Definition.instance("XPath",contextid);
		VDoc vdoc = new VDoc(contextid);
		vdoc.setDescription("A little test VDoc to see if everything works");
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		
		Element element = vdoc.getElement();
		
		VDoc vdoc2 = new VDoc(element);
		Element element2 = vdoc2.getElement();
		
		if (element2 == null) {fail();}
		
		//XMLOutputter out = new XMLOutputter("  ",true);
		//System.out.println(out.outputString(element2));
	}
	
	public void testProcess() 
		throws 
			PermissionDeniedException, 
			EntityDirtyException, 
			EntityDoesNotExistException, 
			DatabaseException, 
			UserDoesNotExistException, 
			MalformedURIException, 
			ModuleCriticalException, 
			ArchiveException, 
			JDOMException, 
			IOException
	{
		Definition def = Definition.instance("XPath",defContextid);
		def.setParameter("XPath","/simple");
		def.setParameter("Schema","uid://X0000000000000000/X00000000");
		def.setParameter("User","user");
		
		VDoc vdoc = new VDoc(contextid);
		vdoc.addDefinition(def);
		vdoc.addDefinition(def);
		
		String result = vdoc.process(internal,"user");
//		System.out.println(result);
		
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		
		Document doc = builder.build(new StringReader(result));
		Element root = doc.getRootElement();
		
		List vdocs = root.getChildren("simpledoc");
		if (vdocs.size() != 2){fail();}
		
		Element vdoc_e = (Element)vdocs.get(0);
		List simples = vdoc_e.getChildren("simple");
		if (simples.size() != 10){fail();}
	}
}
