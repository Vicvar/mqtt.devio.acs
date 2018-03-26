/*
 * 	  Created on 20-Apr-2005
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.database.lite.LiteIdentifierManager;
import alma.archive.database.lite.LiteInternalIF;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.EntityDoesNotExistException;
import alma.archive.exceptions.syntax.IllegalHistoryNumberException;
import alma.archive.exceptions.syntax.MalformedURIException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.ngas.client.JClient;
import alma.ngas.client.NGAMSJClientLite;

/**
 * @author simon
 */
public class DocumentFinderTest extends TestCase
{
	String VOTableSchema = "<VOTable/>";
	String simpleDoc = "<a><b><c cAtt=\"cc\"/><c/><d><d/><e/></d></b></a>";
	String xmlFileName = "X0000000000000002_X00000000";
	
	InternalIF internal = null;
	SchemaManager smanager = null;
	IdentifierManager imanager = null;
	URI docid = null;
	
	public DocumentFinderTest()
	{
		super("DocumentFinderTest");
	}
	
	public void setUp() 
		throws 
			PermissionDeniedException, 
			UserDoesNotExistException, 
			ModuleCriticalException, 
			ArchiveException
	{
		internal = new LiteInternalIF();
		smanager = internal.getSchemaManager("test");
		imanager = new LiteIdentifierManager();
		
		//Add a "VOTable" schema
		URI schemaid = imanager.getIdNamespace();
		smanager.addSchema("VOTable",VOTableSchema,"",
			schemaid,"owner",new Permissions());
		
		//Add an xml document to the lite store
		docid = imanager.getIdNamespace();
		internal.store(docid,simpleDoc,schemaid,
			"VOTable","owner",new Permissions(),"user",true);
		
		//Make sure that the test file is in the right place
		File xml = new File(xmlFileName);
		if (!xml.exists()) {
			throw new ArchiveException(
				"Cant find the test xml file: " + xmlFileName);
		}
	}
	
	public void tearDown() 
		throws 
			DatabaseException, 
			ModuleCriticalException
	{
		smanager.close();
		internal.close();
	}
	
	public void testDocumentFinderLocal() 
		throws 
			PermissionDeniedException, 
			EntityDoesNotExistException, 
			DatabaseException, 
			UserDoesNotExistException, 
			IllegalHistoryNumberException, 
			MalformedURIException, 
			ModuleCriticalException, 
			ArchiveException, 
			IOException,
			MessagingException
	{
		List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		list.add(new InetSocketAddress("localhost", 7777));
		JClient ngasJClient = 
			new NGAMSJClientLite(
				list); 
		DocumentFinder df = new DocumentFinder(
			internal,ngasJClient,Logger.global,false,"");
		df.addNgasType("VOTable");
		
		InputStream istream = df.fetch(docid);
		
		
		MimeBodyPart mp = new MimeBodyPart(istream);
		MimeMultipart mmp = (MimeMultipart) mp.getContent();
		String idString = mp.getHeader("alma-uid")[0];
		String xml = (String) mmp.getBodyPart(0).getContent();
	}
	
	public void testDocumentFinderRemote() 
		throws 
			PermissionDeniedException, 
			EntityDoesNotExistException, 
			DatabaseException, 
			UserDoesNotExistException, 
			IllegalHistoryNumberException, 
			MalformedURIException, 
			ModuleCriticalException, 
			ArchiveException
	{
		
		List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		list.add(new InetSocketAddress("localhost", 7777));
		JClient ngasJClient = 
			new NGAMSJClientLite(list); 

		DocumentFinder df = new DocumentFinder(
			internal,ngasJClient,Logger.global,true,"");
		df.addNgasType("VOTable");
		
		InputStream istream = df.fetch(docid); 
	}
}
