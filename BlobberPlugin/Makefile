#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2003 
#
#This library is free software; you can redistribute it and/or
#modify it under the terms of the GNU Lesser General Public
#License as published by the Free Software Foundation; either
#version 2.1 of the License, or (at your option) any later version.
#
#This library is distributed in the hope that it will be useful,
#but WITHOUT ANY WARRANTY; without even the implied warranty of
#MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#Lesser General Public License for more details.
#
#You should have received a copy of the GNU Lesser General Public
#License along with this library; if not, write to the Free Software
#Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
# "@(#) $Id: Makefile,v 1.83 2013/03/14 14:04:21 amanning Exp $"
#
#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#******************************************************************************* 

###############################################
# Macro definitions.                          #
###############################################
define makeIt
   (( /usr/bin/time -f "$1 COMPILATION TIME %E" make $(MAKE_FLAGS) -C $1 $2 2>&1 ) || ( echo "### ==> FAILED $2 ! " | tee -a $3 $4 1>&2 )) | tee -a $3 $4 >/dev/null;
endef

define makeItAux
   (( make $(MAKE_FLAGS) -C $1 $2 2>&1 ) || ( echo "### ==> FAILED $2 ! " | tee -a $3 $4 1>&2 )) | tee -a $3 $4 >/dev/null;
endef
###############################################

SUBSYSTEM = ARCHIVE


MODULES =   Database TMCDB/Persistence TMCDB/Database TMCDB/DAO  
#MODULES_TEST = TMCDB/Database TMCDB/Persistence TMCDB/DAO 

MAKE_FLAGS = "-k"
PLATFORM := $(shell uname)

SHELL=/bin/ksh
ECHO=echo

ifdef MAKE_VERBOSE
    AT = 
    OUTPUT =
else
    AT = @
    OUTPUT = > /dev/null
endif
#
os     = $(shell uname)
osrev  = $(shell uname -r)

ifeq ($(os),SunOS)
	realtime=YES
endif

#
# This target just forward any make target to all modules
#

define canned
	@$(ECHO) "############ Executing '$@' on all $(SUBSYSTEM) modules #################"
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    $(ECHO) "############ $${member}" ;\
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/src/ $@ || break ;\
		    elif [ -f $${member}/ws/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ $@ || break ;\
		    fi;\
		    if [ "$(realtime)" == "YES" ]; then \
			if [ -f $${member}/lcu/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ $@ || break ;\
			fi;\
		    fi;\
		done
endef

clean_log:
	@$(ECHO) "############ Clean Build Log File: build.log #################"
	@rm -f build.log
	@touch build.log

#
# building all modules
#
build:	clean_log
	@$(ECHO) "############ build $(SUBSYSTEM) Software         #################"| tee -a build.log
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log; \
                         sh acsBUILDCheckModuleTree.sh $${member} >> build.log 2>& 1; \
                         $(call makeItAux,$${member}/src,clean,build.log,$${member}/src/NORM-BUILD-OUTPUT) \
                         $(call makeIt,$${member}/src,all,build.log,$${member}/src/NORM-BUILD-OUTPUT) \
                         $(call makeItAux,$${member}/src,install,build.log,$${member}/src/NORM-BUILD-OUTPUT) \
                         continue ;\
		    fi;\
		    if [ -f $${member}/ws/src/Makefile ]; then \
		    $(ECHO) "############ $${member} WS" | tee -a build.log;\
                         $(call makeItAux,$${member}/ws/src,clean,build.log,$${member}/ws/src/NORM-BUILD-OUTPUT) \
                         $(call makeIt,$${member}/ws/src,all,build.log,$${member}/ws/src/NORM-BUILD-OUTPUT) \
                         $(call makeItAux,$${member}/ws/src,install,build.log,$${member}/ws/src/NORM-BUILD-OUTPUT) \
		    fi;\
		    if [ "$(realtime)" == "YES" ]; then \
			if [ -f $${member}/lcu/src/Makefile ]; then \
			    $(ECHO) "############ $${member} LCU" | tee -a build.log;\
                         $(call makeItAux,$${member}/lcu/src,clean,build.log,$${member}/lcu/src/NORM-BUILD-OUTPUT) \
                         $(call makeIt,$${member}/lcu/src,all,build.log,$${member}/lcu/src/NORM-BUILD-OUTPUT) \
                         $(call makeItAux,$${member}/lcu/src,install,build.log,$${member}/lcu/src/NORM-BUILD-OUTPUT) \
			fi;\
		    fi;\
		done

		@if [ "X$(INTROOT)" != X ] ; \
                then \
                        INSTROOT="$(INTROOT)"; \
                else \
                        INSTROOT="$(ACSROOT)"; \
                fi; \
                if [ -f "ARCHIVE_TAG" ]; \
                then \
                        echo "ARCHIVE_TAG is:"; \
                        cat ARCHIVE_TAG; \
                        cp ARCHIVE_TAG $${INSTROOT}; \
                else; \
                        echo "WARNING: No ARCHIVE_TAG available"; \
                        echo "WARNING: No ACS_TAG available" > $${INSTROOT}/ARCHIVE_TAG; \
                fi;
	@$(ECHO) "... done"

#
# Test target
#

.PHONY: test

Test = test
$(Test):
	@$(ECHO) "############ Clean Test Log File: test.log #################"
	@rm -f test.log
	@touch test.log
	@$(ECHO) "############ TEST $(SUBSYSTEM) Software #################"| tee -a test.log
	@for member in $(foreach name,$(MODULES_TEST),$(name)); do\
		if [ -d $${member}/ws/test ]; then\
			$(ECHO) "############ $${member}/ws/test WS TEST ############" | tee -a test.log ;\
			$(MAKE) -k -C $${member}/ws/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
			if [ -d $${member}/lcu/test ]; then\
				$(ECHO) "############ $${member}/lcu/test LCU TEST ############" | tee -a test.log;\
				$(MAKE) -k -C $${member}/lcu/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
			fi;\
		elif [ -d $${member}/test ]; then\
			$(ECHO) "############ $${member}/test MAIN TEST ############" | tee -a test.log ;\
			$(MAKE) -k -C $${member}/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
		else\
			$(ECHO) "### ==> $${member} TEST DIRECTORY STRUCTURE NOT FOUND! FAILED!" | tee -a test.log ;\
		fi;\
	done
	@$(ECHO) "... done"
#
# Standard canned targets
#
clean:	
	$(canned)
all:	
	$(canned)
install:
	$(canned)

man:
	$(canned)

#
# ___oOo___
