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

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

import archive.tmcdb.monitoring.TMCOffline.TMCDumper;
import archive.tmcdb.monitoring.TMCOffline.TMCProperties;

/**
 * Test for the TMCDumper class
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCDumperTest {
    private List<String> list;
    private TMCDumper tmcDumper;

    @Before
    public void setUp() {
        tmcDumper = new TMCDumper();
        list = new ArrayList<String>();
        list.add("2012-08-23 14:01:19|135650232291488630;135650232491597390;1345730403272;135650232032726500|724.0\n;724.0");
        list.add("2012-08-23 14:02:05|135650232891385110;135650233091488920;1345730463272;135650232632726500|724.0\n;724.0");
        tmcDumper.setDataList(list);
        String channelName = "TMCS:CONTROL/DV01/WVR:WVR_STATE_BOOTED";
        tmcDumper.setChannelName(channelName);
    }

    @Test
    public void testRun() {
    }

    @Test
    public void testGetDataListAsString() {
        String channelName = "";
        String expectedResult = "2012-08-23T14:00:03|724.0\n2012-08-23T14:01:03|724.0\n";
        String fileName = "";
        String result = tmcDumper.getDataListAsString(0, 0, fileName);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetStartTime() {
        String expectedResult = "2012-08-23";
        String result = tmcDumper.getStartTime((String) list.get(0));
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetFolderName() {
        String yearMonthDay = "2012-08-23";
        String expectedResult = "/var/opt/alma/monitordata/2014/08/2014-08-23/CONTROL_DV01_WVR";
        String result = tmcDumper.getFolderName(yearMonthDay);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetFileName() {
        String expectedResult = "WVR_STATE_BOOTED.txt";
        String result = tmcDumper.getFileName();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetFullFileName() {
        String fileName = "WVR_STATE_BOOTED.txt";
        String stringFolder = "/var/opt/alma/monitordata/2014/08/2014-08-23/CONTROL_DV01_WVR";
        String expectedResult = "/var/opt/alma/monitordata/2014/08/2014-08-23/CONTROL_DV01_WVR/WVR_STATE_BOOTED.txt";
        String result = tmcDumper.getFullFileName(stringFolder, fileName);
        assertEquals(expectedResult, result);
    }
}
