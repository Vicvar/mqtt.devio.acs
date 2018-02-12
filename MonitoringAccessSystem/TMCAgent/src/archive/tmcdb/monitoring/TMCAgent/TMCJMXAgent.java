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
package archive.tmcdb.monitoring.TMCAgent;

import java.lang.management.ManagementFactory;

import java.rmi.registry.LocateRegistry;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;

/**
 * Provide a JMX Agent for the application
 */
public class TMCJMXAgent {
    /** The logger */
    private static final Logger log = Logger.getLogger(TMCJMXAgent.class);

    /** The registry port */
    public static final String RMIREGISTRY_PORT = "archive.tmcdb.monitoring.rmiregistry.port";

    /** The server connection */
    public static final String RMISERVER_PORT = "archive.tmcdb.monitoring.rmiserver.port";

    /** The bean server */
    private static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    /**
     * Constructor
     */
    private TMCJMXAgent() {
    }
 
    /**
     * The premain method
     *
     * @param agentArgs
     */
    public static void premain(String agentArgs) throws Throwable {
        int rmiServerPort = Integer.parseInt(System.getProperty(RMISERVER_PORT));
        int rmiRegistryPort = Integer.parseInt(System.getProperty(RMIREGISTRY_PORT));
        String hostname = System.getProperty("java.rmi.server.hostname");
        LocateRegistry.createRegistry(rmiRegistryPort);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + rmiServerPort + "/jndi/rmi://" + hostname + ":" + rmiRegistryPort + "/jmxrmi");
        if (log.isDebugEnabled()) {
            log.debug("Local Connection URL: " + url);
            log.debug("Creating RMI connector server");
        }
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        cs.start();
    }

    /**
     * Permits register the MBean
     *
     * @param beanToRegister The bean to register
     * @param bName The bean name
     */
    public void registerMBean(Object beanToRegister, String bName) {
        try {
             if (log.isDebugEnabled()) {
                 log.debug("beanToRegister=" + beanToRegister);
                 log.debug("bName=" + bName);
             }
             ObjectName beanName = new ObjectName(bName);
             mbs.registerMBean(beanToRegister, beanName);
             if (log.isInfoEnabled())
                 log.info("Registered to MBean");
        }
        catch (Exception e) {
             log.error("Failed to register this MBean...");
             log.error(e);
        }
    }
}
