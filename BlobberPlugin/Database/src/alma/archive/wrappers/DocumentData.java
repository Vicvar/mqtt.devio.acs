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
 *
 *    Created on Aug 28, 2003
 *
 */

// $Author: mbauhofe $
// $Date: 2011/06/14 14:02:54 $
// $Log: DocumentData.java,v $
// Revision 1.11  2011/06/14 14:02:54  mbauhofe
// Replaced XML_HISTORY concept with UID_LOOKUP (view).
// Some clean up/core reorganization.
//
// Revision 1.10  2005/05/13 09:33:46  hmeuss
// replaced deprecated XMLOutputter constructor by new one.
//
// Revision 1.9  2004/05/24 14:44:27  sfarrow
// Added the virtual document flag
//
// Revision 1.8  2004/01/29 13:47:13  hmeuss
// removed version
//
// Revision 1.7  2003/12/10 10:18:37  sfarrow
// *** empty log message ***
//
// Revision 1.6  2003/12/01 15:48:00  sfarrow
// Added getElement method to allow for browser access
//
// Revision 1.5  2003/10/07 20:14:44  sfarrow
// *** empty log message ***
//
// Revision 1.4  2003/10/07 16:20:06  sfarrow
// *** empty log message ***
//
// Revision 1.3  2003/09/24 13:47:38  sfarrow
// A pre alpha of the Xindice implementation of the internal interface
//
// Revision 1.2  2003/08/29 11:25:17  hmeuss
// Java internal interface updated
//
// Revision 1.1  2003/08/28 15:43:04  hmeuss
// Java class for document metadata. Possibly an IDL struct is better?
// 

package alma.archive.wrappers;

import java.net.URI;
import java.net.URISyntaxException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author hmeuss
 *
 */
public class DocumentData {

	private ArchiveTimeStamp m_timestamp;
	private URI m_schema;
	private String m_owner;
	private Permissions m_permissions;
	private String m_locks;
	private boolean m_deleted;
	private String m_admin;
	private boolean m_hidden;
	private boolean m_dirty;
	private boolean m_virtual;

	/**
	 * 
	 */
	public DocumentData(
		ArchiveTimeStamp timestamp,
		URI schema,
		String owner,
		Permissions permissions,
		boolean hidden,
		boolean dirty,
		boolean deleted,
		boolean virtual,
		String admin) {
		m_timestamp = timestamp;
		m_schema = schema;
		m_owner = owner;
		m_permissions = permissions;
		m_hidden = hidden;
		m_dirty = dirty;
		m_deleted = deleted;
		m_virtual = virtual;
	}

	/**
	 * @return
	 */
	public String getAdmin() {
		return m_admin;
	}

	/**
	 * @return
	 */
	public boolean getDeleted() {
		return m_deleted;
	}

	/**
	 * @return
	 */
	public boolean getHidden() {
		return m_hidden;
	}

	/**
	 * @return
	 */
	public boolean getDirty() {
		return m_dirty;
	}

	/**
	 * @return
	 */
	public String getOwner() {
		return m_owner;
	}

	/**
	 * @return
	 */
	public Permissions getPermissions() {
		return m_permissions;
	}

	/**
	 * @return
	 */
	public URI getSchema() {
		return m_schema;
	}

	/**
	 * @return
	 */
	public ArchiveTimeStamp getTimestamp() {
		return m_timestamp;
	}
	
	/**
	 * @return
	 */
	public boolean getVirtual()
	{
		return m_virtual;
	}
	
	public void setVirtual(boolean virtual)
	{
		this.m_virtual = virtual;
	}

	public Element getElement()
	{
		Element dd = new Element("DocumentData");
		dd.setAttribute("timeStamp",m_timestamp.toISOString());
		dd.setAttribute("schema",m_schema.toASCIIString());
		dd.setAttribute("owner",m_owner);
		dd.setAttribute("virtual",Boolean.toString(m_virtual));
		
		dd.addContent(m_permissions.getElement());
		
		Element visibility = new Element("visibility");
		visibility.setAttribute("hidden",Boolean.toString(m_hidden));
		visibility.setAttribute("dirty",Boolean.toString(m_dirty));
		visibility.setAttribute("deleted",Boolean.toString(m_deleted));
		
		dd.addContent(visibility);
		
		Element admin = new Element("admin");
		admin.setText(m_admin);
		
		dd.addContent(admin);
		
		return dd;
	}
	
//	public static void main(String[] args) throws URISyntaxException
//	{
//		DocumentData dd = new DocumentData(new ArchiveTimeStamp(),
//		                                   new URI("/schema"),
//                                           "owner",
//                                           new Permissions(),
//                                           false,
//                                           false,
//                                           false,
//                                           false,
//                                           "admin");
//		Element e = dd.getElement();
//		Document doc = new Document(e);
//
//		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
//		// XMLOutputter out = new XMLOutputter("  ",true,"UTF-8");
//		String o = out.outputString(doc);
//		System.out.println(o);                                 
//	}
}
