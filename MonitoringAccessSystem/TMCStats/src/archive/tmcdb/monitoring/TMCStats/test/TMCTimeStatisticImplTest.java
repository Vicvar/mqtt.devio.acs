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
package archive.tmcdb.monitoring.TMCStats.test;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

import archive.tmcdb.monitoring.TMCStats.TMCTimeStatisticImpl;

/**
 * Test for the TMCClobProcessTimeStatisticImpl class
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCTimeStatisticImplTest {
	private static final String NAME = "ClobProcessTime";
	private TMCTimeStatisticImpl tmcTimeStatisticImpl;

	@Before
	public void setUp() {
		tmcTimeStatisticImpl = new TMCTimeStatisticImpl(NAME);
	}

	@Test
	public void testGetName() {
		assertEquals(tmcTimeStatisticImpl.getName(), NAME);
	}

	@Test
	public void testSetName() {
		tmcTimeStatisticImpl.setName(NAME + "2");
		assertEquals(tmcTimeStatisticImpl.getName(), NAME + "2");
	}

	@Test
	public void testReset() {
		tmcTimeStatisticImpl.reset();

		assertEquals(tmcTimeStatisticImpl.getCount(), 0);
		assertEquals(tmcTimeStatisticImpl.getMaxTime(), 0);
		assertEquals(tmcTimeStatisticImpl.getMinTime(), 0);
		assertEquals(tmcTimeStatisticImpl.getTotalTime(), 0);
	}

	@Test
	public void testAddTime() {
		tmcTimeStatisticImpl.setCount(0);
		tmcTimeStatisticImpl.setMaxTime(0);
		tmcTimeStatisticImpl.setMinTime(0);
		tmcTimeStatisticImpl.addTime(100);

		assertEquals(tmcTimeStatisticImpl.getCount(), 1);
		assertEquals(tmcTimeStatisticImpl.getTotalTime(), 100);
		assertEquals(tmcTimeStatisticImpl.getMinTime(), 0);
		assertEquals(tmcTimeStatisticImpl.getMaxTime(), 100);
	}
}
