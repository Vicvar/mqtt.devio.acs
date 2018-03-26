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
package alma.archive.tmcdb.Persistence.UnitTest;

import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import alma.archive.tmcdb.persistence.TMCDBConfig;
import alma.archive.tmcdb.persistence.TMCDBPersistence;

public class PersistenceUtilTest {

    TMCDBConfig config = null;
    TMCDBPersistence tmcdbPersistence=null;

    @BeforeClass(groups = {"persistenceUtil"})
    public void setUp() {
    	Logger logger = Logger.getAnonymousLogger();
    	config = TMCDBConfig.getInstance(logger);
    }

    @Test(groups = {"persistenceUtil"})
    public void tmcdbConfigTest() {
        assert (config.getConfigurationName().equalsIgnoreCase("test"));
        assert (config.getDbUser().equalsIgnoreCase("sa"));
        assert (config.getDbType().equals(TMCDBConfig.DBType.HSQLDB));
    }
}