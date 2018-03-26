/*
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

package alma.archive.utils;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author simon
 * 
 * DatabaseSimpleEntity.java Created by simon on 07-Nov-2002
 * 
 */
public final class DatabaseSimpleEntity
{
	private String value = "";
	private String attribute = "";
	private Namespace ns = null;
	
	
	public DatabaseSimpleEntity(){}
	public DatabaseSimpleEntity(String xml)
	{
		this.parse(xml);
	}
	
	public DatabaseSimpleEntity(String value, String uid)
	{
		this.value = value;
	}
	
	public DatabaseSimpleEntity(String value, String uid, String attr)
	{
		this.value = value;
		this.attribute = attr;
	}
	
	public DatabaseSimpleEntity(String value, String uid, String attr, Namespace ns)
	{
		this.value = value;
		this.attribute = attr;
		this.ns = ns;
	}
	
	public String toXmlString()
	{
		Element root = new Element("simple");
		Document doc = new Document(root);
		
		root.setAttribute("attr",attribute);
		root.setText(value);
		if (ns != null) root.setNamespace(ns);


		XMLOutputter out = new XMLOutputter(Format.getRawFormat());
		// XMLOutputter out = new XMLOutputter("",false, "UTF-8");
		String output = out.outputString(doc);
		return output;
	}
	
	public void parse(String xmlString)
	{
		SAXBuilder builder = new SAXBuilder();
		try
		{ 
			Document doc = builder.build(new StringReader(xmlString));
			Element root = doc.getRootElement();
			value = root.getText();
			attribute = root.getAttributeValue("attr");
		}
		catch (JDOMException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String getAttribute()
	{
		return attribute;
	}
}