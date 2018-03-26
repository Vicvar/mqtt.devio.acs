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
package archive.tmcdb.monitoring.TMCOffline;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Reads the configuration properties
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCProperties {
    /** The properties */
    private static Properties properties = new Properties();

    /** The logger */
    private static final Logger logger = Logger.getLogger(TMCProperties.class);

    /**
     * Constructor
     *
     * @param configurationFile The configuration file
     */
    public TMCProperties(String configurationFile) {
        try {
            if (logger.isInfoEnabled())
                logger.info("Configuration file:" + configurationFile);

            String configFile = System.getenv(configurationFile);

            if (logger.isInfoEnabled())
                logger.info("Config file:" + configFile);

            properties.load(new FileInputStream(configFile));

            if (properties.stringPropertyNames() == null || properties.stringPropertyNames().isEmpty())
                logger.error("Error loading the configuration file");
        }
        catch (IOException ex) {
            logger.error("Problems tryin to read the configuration file");
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Get the properties
     *
     * @return The properties
     */
    public static Properties getProperties() {
        return properties;
    }
}