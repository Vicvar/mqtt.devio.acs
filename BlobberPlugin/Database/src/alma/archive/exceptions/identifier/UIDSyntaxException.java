/*
 * 	  Created on 06-Apr-2005
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
package alma.archive.exceptions.identifier;

import alma.archive.exceptions.ArchiveException;

/**
 * @author simon
 */
public class UIDSyntaxException extends ArchiveException
{

	/**
	 * 
	 */
	public UIDSyntaxException()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public UIDSyntaxException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UIDSyntaxException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UIDSyntaxException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
