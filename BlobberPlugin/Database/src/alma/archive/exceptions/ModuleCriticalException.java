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
 *    Created on Sep 16, 2004
 *
 */
 
  
// $Author: mbauhofe $
// $Date: 2011/06/14 14:02:56 $
// $Log: ModuleCriticalException.java,v $
// Revision 1.6  2011/06/14 14:02:56  mbauhofe
// Replaced XML_HISTORY concept with UID_LOOKUP (view).
// Some clean up/core reorganization.
//
// Revision 1.5  2004/09/23 11:59:15  hmeuss
// Oracle DatabaseReader now creates a new connection object in every method call.
//
// Added ModuleCriticalException to more methods of the internal IF
//
// Revision 1.4  2004/09/22 13:30:13  hmeuss
// getMessage overridden
//
// Revision 1.3  2004/09/20 15:36:49  hmeuss
// Moved all IDLs to IDL module, master component implementation back SubsytemAdministration module
//
// Revision 1.2  2004/09/17 09:06:02  hmeuss
// *** empty log message ***
//
// Revision 1.1  2004/09/17 08:50:38  hmeuss
// Added ModuleCriticalException that will be propagated upwards through all classes
// 
 
package alma.archive.exceptions;

/**
 * 
 * A wrapper for exceptions thrown by Database module that 
 * are critical for the module. If any class of this module throws this exception, it must be
 * catched by a Archive component implementation. In this case,
 * the subsystem master component must be notified using the troubleCode and the troubleMessage, and
 * then the exception is handled according to the cause of the ModuleCriticalException. The cause
 * is always the exception originally thrown in the Database module. 
 * 
 * This class contains two parameters, the troubleCode and the troubleMessage. These are 
 * used to forward the problem to the subsystem master component. 
 * 
 * @author hmeuss
 *
 * Trouble codes:
 * 1) No connection to the database cpuld be established.
 *
 */
public class ModuleCriticalException extends Exception {

    private static final long serialVersionUID = -6476736271153168220L;

    final protected int m_troubleCode;

    final protected String m_troubleMessage;

	/**
	 * @param cause
	 */
	public ModuleCriticalException(Throwable cause, int troubleCode, String troubleMessage) {
		super(cause);
		m_troubleCode=troubleCode;
		m_troubleMessage=troubleMessage;
	}



	/**
	 * @return
	 */
	public int getTroubleCode() {
		return m_troubleCode;
	}

	/**
	 * @return
	 */
	public String getTroubleMessage() {
		return m_troubleMessage;
	}

	/**
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		// we have to return the message of the cause:
		return getCause().getMessage();
	}

}
