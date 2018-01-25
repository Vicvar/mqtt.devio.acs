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
# "@(#) $Id: Makefile,v 1.10 2012/10/24 15:00:28 rtobar Exp $"
#
# Makefile for buiding DDL for TMCDB 
#
# who       when      what
# --------  --------  ----------------------------------------------
# pburgos  2009-04-05  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

# Jarfiles and their directories
#
JARFILES=TMCDB_HD_plugin
TMCDB_HD_plugin_DIRS=com/cosylab/cdb/jdal/hibernate/plugin
TMCDB_HD_plugin_EXTRAS=cdb_rdb-hibernate.cfg.xml

DDLDATA=$(ACSDATA)/config/DDL
HSQLDB_DIR=$(ACSDATA)/config/DDL/hsqldb

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
INSTALL_FILES = ../lib/TMCDBpojos.jar

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

MODELFILE=TMCDB_hwconfigmonitoring

all:	.done_generating_sql ../lib/TMCDBhwconfigmonitoringStrategy.jar .done_generating_classes ../lib/TMCDBpojos.jar do_all
	@echo " . . . 'all' done" 

clean: clean_all
	@rm -rf ../bin/$(MODELFILE)
	@rm -f .done_generating_sql
	@rm -f .done_generating_classes
	@rm -rf tmcdb
	@rm -rf gen
	@rm -f CreateHsqldbTables.sql
	@rm -f alma/acs/tmcdb/translator/Table2Class_*
	@rm -f alma/acs/tmcdb/translator/Column2Attribute_*
	@rm -f alma/acs/tmcdb/translator/TableInheritance_*
	@rm -f ../lib/TMCDBpojos.jar
	@rm -f ../lib/TMCDBhwconfigmonitoringStrategy.jar
	@rm -rf ../config/TMCDB_hwconfigmonitoring
	@echo " . . . 'clean' done"

man   : do_man 
	@echo " . . . man page(s) done"

install: install_all
	@mkdir -p $(DDLDATA)/generic
	@mkdir -p $(DDLDATA)/oracle/TMCDB_hwconfigmonitoring
	@mkdir -p $(DDLDATA)/hsqldb/TMCDB_hwconfigmonitoring
	@echo "== Copying generic .ddl files to $(DDLDATA)/generic"
	@cp generic/TMCDB_hwconfigmonitoring.ddl $(DDLDATA)/generic
	@echo "== Copying generated Oracle .sql files to $(DDLDATA)/oracle"
	@cp ../config/TMCDB_hwconfigmonitoring/oracle/* $(DDLDATA)/oracle/TMCDB_hwconfigmonitoring
	@echo "== Copying generated HSQLDB .sql files to $(DDLDATA)/oracle"
	@cp ../config/TMCDB_hwconfigmonitoring/hsqldb/* $(DDLDATA)/hsqldb/TMCDB_hwconfigmonitoring
	@echo " . . . installation done"

.done_generating_sql: generic/TMCDB_hwconfigmonitoring.ddl
	@echo "=="
	@echo "== Generating SQL code"
	@echo "=="
	@CLASSPATH=$$CLASSPATH:$(DDLDATA) generateTmcdbSchemas generic/$(MODELFILE).ddl ../config
	@echo "=="
	@echo "== Generating SQL/Java translation code"
	@echo "=="
	@CLASSPATH=$$CLASSPATH:$(DDLDATA) generateTmcdbHibernateStrategy generic/TMCDB_hwconfigmonitoring.ddl .
	@touch .done_generating_sql

.done_generating_classes:
	@echo "=="
	@echo "== Generating Java domain classes"
	@echo "=="
	@-mkdir tmcdb
	@rm -f CreateHsqldbTables.sql
	@echo "Gathering SQL pieces from $(HSQLDB_DIR)"
	@cat $(HSQLDB_DIR)/TMCDB_swconfigcore/CreateHsqldbTables.sql $(HSQLDB_DIR)/TMCDB_swconfigext/CreateHsqldbTables.sql ../config/TMCDB_hwconfigmonitoring/hsqldb/CreateHsqldbTables.sql > CreateHsqldbTables.sql
	@acsStartJava org.hsqldb.cmdline.SqlTool --rcFile sqltool.rc tmcdb CreateHsqldbTables.sql
	#@CLASSPATH=""; ant -DJACORB_HOME=$(JACORB_HOME) generate
	ant -DJACORB_HOME=$(JACORB_HOME) generate
	@touch .done_generating_classes

../lib/TMCDBpojos.jar: .done_generating_classes
	@echo "=="
	@echo "== Compiling and packing generated domain classes"
	@cd gen; CLASSPATH=$(shell acsMakeJavaClasspath) javac alma/acs/tmcdb/*.java; \
	   cdbrdbPojosDir=$(shell searchFile lib/cdbrdb-pojos.jar); \
	   if [ -z "$$cdbrdbPojosDir" -o ! -d "$$cdbrdbPojosDir" ]; then echo "Cannot find cdbrdb-pojos.jar in ACSROOT or INTROOT"; exit -1; fi; \
	   cdbrdbPojos=$$cdbrdbPojosDir/lib/cdbrdb-pojos.jar; \
	   echo "== Will remove the classes already present in '$$cdbrdbPojos'"; \
	   for i in $$(unzip -qq -l $$cdbrdbPojos *.class *.java | awk '{print $$4}' | sed 's/^src\///'); do rm -f $$i; done; \
	   jar cf ../../lib/TMCDBpojos.jar alma/acs/tmcdb/*.class; \
	   cd ..; mv gen src; jar uf ../lib/TMCDBpojos.jar src/alma/acs/tmcdb/*.java; mv src/ gen; \
	   jar uf ../lib/TMCDBpojos.jar -C ../config/TMCDB_hwconfigmonitoring/ HwConfigMonitoring-orm.xml; \
	   echo "== ";

../lib/TMCDBhwconfigmonitoringStrategy.jar:
	@echo "== Compiling TMCDBhwconfigmonitoringStrategy.jar "
	@CLASSPATH=$(shell acsMakeJavaClasspath) javac alma/acs/tmcdb/translator/*.java; jar cf ../lib/TMCDBhwconfigmonitoringStrategy.jar alma/acs/tmcdb/translator/*.class; cd ..; jar uf lib/TMCDBhwconfigmonitoringStrategy.jar src/alma/acs/tmcdb/translator/*.java;
	@rm -f alma/acs/tmcdb/translator/*.class
#___oOo___
