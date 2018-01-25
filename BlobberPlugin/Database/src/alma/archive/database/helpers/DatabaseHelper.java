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
 *    Created on Jan 16, 2004
 *
 */
package alma.archive.database.helpers;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.syntax.MalformedXMLException;

/**
 * General helper methods possibly used by Xindice and DB2
 * 
 * @author hmeuss
 *
 */
public class DatabaseHelper {
	
	//private static Pattern basicUidPattern = Pattern.compile("^[uU][iI][dD]://[xX][0-9a-fA-F]{2,}(/[xX][0-9a-fA-F]+){2}$");
	// the following allows for old style UIDs, too:
	// private static Pattern basicUidPattern = Pattern.compile("^[uU][iI][dD]://[xX][0-9a-fA-F]{2,}(/[xX][0-9a-fA-F]+){1,2}$");
	// and now the first part (archiveId) can be any string consisting of characters and numbers:
	public static Pattern basicUidPattern = Pattern.compile("^[uU][iI][dD]://[0-9a-zA-Z]+(/[xX][0-9a-fA-F]+){2}(#\\w{1,}){0,}$");
	// restrictedUidPattern contains no local part, ie. no # followed by local identifiers:
	public static Pattern restrictedUidPattern = Pattern.compile("^[uU][iI][dD]://[0-9a-zA-Z]+(/[xX][0-9a-fA-F]+){2}$");

	
	
	/**
	 * check whether user is allowed to access entity owned by docOwner where entity has given permissions.
	 * When user wants to read entity, permissions parameter should be read permissions of entity, else 
	 * write permissions.
	 * 
	 * @param user
	 * @param permissions
	 * @param docOwner
	 * @return true, if user is allowed to access document according to permissions string. 
	 */
	@Deprecated
	public static boolean checkAccessPermissions(
		String user,
		String permissions,
		String docOwner) {

		// TODO implement
		return true;
	}

	/**
	 * Compares UID of incoming document with ArchiveId and implements policiy,
	 * what may be stored.
	 * @throws MalformedURIException
	 */
	public static boolean checkArchiveIdStoragePermission(URI uid, IdentifierManager imanager) throws DatabaseException, MalformedURIException {
		
		// TODO implement new strategy
		
		// first check whether incoming UID is a *basic* UID, ie. contains no '#...' part after the local part:
		Matcher m = restrictedUidPattern.matcher(uid.toString());
		if (!m.matches()) {
			throw new MalformedURIException("The UID is not wellformed: "+uid.toString());
		}
		
		// not allowed: incoming ID=0, archive<>0:
		String archiveId="DUMMY";
		try {
			archiveId = imanager.getArchiveId();
		} catch (Exception e) {
			throw new DatabaseException("Could not retrieve ArchiveId. "+e.toString());
		}
		//System.out.println("XXXX   "+archiveId+"  UID: "+uid.toString());
		// if we are in an operational Archive (ie. archiveId!="X00"), then we don't store local documents:
		if (!archiveId.startsWith("X0")) {
			//System.out.println("FALSE CASE: "+!uid.toString().startsWith("uid://X0"));
			return !uid.toString().startsWith("uid://X0");
		} 
		
		return true;
	}
	
	/**
	 * read a file
	 * @param fileLocation
	 * @return string with file contents
	 */
	public static String readFile(String fileLocation) throws IOException {
		FileReader fr = null;
		StringBuffer content = new StringBuffer("");
		try {
			fr = new FileReader(fileLocation);
			while (fr.ready()) {
				content.append((char) fr.read());
			}
		}
		finally {
			if (fr != null) fr.close();
		}
		return content.toString();
	}

	// adds new child as last child of the root element of xml
	public static String addAsLastChild(String xml, String newChild) throws DatabaseException, MalformedXMLException {
		SAXBuilder parser = new SAXBuilder();
		Document doc;
		try {
			doc = parser.build(new StringReader(xml));
		} catch (JDOMException e) {
			throw new DatabaseException("Could not parse original stored XML document.");
		} catch (IOException e) {
			throw new DatabaseException("Something completely wrong here: "+e.toString());
		}
		Element root = doc.getRootElement();
		
		Element child = null;
		try {
			child = parser.build(new StringReader(newChild)).getRootElement();
		} catch (JDOMException e) {
			throw new MalformedXMLException("Could not parse new child element.");			
		} catch (IOException e) {
			throw new DatabaseException("Something completely wrong here: "+e.toString());
		}
		
		root.addContent(child.detach());
		
		return new XMLOutputter().outputString(doc);
	}

    /**
     * Prints the exceptions stack trace to a string and returns it.
     */
    public static String traceToString(Throwable e) {
        final StringWriter sWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(sWriter));
        return sWriter.toString();
    }
}
