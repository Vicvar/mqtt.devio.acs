# $Id: Makefile,v 1.7 2011/01/24 13:40:08 tstaig Exp $ 
#
# Copyright (C) 2003, 2004
# Associated Universities, Inc. Washington DC, USA.
#
# Produced for the ALMA project
#
# This library is free software; you can redistribute it and/or modify it 
# under the terms of the GNU Library General Public License as published by 
# the Free Software Foundation; either version 2 of the License, or (at your
# option) any later version.
#
# This library is distributed in the hope that it will be useful but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Library General Public 
# License for more details.
#
# You should have received a copy of the GNU Library General Public License 
# along with this library; if not, write to the Free Software Foundation, 
# Inc., 675 Massachusetts Ave, Cambridge, MA, 02139, USA.
#
# Correspondence concerning ALMA should be addressed as follows:
# Internet email: alma-sw-admin@nrao.edu
#
#

SUBSYSTEM = "ARCHIVE/TMCDB"

GROUPS = 

MODULES = Database Persistence DAO


# --------------- Standard Makefile Beyond this Point -----------------
# If option KEEP_GOING=on is present in the make command line gnu_make
# is NOT interrupted when the first error is encountered

KEEP_GOING = 1
# This variable is always defined so that NRI will always build all
# modules. This hack should be removed when NRI invokes this makefile
# with this variable defined. 

ifdef KEEP_GOING
   KEEP_GOING="on"
else
   KEEP_GOING="off"
endif

RETURN_CODE=return_code
TMP_RETURN_CODE=tmp_return_code

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

#
# "Failed all" error management
#
define mng_failed_all
	if [[ -a  $(TMP_RETURN_CODE) ]]; then\
		$(ECHO) "### ==> FAILED all ! " | tee -a build.log | tee -a $(RETURN_CODE);\
		rm $(TMP_RETURN_CODE);\
		if [[ $(KEEP_GOING) = "off" ]]; then \
			if [[ -a $(RETURN_CODE) ]]; then \
				rm $(RETURN_CODE);\
			fi;\
			exit 2;\
		fi;\
	fi 
endef

#
# "Failed install" error management
#
define mng_failed_install
	if [[ -a  $(TMP_RETURN_CODE) ]]; then\
		$(ECHO) "### ==> FAILED install ! " | tee -a build.log | tee -a $(RETURN_CODE);\
		rm $(TMP_RETURN_CODE);\
		if [[ $(KEEP_GOING) = "off" ]]; then \
			if [[ -a $(RETURN_CODE) ]]; then \
				rm $(RETURN_CODE);\
			fi;\
			exit 2;\
		fi;\
	fi 
endef


#
# This target just forward any make target to all modules
#
define canned
	@$(ECHO) "############ Executing '$@' on all $(SUBSYSTEM) modules #################"
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} $@ | tee -a build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    $(ECHO) "############ $${member}" ;\
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/src/ $@ || break ;\
		    fi;\
		done
endef

#
# This target just forward any make target to the test directory in all modules
#
define testcanned
	@$(ECHO) "############ Executing '$@' on all $(SUBSYSTEM) test modules #################"
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} $@ | tee -a build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES_TEST), $(name) ) ; do \
		    $(ECHO) "############ $${member}" ;\
		    if [ ! -d $${member}/test ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/test/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/test/ $@ || break ;\
		    fi;\
		done
endef

clean_log:
	@$(ECHO) "############ Clean Build Log File: build.log #################"
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} $@ | tee -a build.log;\
			continue ;\
		fi;\
	done
	@rm -f build.log
	@touch build.log

#
# building all modules
#
build:	
	@$(ECHO) "############ build $(SUBSYSTEM) Software         #################"| tee -a build.log
	@# Deletion of temporary files used to store make return code
	@if [[ -a $(TMP_RETURN_CODE) ]]; then \
		rm $(TMP_RETURN_CODE);\
	fi
	@if [[ -a $(RETURN_CODE) ]]; then \
		rm $(RETURN_CODE);\
	fi
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(RM) $${group}/build.log;\
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} build | tee -a build.log;\
			cat $${group}/build.log >> build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
			echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ clean >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ all >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		    if [ -f $${member}/Makefile ]; then \
			 $(ECHO) "############ $${member} External" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/ clean >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/ all >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/ install >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		done

