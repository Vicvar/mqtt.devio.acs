/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.archive.tmcdb.persistence;

import java.util.Arrays;

/**
 * Helper class with static methods to help developers to change from fully-qualified component name
 * ("CONTROL/DV01/FrontEnd/ColdCart4") to path + name ("CONTROL/DV01/FrontEnd" + "ColdCart4"),
 * and the other way around
 * 
 * @author rtobar
 *
 */
public class ComponentNameHelper {

	public static String[] getPathAndName(String longname) {

		String[] tokens = longname.split("/");

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<= tokens.length - 2; i++) {
			sb.append(tokens[i]);
			if( i != tokens.length - 2)
				sb.append("/");
		}
		
		String name = tokens[tokens.length - 1];
		return new String[] { sb.toString().replaceAll("^/", ""), name };
	}

	public static String getFullName(String path, String name) {

		StringBuilder sb = new StringBuilder();
		if( path != null && path.length() > 0 )
			sb.append(path).append("/");
		sb.append(name);

		return sb.toString().replaceAll("/+", "/").replaceAll("^/", "").trim();
	}

	// For testing
	public static void main(String args[]) {

		System.out.println(Arrays.toString(getPathAndName("Nexito")));
		System.out.println(Arrays.toString(getPathAndName("CONTROL/DV01/FrontEnd/ColdCart1")));
		System.out.println(Arrays.toString(getPathAndName("*")));
		System.out.println(Arrays.toString(getPathAndName("/alma/ACS-8.2")));
		System.out.println(Arrays.toString(getPathAndName("/CORR/LALA/LALO/")));

		System.out.println(getFullName("/CORR/LALA","//LALO/"));
		System.out.println(getFullName("CONTROL/DV01/FrontEnd","ColdCart1"));
	}
}