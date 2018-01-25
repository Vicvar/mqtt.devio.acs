/*
 * 	  Created on 24-Feb-2004
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import alma.ArchiveIdentifierError.wrappers.AcsJArchiveIdentifierErrorEx;
import alma.archive.database.interfaces.DBCursor;
import alma.archive.database.interfaces.IdentifierManager;
import alma.archive.database.interfaces.InternalIF;
import alma.archive.database.interfaces.InternalIFFactory;
import alma.archive.database.interfaces.SchemaManager;
import alma.archive.exceptions.ArchiveException;
import alma.archive.exceptions.ModuleCriticalException;
import alma.archive.exceptions.access.PermissionDeniedException;
import alma.archive.exceptions.general.DatabaseException;
import alma.archive.exceptions.general.EntityExistsException;
import alma.archive.exceptions.user.UserDoesNotExistException;
import alma.archive.wrappers.ArchiveTimeStamp;
import alma.archive.wrappers.DocumentData;
import alma.archive.wrappers.Permissions;
/**
 * @author simon
 */
public class InternalIFPerfomanceTest
{
	private class SuffixFilter implements FileFilter
	{
		private String suffix;
		
		public SuffixFilter (String suffix)
		{
			this.suffix = suffix;
		}

		public boolean accept(File pathname)
		{
			String name = pathname.getName();
			if (name.endsWith(suffix)) return true;
			else return false;
		}
	}
	
	private InternalIF internal;
	private SchemaManager smanager;
	private IdentifierManager imanager;
	
	private long[] diffList = new long[10000];
	private long[] retdifflist = new long[10000];
	private int total = 0;
	private int rettotal = 0;
	
	
	public InternalIFPerfomanceTest() throws ModuleCriticalException
	{
		try
		{
			internal = InternalIFFactory.getInternalIF(Logger.getAnonymousLogger());
			internal.init();
			imanager = InternalIFFactory.getIdentifierManager(Logger.getAnonymousLogger());
			smanager = internal.getSchemaManager("user");
		}
		catch (ArchiveException e) {e.printStackTrace();}
	}
	
	private File[] fileList(String path,String suffix)
	{
		SuffixFilter filter = new SuffixFilter(suffix);
		File directory = new File(path);
		return directory.listFiles(filter);
	}
	
	private void store(File file, int filenum, String schemaname) throws ModuleCriticalException
	{
		SAXBuilder builder = new SAXBuilder();
		try
		{	
			Document doc = builder.build(new FileReader(file));		

			XMLOutputter out = new XMLOutputter(Format.getRawFormat());
			// XMLOutputter out = new XMLOutputter("",false,"UTF-8");
			String xml = out.outputString(doc);
			
			URI uri = imanager.getIdNamespace();
			URI schema = smanager.getSchemaURI(schemaname);
			
			try
			{
				ArchiveTimeStamp start = new ArchiveTimeStamp();
				internal.store(uri,xml,schema,schemaname,"owner", new Permissions(),"user",true);
				ArchiveTimeStamp stop = new ArchiveTimeStamp();
				
				retrive(uri,filenum);

				long diff = ArchiveTimeStamp.diff(stop,start);
				diffList[filenum] = diff;
				total++;
			}
			catch (EntityExistsException e)
			{
				DocumentData dd = internal.status(uri,"user");
				internal.update(uri,dd.getTimestamp(),xml,schema,false,"user");
			}
 
		}
		catch (JDOMException e) {e.printStackTrace();}
		catch (FileNotFoundException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		catch (ArchiveException e) {e.printStackTrace();}
	}
	
	
	private void retrive(URI uri, int filenum) throws ModuleCriticalException
	{
		try 
		{
			long diff = 0;
			int iterations = 10;
			for (int x = 0; x < iterations; x++)
			{
				ArchiveTimeStamp start = new ArchiveTimeStamp();
				String result = internal.get(uri,"user");
				ArchiveTimeStamp stop = new ArchiveTimeStamp();
				diff = diff + (ArchiveTimeStamp.diff(stop,start));
			}
			diff = diff / iterations;
			retdifflist[filenum] = diff;
			rettotal++;
		}
		catch (ArchiveException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void query(String schemaName) throws ModuleCriticalException
	{
		try 
		{
			String query = "/*";
			URI schemaUri = smanager.getSchemaURI(schemaName);
			HashMap namespaces = smanager.getSchemaNamespaces(schemaUri);
			long diff = 0;
			int iterations = 2;
			for (int x = 0; x < iterations; x++)
			{
				ArchiveTimeStamp start = new ArchiveTimeStamp();
				DBCursor cursor = internal.query(query,schemaName,namespaces,false,"user");
				ArchiveTimeStamp stop = new ArchiveTimeStamp();
				cursor.close();
				diff = diff + ArchiveTimeStamp.diff(stop,start);
			}
			diff = diff / iterations;
//			System.out.println("Retrive -> Result : " + diff);
		}
		catch (ArchiveException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void clean() throws PermissionDeniedException, DatabaseException, UserDoesNotExistException, ModuleCriticalException, ArchiveException, AcsJArchiveIdentifierErrorEx
	{
		internal.cleanTestArea("user");
		imanager.close();
		smanager.close();
		internal.close();	
	}
	
	private void printStats()
	{
		long totaldiff = 0;
		long min = diffList[0];
		long max = 0;
		for (int x = 0; x < total; x++)
		{
			if (diffList[x] != 0)
			{
				totaldiff += diffList[x];
				if (diffList[x] < min) min = diffList[x];
				if (diffList[x] > max) max = diffList[x];
			} 
		}
		long average = totaldiff / total;
//		System.out.println("Store   -> Average: " + average + " Min: " + min + " Max: " + max);
		
		
		totaldiff = 0;
		min = retdifflist[0];
		max = 0;
		for (int x = 0; x < rettotal; x++)
		{
			if (retdifflist[x] != 0)
			{
				totaldiff += retdifflist[x];
				if (retdifflist[x] < min) min = retdifflist[x];
				if (retdifflist[x] > max) max = retdifflist[x];
			} 
		}
		average = totaldiff / rettotal;
//		System.out.println("Retrive -> Average: " + average + " Min: " + min + " Max: " + max);
	}
	
	/**
	 * This will store the xml files on the end of the path. Even if the
	 * number of files is less than that required it will just reuse the
	 * files under different UID's
	 * 
	 * @param inpath
	 * @param limit
	 * @param schemaname The schema name that the xml will be stored under
	 */
	public void run(String inpath, int limit,String schemaname) throws ArchiveException, ModuleCriticalException, AcsJArchiveIdentifierErrorEx
	{
		File[] files = fileList(inpath,"xml");
		int stored = 0;
		for (int x = 0; stored < limit; x++)
		{
//			System.out.println(stored + " ");
			if (x >= files.length) x = 0;
			store(files[x],stored,schemaname);
			stored ++;
		}
		printStats();
		query(schemaname);
		clean();
	}
	
	public static void main(String[] args) throws NumberFormatException, ArchiveException, ModuleCriticalException, AcsJArchiveIdentifierErrorEx
	{
		InternalIFPerfomanceTest tl = new InternalIFPerfomanceTest();
		tl.run(args[0],Integer.parseInt(args[1]),args[2]);
	}
}
