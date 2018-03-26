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
package archive.tmcdb.monitoring.TMCOffline.test;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

import archive.tmcdb.monitoring.TMCOffline.TMCTimeConverter;

import java.util.Date;

/**
 * Test for the TMCTimeConverter class
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCTimeConverterTest {
    @Before
    public void setUp() {
    }

    @Test
    public void testToAcstime() {
        long acstimeExpected = 134815967990000000L;
        String dateWithFormat = "2010-01-01T00:00:00";
        long acstimeResult = TMCTimeConverter.toAcstime(dateWithFormat);
        System.out.println("acstimeResult=" + acstimeResult);
        assertEquals(acstimeExpected, acstimeResult);
    }

    @Test
    public void toDateReadableFormat() {
        long acstime = 136235199002821190L;
        String dateReadableFormatExpected = "2014-07-01T15:05:00";
        String dateReadableFormatResult = TMCTimeConverter.toDateReadableFormat(acstime);
        System.out.println("dateReadableFormatResult=" + dateReadableFormatResult);
        assertEquals(dateReadableFormatExpected, dateReadableFormatResult);
    }

    @Test
    public void toDateReadableFormatMillisecond() {
		long acstime1 = 136235199002821190L;
        String dateReadableFormatMillisecondExpected1 = "2014-07-01T15:05:00.282118";
        String dateReadableFormatMillisecondResult1 = TMCTimeConverter.toDateReadableFormatMillisecond(acstime1);
        System.out.println("dateReadableFormatMillisecondResult1=" + dateReadableFormatMillisecondResult1);
        assertEquals(dateReadableFormatMillisecondExpected1, dateReadableFormatMillisecondResult1);

		long acstime2 = 136235199001640980L;
		String dateReadableFormatMillisecondExpected2 = "2014-07-01T15:05:00.164098";
		String dateReadableFormatMillisecondResult2 = TMCTimeConverter.toDateReadableFormatMillisecond(acstime2);
		System.out.println("dateReadableFormatMillisecondResult2=" + dateReadableFormatMillisecondResult2);
		assertEquals(dateReadableFormatMillisecondExpected2, dateReadableFormatMillisecondResult2);
    }

    @Test
    public void toDateReadableFormatMillisecondShort() {
        long acstime = 136235199002821190L;
		String dateReadableFormatMillisecondShortExpected = "2014-07-01T15:05:00.284";
        String dateReadableFormatMillisecondShortResult = TMCTimeConverter.toDateReadableFormatMillisecondShort(acstime);
        System.out.println("dateReadableFormatMillisecondShortResult=" + dateReadableFormatMillisecondShortResult);
        assertEquals(dateReadableFormatMillisecondShortExpected, dateReadableFormatMillisecondShortResult);
    }
}
