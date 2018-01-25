/*
 * 	  Created on 09-Sep-2003
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
package alma.archive.wrappers;

import org.jdom.Element;

/**
 * @author simon
 */
public class Permissions
{
	public Permissions(){}
	
	public Permissions (String read, String write)
	{
		this.read = replaceNull(read);
		this.write = replaceNull(write);
	}
	/*
	 * this is going to associate read write etc. with both an owner
	 * and a user. up for debate
	 */
	 private String read ="";
	 private String write="";
	 
	/**
	 * @return
	 */
	public String getRead()
	{
		return read;
	}

	/**
	 * @return
	 */
	public String getWrite()
	{
		return write;
	}

	/**
	 * @param string
	 */
	public void setRead(String string)
	{
		read = replaceNull(string);
	}

	/**
	 * @param string
	 */
	public void setWrite(String string)
	{
		write = replaceNull(string);
	}

	/**
	 * 
	 * replace null values by the empth string.
	 * Necessary for Oracle sometimes.
	 * 
	 * @param string
	 * @return The input string, if not null. Else the empty string "".
	 */
	private String replaceNull(String string) {
		if (string==null) {
			return "";
	} else {
		return string;
	}
	}

	public Element getElement()
	{
		Element perm = new Element("permissions");
		perm.setAttribute("write",write);
		perm.setAttribute("read",read);
		return perm;
	}
}
