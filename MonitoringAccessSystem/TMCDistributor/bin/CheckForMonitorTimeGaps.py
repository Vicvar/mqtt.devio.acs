#!/usr/bin/python 
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2009
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
# "@(#) $Id: CheckForMonitorTimeGaps.py,v 1.6 2015/12/04 08:00:00 pmerino Exp $"
#
# who       when      what
# --------  --------  ----------------------------------------------
# pmerino     2015-12-04 First implementation
#

from optparse import OptionParser
import tmUtils as tm
import os,sys
import numpy
import time
from dateutil import rrule
from datetime import datetime,timedelta
from calendar import monthrange
import pickle
import getpass
import smtplib
username = getpass.getuser()
sys.path.append('/users/%s/AIV/science/analysis_scripts/'%username)

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'

def get_monitor_list():
    monList= []
    monList.append(['Mount','METR_EQUIP_STATUS'])
    monList.append(['Mount','METR_TEMPS_00'])
    monList.append(['Mount','METR_TEMPS_01'])
    monList.append(['Mount','METR_TEMPS_02'])
    monList.append(['Mount','METR_TEMPS_03'])
    monList.append(['Mount','METR_TEMPS_04'])
    monList.append(['Mount','METR_TEMPS_05'])
    monList.append(['Mount','METR_TEMPS_06'])
    monList.append(['Mount','METR_TEMPS_07'])
    monList.append(['Mount','METR_TEMPS_08'])
    monList.append(['Mount','METR_TEMPS_09'])
    monList.append(['Mount','METR_TEMPS_0A'])
    monList.append(['Mount','METR_TEMPS_0B'])
    monList.append(['Mount','METR_TEMPS_0C'])
    monList.append(['Mount','METR_TEMPS_0D'])
    monList.append(['Mount','METR_TEMPS_0E'])
    monList.append(['Mount','METR_TEMPS_0F'])
    monList.append(['Mount','METR_TEMPS_10'])
    monList.append(['Mount','METR_TEMPS_11'])
    monList.append(['Mount','METR_TEMPS_12'])
    monList.append(['Mount','METR_TEMPS_13'])
    monList.append(['Mount','METR_TEMPS_14'])
    monList.append(['Mount','METR_TEMPS_15'])
    monList.append(['Mount','METR_TEMPS_16'])
    monList.append(['Mount','METR_TEMPS_17'])
    monList.append(['Mount','METR_TEMPS_18'])
    monList.append(['Mount','METR_TEMPS_19'])
    monList.append(['Mount','METR_TEMPS_1A'])
    monList.append(['Mount','METR_TEMPS_1B'])
    monList.append(['Mount','METR_TEMPS_1C'])
    monList.append(['Mount','METR_TEMPS_1D'])
    monList.append(['Mount','METR_TEMPS_1E'])
    monList.append(['Mount','METR_TEMPS_1F'])
    monList.append(['Mount','METR_TEMPS_20'])
    monList.append(['Mount','METR_TEMPS_21'])
    monList.append(['Mount','METR_TEMPS_22'])
    monList.append(['Mount','METR_TEMPS_23'])
    monList.append(['Mount','METR_TEMPS_24'])
    monList.append(['Mount','METR_TEMPS_25'])
    monList.append(['Mount','METR_TEMPS_26'])
    monList.append(['Mount','METR_TEMPS_27'])
    monList.append(['Mount','METR_TEMPS_28'])
    monList.append(['Mount','METR_TEMPS_29'])
    monList.append(['Mount','METR_TEMPS_2A'])
    monList.append(['Mount','METR_TEMPS_2B'])
    monList.append(['Mount','METR_TEMPS_2C'])
    monList.append(['Mount','METR_TEMPS_2D'])
    monList.append(['Mount','METR_TEMPS_2E'])
    monList.append(['Mount','METR_TEMPS_2F'])
    monList.append(['Mount','METR_TEMPS_30'])
    monList.append(['Mount','METR_TEMPS_31'])
    monList.append(['Mount','METR_TEMPS_32'])
    monList.append(['Mount','METR_TEMPS_33'])
    monList.append(['Mount','METR_TEMPS_34'])
    monList.append(['Mount','METR_TEMPS_35'])
    monList.append(['Mount','METR_TEMPS_36'])
    monList.append(['Mount','METR_TEMPS_37'])
    monList.append(['Mount','METR_TEMPS_38'])
    monList.append(['Mount','METR_TEMPS_39'])
    monList.append(['Mount','METR_TEMPS_3A'])
    monList.append(['Mount','METR_TEMPS_3B'])
    monList.append(['Mount','METR_TEMPS_3C'])
    monList.append(['Mount','METR_TEMPS_3D'])
    monList.append(['Mount','METR_TEMPS_3E'])
    monList.append(['Mount','METR_TEMPS_3F'])
    monList.append(['Mount','METR_TEMPS_40'])
    monList.append(['Mount','METR_TEMPS_41'])
    monList.append(['Mount','METR_TEMPS_42'])
    monList.append(['Mount','METR_TEMPS_43'])
    monList.append(['Mount','METR_TEMPS_44'])
    monList.append(['Mount','METR_TEMPS_45'])
    monList.append(['Mount','METR_TEMPS_46'])
    monList.append(['Mount','METR_TEMPS_47'])
    monList.append(['Mount','METR_TEMPS_48'])
    monList.append(['Mount','METR_TEMPS_49'])
    monList.append(['Mount','METR_TEMPS_4A'])
    monList.append(['Mount','METR_TEMPS_4B'])
    monList.append(['Mount','METR_TEMPS_4C'])
    monList.append(['Mount','METR_TEMPS_4D'])
    monList.append(['Mount','METR_TEMPS_4E'])
    monList.append(['Mount','METR_TEMPS_4F'])
    monList.append(['FrontEnd_ACD','ARM2'])
    monList.append(['FrontEnd_Cryostat','SERIAL_NUMBER'])
    monList.append(['IFProc0','FIFO_DEPTHS'])
    monList.append(['PSA','ALIVE_COUNTER'])
    monList.append(['PSD','ALIVE_COUNTER'])
    monList.append(['LLC','VF_MON'])
    return monList

