#! /usr/bin/env python
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# Copyright (c) Associated Universities Inc., 2017 
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
#
# who       when      what
# --------  --------  ----------------------------------------------
# ruben.soto  2017-06-12  created
#

#************************************************************************
#   NAME
# 
#   SYNOPSIS
# 
#   DESCRIPTION
#
#   FILES
#
#   ENVIRONMENT
#
#   RETURN VALUES
#
#   CAUTIONS
#
#   EXAMPLES
#
#   SEE ALSO
#
#   BUGS     
#
#------------------------------------------------------------------------
#

# Import the acspy.PySimpleClient class
import time
from Acspy.Clients.SimpleClient import PySimpleClient


# Make an instance of the PySimpleClient
simpleClient = PySimpleClient()

# Print information about the available COBs

# Do something on a device.
simpleClient.getLogger().logInfo("We can directly manipulate a device once we get it, which is easy.")
try:
    # Get the standard MOUNT1 Mount device
    ws = simpleClient.getComponent("MONITOR_COLLECTOR")

    # Get the humidity
    ws.registerMonitoredDevice("SensorTag","1")
    # Print value 
    ws.startMonitoring("SensorTag");
    raw_input("##################Press enter to quit#########################")
    # Release it
    simpleClient.releaseComponent("MONITOR_COLLECTOR")

except Exception, e:
    simpleClient.getLogger().logCritical("Sorry, I expected there to be a Mount in the system and there isn't.")
    simpleClient.getLogger().logDebug("The exception was:" + str(e))
simpleClient.disconnect()
print "The end __oOo__"
#
# ___oOo___
