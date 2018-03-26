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
 
  
// $Author: sfarrow $
// $Date: 2003/12/01 12:20:04 $
// $Log: ArchiveGeneralException.java,v $
// Revision 1.4  2003/12/01 12:20:04  sfarrow
// Removed dependance on ACS
//
// Revision 1.3  2003/10/22 15:11:10  sfarrow
// ACS-3.0
//
// Revision 1.2  2003/09/19 09:54:39  sfarrow
// Preliminary implementation of the Xindice internal interface
//
// Revision 1.1  2003/08/28 15:41:19  hmeuss
// Some exceptions based AcsJexception
// 
 
package alma.archive.exceptions;
/**
 * @author hmeuss
 *
 */
public class ArchiveGeneralException extends ArchiveException {

	/**
	 * 
	 */
	public ArchiveGeneralException()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ArchiveGeneralException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArchiveGeneralException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ArchiveGeneralException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