def get_ant_list():
    return ['CM01', 'CM02', 'CM03', 'CM04', 'CM05', 'CM06', 'CM07', 'CM08', 'CM09', 'CM10', 'CM11', 'CM12',
	'DA41', 'DA42', 'DA43', 'DA44', 'DA45', 'DA46', 'DA47', 'DA48', 'DA49', 'DA50', 'DA51', 'DA52', 'DA53', 'DA54', 'DA55', 'DA56', 'DA57', 'DA58', 'DA59', 'DA60', 'DA61', 'DA62', 'DA63', 'DA64', 'DA65', 'DA66',
	'DV01', 'DV02', 'DV03', 'DV04', 'DV05', 'DV06', 'DV07', 'DV08', 'DV09', 'DV10', 'DV11', 'DV12', 'DV13', 'DV14', 'DV15', 'DV16', 'DV17', 'DV18', 'DV19', 'DV20', 'DV21', 'DV22', 'DV23', 'DV24', 'DV25',
	'PM01', 'PM02', 'PM03', 'PM04']

def system_except(cmd):
    ret = os.system(cmd)
    if ret != 0:
        raise Exception("Command '%s' returned non-zero exit status %d" %(cmd, ret))

def find(percentageLostArray, limit):
    for element in percentageLostArray:
        if element > limit:
            return element
    return None

def median(lst):
    return numpy.median(numpy.array(lst))

if __name__ == '__main__':
    usage = '''

    '''
    parser = OptionParser()

    parser.add_option("-f", "--from", dest="fr",
                      help="date you want to start your check. format YYYY-MM-DDTHH:MM:SS ")

    parser.add_option("-t", "--to", dest="to",
                      help="date you want to stop your check on.format YYYY-MM-DDTHH:MM:SS ")

    parser.add_option("-o", "--operational", dest="op",
                      help="percentage that the control subsystem was operational from -f to -t ")

    (options, args) = parser.parse_args()

    now = datetime.now()
    today = ('%s'%now)[0:10]
    yesterday = ('%s'%(now - timedelta(days=1)))[0:10]
    start='%sT00:00:00'%yesterday
    end='%sT23:59:59'%yesterday
    
    if options.fr is None:
        day1 = start
    else:
        day1 = options.fr 
    if options.to is None:
        day2 = end
    else:
        day2 = options.to
    if options.op is None:
        operational = "100"
    else:
        operational = options.op
    print '### Checking monitoring time gaps from: %s to: %s'%(day1,day2)
    
    monList = get_monitor_list()
    antList = get_ant_list()
    percentLost = []
    ngaps = []
    monName = []
    count = 0
    for ant in antList:
        for mon in monList:
            print count, ant, mon
            monName.append('%s_%s_%s'%(ant,mon[0],mon[1]))
            a,c =  tm.check_for_time_gaps(ant,mon[0],mon[1], startdate= day1, enddate=day2)
            if (a!=None and c!=None):
              ngaps.append(a)
              percentLost.append(c)
            print ""
            count += 1

    print '\n'
    print '\n'
    print '\n'
    print '### Final results over %d monitor points and %d antennas ###'%(len(monList),len(antList))
    q = find(percentLost, 5.0)
    if q is None:
        print day1
        print 'No gaps > 5%% in this list of monitor points from %s to %s'%(day1,day2)
    else:
        print "Found a median of %3.1f%% data lost "%(median(percentLost))
        print "CONTROL subsystem was Operational %s percent of the total time from: %s to: %s"%(operational,day1,day2)
        msg = "MONITOR POINT\t\t\t%LOST\n"
    	msg = msg + "======================================\n"
    	print msg
        for i in range(len(percentLost)):
            if percentLost[i] > 5.0:                
                print bcolors.FAIL+'%30s %4.1f'%(monName[i],percentLost[i])+bcolors.ENDC
    		msg = msg + '%30s %4.1f\n'%(monName[i],percentLost[i])

    	if bool(msg and msg.strip()) is True:
    		sender = 'software@alma.cl'
    		receivers = ['software-notifications@alma.cl', 'ahales@alma.cl', 'tsawada@alma.cl']
		message = "From: Monitor Data Service <software@alma.cl>\nTo: Software Notifications <software-notifications@alma.cl>; Antonio Hales <ahales@alma.cl>; Tsuyoshi Sawada <tsawada@alma.cl>\nSubject: Report of Gaps in Monitor Data Service - Warnings\n\n\nThis is a daily report of Gaps in Monitor Data Service http://monitordata.osf.alma.cl\nFrom\t\t: %s\nTo\t\t: %s\nMonitor points\t: %s\nAntennas\t: %s\n\nThe following monitor points have more than 5 percent of data lost. Please take care of this situation because it could affect to the normal operations.\n\nCONTROL subsystem was Operational %s percent of the total time from: %s to: %s.\n\n%s."%(day1, day2, monList, antList, operational, day1, day2, msg)
    		try:
    			smtpObj = smtplib.SMTP('smtp.alma.cl')
    			smtpObj.sendmail(sender, receivers, message)
    			print "Successfully sent email"
    		except SMTPException:
    			print "Error: unable to send email"
