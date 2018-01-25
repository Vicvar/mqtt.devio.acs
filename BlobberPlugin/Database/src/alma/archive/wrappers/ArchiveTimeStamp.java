/*
 * 	  Created on 07-Oct-2003
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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import alma.archive.exceptions.general.DatabaseException;

/**
 * 
 * @author simon
 *
 * 
 */
public class ArchiveTimeStamp
{
	private Timestamp time = null;
	
	protected final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected final SimpleDateFormat sqlFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");   // used by Oracle

	/**
	 * Creates a new timestamp with the current time
	 */
	public ArchiveTimeStamp()
	{
		Calendar now = Calendar.getInstance();
		time = new Timestamp(now.getTimeInMillis());
	}

	/**
	 * Creates an  ArchiveTimestamp corresponding to the SQL timestamp 
	 */
	public ArchiveTimeStamp(Timestamp ts)
	{
		time = ts;
	}

	/**
		 * Parses the string to create a time
		 * @param timeStamp
		 */
		public ArchiveTimeStamp(String timeStampString) throws DatabaseException
		{
			// TODO throw exception!!!
				//time = Timestamp.valueOf(timeStampString);			
				try {
					synchronized (isoFormat) {
						time = new Timestamp(isoFormat.parse(timeStampString).getTime());
					}
				} catch (ParseException e) {
					throw new DatabaseException("Illegal Timestamp format: "+timeStampString);
					//System.out.println(e.toString());
					//System.out.println("Timestamp was: "+timeStampString);
					//MPA: no logging, no action. correct?
					
				}
		}
		
	/**
		 * return java.sql.Timestamp
		 */
		public Timestamp getTimestamp()
		{
				return time;
		}
	


	public static long diff(ArchiveTimeStamp one, ArchiveTimeStamp two)
	{
		return one.time.getTime() - two.time.getTime();
	}
	
	/**
	 * Compares the absolute value of time for equality
	 * @param timeStamp
	 * @return
	 */
	public boolean equals(Object timeStamp)
	{
		// try to cast to ArchiveTimeStamp
		if (timeStamp.getClass().getName()=="alma.archive.wrappers.ArchiveTimeStamp") {
			return ((ArchiveTimeStamp) timeStamp).time.equals(this.time);
		}
		// otherwise cast to Timestamp (also covering java.sql.Timestamp)
		return ((Timestamp) timeStamp).equals(time); 
	}

	public boolean after(ArchiveTimeStamp timeStamp)
	{
		return this.time.after(timeStamp.time);
	}
	
	public boolean before(ArchiveTimeStamp timeStamp)
	{
		return this.time.before(timeStamp.time);
	}
	
	// convenience: the above two methods for java.sql.TimeStamp
	public boolean after(Timestamp timeStamp)
	{
		return this.time.after((Date) timeStamp);
	}
	
	public boolean before(Timestamp timeStamp)
	{
		return this.time.before((Date) timeStamp);
	}

	public String toISOString()
	{
		synchronized (isoFormat) {
			return isoFormat.format(time);
		}
	}

	public String toSQLString()
	{
		synchronized (sqlFormat) {
			return sqlFormat.format(time);
		}
	}

	
//	public static void main(String[] args) throws CloneNotSupportedException 
//	{
//		ArchiveTimeStamp ts = new ArchiveTimeStamp();
//		System.out.println(ts.equals(ts));
//		System.out.println(ts.toISOString());
//		ts = new ArchiveTimeStamp(Timestamp.valueOf("1901-1-1 13:20:40.00001"));
//		System.out.println(ts.toISOString());
//		System.out.println(ts.equals(Timestamp.valueOf("1901-1-1 13:20:40.00001")));
//	}
}
