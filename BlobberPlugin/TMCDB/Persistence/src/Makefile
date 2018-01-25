#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2005 
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
# "@(#) $Id: Makefile,v 1.5 2011/11/23 15:13:58 rtobar Exp $"
#
# Makefile for buiding Persistence Layer for TMCDB 
#
# who       when      what
# --------  --------  ----------------------------------------------
# pburgos  2009-04-20  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

# Jarfiles and their directories
#
JARFILES= TMCDBPersistenceLayer
TMCDBPersistenceLayer_DIRS= alma
TMCDBPersistenceLayer_EXTRAS= META-INF/ alma/archive/tmcdb/persistence/TMCDB-named-queries.xml

#
# java sources in Jarfile on/off
DEBUG= on

ACSERRDEF = 
# 
# IDL Files and flags
# 
IDL_FILES = 
TMCDBBase_IDLStubs_LIBS = 
TMCDB_IDLStubs_LIBS = 

# This is a workaround that can be removed with ACS 6.0.3
CDB_SCHEMAS = #ControlDevice Don't forget to add this schema def somewhere on ARCHIVE_TMCDB

# Scripts (public and local)
# ----------------------------
SCRIPTS         = 

#
# other files to be installed
#----------------------------

INSTALL_FILES = ../lib/asm-1.5.3.jar ../lib/asm-attrs-1.5.3.jar ../lib/c3p0-0.9.5.2.jar ../lib/hibernate-entitymanager.jar ../lib/mchange-commons-java-0.2.11.jar

#
# list of all possible C-sources (used to create automatic dependencies)
# ------------------------------
CSOURCENAMES = \
	$(foreach exe, $(EXECUTABLES) $(EXECUTABLES_L), $($(exe)_OBJECTS)) \
	$(foreach rtos, $(RTAI_MODULES) , $($(rtos)_OBJECTS)) \
	$(foreach lib, $(LIBRARIES) $(LIBRARIES_L), $($(lib)_OBJECTS))

#
#>>>>> END OF standard rules

#
# INCLUDE STANDARDS
# -----------------

MAKEDIRTMP := $(shell searchFile include/acsMakefile)
ifneq ($(MAKEDIRTMP),\#error\#)
   MAKEDIR := $(MAKEDIRTMP)/include
   include $(MAKEDIR)/acsMakefile
endif

#
# TARGETS
# -------
all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all
	@echo " . . . 'clean' done"

clean_dist:     clean_all clean_dist_all
	@echo " . . . clean_dist done"

	
man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"



#___oOo___