#
# rebuilding all modules
#
rebuild:	
	@$(ECHO) "############ rebuild $(SUBSYSTEM) Software       #################"| tee -a build.log
	@# Deletion of temporary files used to store make return code
	@if [[ -a $(TMP_RETURN_CODE) ]]; then \
		rm $(TMP_RETURN_CODE);\
	fi
	@if [[ -a $(RETURN_CODE) ]]; then \
		rm $(RETURN_CODE);\
	fi
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(RM) $${group}/build.log;\
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} rebuild | tee -a build.log;\
			cat $${group}/build.log >> build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
			echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ all >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		    if [ -f $${member}/Makefile ]; then \
			 $(ECHO) "############ $${member} External" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/ all >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/ install >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		done
#
# building all RTAI modules
#
build_rtai:	
	@$(ECHO) "############ build RTAI $(SUBSYSTEM) Software         #################"| tee -a build.log
	@# Deletion of temporary files used to store make return code
	@if [[ -a $(TMP_RETURN_CODE) ]]; then \
		rm $(TMP_RETURN_CODE);\
	fi
	@if [[ -a $(RETURN_CODE) ]]; then \
		rm $(RETURN_CODE);\
	fi
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(RM) $${group}/build.log;\
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} build_rtai | tee -a build.log;\
			cat $${group}/build.log >> build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
			echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ clean_rtai >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ directory_structure >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ do_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		    if [ -f $${member}/Makefile ]; then \
			 $(ECHO) "############ $${member} External" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ clean_rtai >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ directory_structure >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ do_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		done

#
# rebuilding all RTAI modules
#
rebuild_rtai:	
	@$(ECHO) "############ rebuild RTAI $(SUBSYSTEM) Software       #################"| tee -a build.log
	@# Deletion of temporary files used to store make return code
	@if [[ -a $(TMP_RETURN_CODE) ]]; then \
		rm $(TMP_RETURN_CODE);\
	fi
	@if [[ -a $(RETURN_CODE) ]]; then \
		rm $(RETURN_CODE);\
	fi
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(RM) $${group}/build.log;\
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} rebuild_rtai | tee -a build.log;\
			cat $${group}/build.log >> build.log;\
			continue ;\
		fi;\
	done
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
			echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ directory_structure >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ do_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		    if [ -f $${member}/Makefile ]; then \
			 $(ECHO) "############ $${member} External" | tee -a build.log;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ directory_structure >> build.log 2>& 1;\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ do_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_all);\
			 $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install_rtai >> build.log 2>& 1 || echo $$? >> $(TMP_RETURN_CODE) ;\
			 $(mng_failed_install);\
			 continue ;\
		    fi;\
		done

#
# Test target
#

.PHONY: test

Test = test
$(Test):
	@rm -f test.log
	@touch test.log
	@$(ECHO) "############ TEST $(SUBSYSTEM) Software #################"| tee -a test.log
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			echo "### ==> $${group} SUBDIRECTORY NOT FOUND! FAILED! " | tee -a build.log;\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} $@ | tee -a build.log;\
			continue ;\
		fi;\
	done
	@for member in $(foreach name,$(MODULES_TEST),$(name)); do\
		if [ -d $${member}/test ]; then\
			$(ECHO) "############ $${member}/test MAIN TEST ############" | tee -a test.log ;\
			$(MAKE) -k -C $${member}/test/ $@ | tee -a test.log | egrep '(Nothing to|FAILED.|PASSED.|Error:)';\
		else\
			$(ECHO) "### ==> $${member} TEST DIRECTORY STRUCTURE NOT FOUND! FAILED!" | tee -a test.log ;\
		fi;\
	done

#
# show_modules target
#
# Simply lists all MODULES that would be build
# with the current setup
#
show_modules:
	@for group in  $(foreach name, $(GROUPS), $(name) ) ; do \
		if [ ! -d $${group} ]; then \
			$(ECHO) "$${group} SUBDIRECTORY NOT FOUND! FAILED! ";\
		fi;\
		if [ -f $${group}/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -s -C $${group} show_modules;\
			continue ;\
		fi;\
	done; \

	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		$(ECHO) "$(SUBSYSTEM)/$${member}";\
	done

##

#
# Standard canned targets
#
clean:
	$(canned)
	$(testcanned)
	$(RM) build.log test.log return_code *~

clean_dist:
	$(canned)
	$(testcanned)
	$(RM) build.log test.log return_code *~

all:	
	$(canned)
install:
	$(canned)

man:
	$(canned)

buildClean: build clean

buildMan: build man
