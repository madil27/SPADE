# SPADE - Support for Provenance Auditing in Distributed Environments.
# Copyright (C) 2015 SRI International.

JAVAC=javac
CC=gcc
JAR=jar
JAVAC_CP = build:lib/*:lib/neo4j-community-3.4.4/lib/*
JAVAC_OPTIONS =$(EXTRA_JAVAC_OPTIONS) -Xlint:none -proc:none
ANDROID_BUILD_TOOLS = 
ANDROID_SDK_TOOLS = 
AUDIT_KERNEL_MODULES_MAKEFILE = src/spade/reporter/audit/kernel-modules/Makefile

#
# In Ubuntu, when you install llvm headers, it puts llvm and llvm-c inside version folder.
# sudo ln -s  llvm-3.6/llvm/ llvm
# sudo ln -s  llvm-c-3.6/llvm-c/ llvm-c
#
LLVM_INCLUDE_PATH=/usr/include/llvm-3.6/
 
LLVM_PATH = /var/clang+llvm-3.0-x86_64-apple-darwin11
# LLVM_TARGET = nweb
 
# System and architecture-specific configuration
 
OS_NAME = $(shell uname)
OS_ARCH = $(shell uname -m)
OS_KERNEL_RELEASE = $(shell uname -r)
OS_KERNEL_MAJOR = $(shell uname -r | cut -d '.' -f 1)
OS_KERNEL_MINOR = $(shell uname -r | cut -d '.' -f 2 | xargs printf "%02d")
OS_KERNEL_NUMBER = $(OS_KERNEL_MAJOR)$(OS_KERNEL_MINOR)
ifeq ($(OS_NAME), Darwin)
	OS_BUILD = build-openbsm
	NO_FUSE = $(shell pkg-config fuse; echo $$?)
	ifeq (0, $(NO_FUSE))
   	 OS_BUILD += build-macfuse
	endif
	OS_CONFIG_PATH = /usr/local/lib/pkgconfig
	REPLIB_OSFLAG = -D_LLVMREPORTER_MACOSX
	LLVM_RULE = build-mac-llvm
endif
ifeq ($(OS_NAME), Linux)
	OS_BUILD = build-linuxaudit
	NO_FUSE = $(shell pkg-config fuse; echo $$?)
	ifeq (0, $(NO_FUSE))
   	 OS_BUILD += build-linuxfuse
	endif
	OS_CONFIG_PATH = /usr/lib/pkgconfig
	REPLIB_OSFLAG = -D_LLVMREPORTER_LINUX
	LLVM_RULE = build-linux-llvm
endif
ifneq (, $(findstring CYGWIN, $(OS_NAME)))
	JAVAC_CP = build;lib\*;lib\neo4j-community-3.4.4\lib\*
endif
 
# -------- Default Build Configuration -----
REPORTERS = Graphviz DSL Network LLVM Facebook Bitcoin StraceLinux JSON CDM CamFlow ProcMon
# -------------------------------------------
 
.PHONY: build prepare-dirs core reporters filters query analyzers clients utilities storages android-build

all:    build
	./bin/keys/generatekeys.sh
 
help:
	@echo 'Usage:'
	@echo '  	make                	- to compile the code'
	@echo '  	make clean          	- to remove the compiled code'
	@echo ' '
 
# ---------- Building SPADE ----------    
.SECONDEXPANSION:

build: | prepare-dirs
build: build-java $$(OS_BUILD) lib/spade.jar
build-java: download-neo4j core clients utilities storages filters query analyzers sketches transformers reporters

NEO4J_DIR=lib/neo4j-community-3.4.4
download-neo4j:
	@if [ -d $(NEO4J_DIR) ]; then \
		echo "Neo4j already exists"; \
	else \
		echo "Neo4j does not exist. Downloading now..."; \
		bin/downloadNeo4j \
		echo "--- Downloaded Neo4j and dependencies ---"; \
	fi

.SUFFIXES : .class .java
 
prepare-dirs:
	mkdir -p build
 
build/%.class : src/%.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build $<
 
core:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/core/*.java
	@echo "--- Built Core ---"
clients:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/client/*.java
	@echo "--- Built Clients ---"
reporters: $$(foreach f,$$(REPORTERS), build/spade/reporter/$$(f).class )
	@echo "--- Built Reporters ---"
query:
	mkdir -p tmp
	find src/spade/query -name "*.java" > tmp/query.classes
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build @tmp/query.classes
	rm tmp/query.classes
	@echo "--- Built Queries ---"
analyzers:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/analyzer/*.java
	@echo "--- Built Analyzers ---"
storages:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/storage/*.java
	@echo "--- Built Storages ---"
utilities:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/profile/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/cache/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/screen/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/store/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/store/db/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/store/db/berkeleydb/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/map/external/store/db/leveldb/*.java
	@echo "--- Built Utilities ---"
filters:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/filter/*.java
	@echo "--- Built Filters ---"
sketches:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/sketch/*.java
	@echo "--- Built Sketches ---"
transformers:
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/transformer/*.java
	@echo "--- Built Transformers ---"
 
lib/spade.jar: build-java $$(OS_BUILD)
	$(JAR) cvf lib/spade.jar -C build .

# Build the LinuxAudit reporter.
build-linuxaudit: build/spade/reporter/Audit.class bin/spadeAuditBridge bin/deleteModule audit-kernel-module
	@echo '-----> IMPORTANT: To use the LinuxAudit reporter, please run the following commands to allow SPADE access to the audit stream:'
	@echo '----->        	sudo chown root bin/spadeAuditBridge'
	@echo '----->        	sudo chmod ug+s bin/spadeAuditBridge'
	@echo ''
bin/spadeAuditBridge: src/spade/reporter/spadeAuditBridge.c
	gcc -o bin/spadeAuditBridge src/spade/reporter/spadeAuditBridge.c
bin/deleteModule: src/spade/reporter/audit/deleteModule.c
	gcc -o bin/deleteModule src/spade/reporter/audit/deleteModule.c
audit-kernel-module:
	@if [ "$(findstring Microsoft, $(OS_KERNEL_RELEASE))" = "" ]; then \
		if [ "$(KERNEL_MODULES)" = "true" ]; then \
			if [ -z "$(OS_KERNEL_NUMBER)" ]; then \
				echo 'ERROR: Kernel modules build failed. Failed to get current kernel release number.'; \
				exit 1; \
			else \
				if [ $(OS_KERNEL_NUMBER) -ge 417 ]; then \
					echo 'ERROR: Kernel modules not supported for the current kernel release.'; \
					exit 1; \
				else \
					$(MAKE) --no-print-directory -f $(AUDIT_KERNEL_MODULES_MAKEFILE); \
				fi \
			fi \
		else \
			$(MAKE) --no-print-directory -f $(AUDIT_KERNEL_MODULES_MAKEFILE) clean; \
		fi \
	else \
		echo 'Skipped kernel modules. OS Kernel not supported'; \
	fi

# Build the LinuxFUSE reporter.
build-linuxfuse: src/spade/reporter/spade_reporter_LinuxFUSE.h lib/libLinuxFUSE.so
	@echo ''
	@echo '-----> IMPORTANT: To use the LinuxFUSE reporter, please enable "user_allow_other" in /etc/fuse.conf'
	@echo ''
src/spade/reporter/spade_reporter_LinuxFUSE.h: build/spade/reporter/LinuxFUSE.class
	$(JAVAC) -classpath 'build:lib/*' -h src/spade/reporter src/spade/reporter/LinuxFUSE.java
lib/libLinuxFUSE.so: src/spade/reporter/libLinuxFUSE.c utilities
	export PKG_CONFIG_PATH=$(OS_CONFIG_PATH);\
	gcc -fPIC -shared -Wl,-soname,libLinuxFUSE.so -I$(shell java -classpath build spade.utility.JavaHome) -I$(shell java -classpath build spade.utility.JavaHome)/linux -Wall src/spade/reporter/libLinuxFUSE.c `pkg-config fuse --cflags --libs` -o lib/libLinuxFUSE.so
 
# Build the OpenBSM reporter.
build-openbsm: build/spade/reporter/OpenBSM.class src/spade/reporter/spadeOpenBSM.c lib/spadeOpenBSM
	@echo '-----> IMPORTANT: To use the OpenBSM reporter, please run the following commands to allow SPADE access to the audit stream:'
	@echo '----->        	sudo chown root lib/spadeOpenBSM'
	@echo '----->        	sudo chmod ug+s lib/spadeOpenBSM'
	@echo ''
lib/spadeOpenBSM: src/spade/reporter/spadeOpenBSM.c
	gcc -o lib/spadeOpenBSM -lbsm src/spade/reporter/spadeOpenBSM.c
 
# Build the MacFUSE reporter.
build-macfuse: build/spade/reporter/MacFUSE.class src/spade/reporter/MacFUSE.java src/spade/reporter/libMacFUSE.c
	$(JAVAC) -classpath 'build:lib/*' -h src/spade/reporter src/spade/reporter/MacFUSE.java
	gcc -dynamiclib -I$(shell java -classpath build spade.utility.JavaHome) -I$(shell java -classpath build spade.utility.JavaHome)/darwin src/spade/reporter/libMacFUSE.c `pkg-config fuse --cflags --libs` -o lib/libMacFUSE.jnilib
 
llvm:
	@make $(LLVM_RULE)
 
 
build-mac-llvm:
	$(eval CXX_FLAGS = $(shell $(LLVM_PATH)/bin/llvm-config --cxxflags))
	$(eval C_FLAGS = $(shell $(LLVM_PATH)/bin/llvm-config --cflags))
	$(eval CLANG_FLAGS = -cc1 -triple x86_64-apple-macosx10.6.8 -emit-obj -mrelax-all -disable-free ${C_FLAGS})
	$(eval CLANG++_FLAGS = ${CXX_FLAGS} -m64 -Wl,-flat_namespace -Wl,-undefined,suppress -dynamiclib -mmacosx-version-min=10.6)
	$(LLVM_PATH)/bin/clang ${CLANG_FLAGS} -g -o src/spade/reporter/llvmTracer.o -x c++ src/spade/reporter/llvmTracer.cpp
	$(LLVM_PATH)/bin/clang ${CLANG++_FLAGS} -o lib/llvmTracer.dylib src/spade/reporter/llvmTracer.o
	$(LLVM_PATH)/bin/clang -static ${REPLIB_OSFLAG} src/spade/reporter/llvmBridge.c -c -o src/spade/reporter/llvmBridge.o
	$(LLVM_PATH)/bin/clang -c -emit-llvm ${TARGET}.c -o ${LLVM_TARGET}.bc
	$(LLVM_PATH)/bin/opt -load lib/llvmTracer.dylib -provenance ${LLVM_TARGET}.bc -o ${LLVM_TARGET}.bc
	$(LLVM_PATH)/bin/llc ${LLVM_TARGET}.bc -o ${LLVM_TARGET}.s
	$(LLVM_PATH)/bin/clang src/spade/reporter/llvmClose.c -c -o src/spade/reporter/llvmClose.o
	$(LLVM_PATH)/bin/clang ${LLVM_TARGET}.s src/spade/reporter/llvmClose.o -dynamiclib -Wl,-flat_namespace -Wl,-undefined,suppress -o ${LLVM_TARGET}.dylib
	$(LLVM_PATH)/bin/clang src/spade/reporter/llvmBridge.o ${LLVM_TARGET}.dylib -o ${LLVM_TARGET}
 
build-linux-llvm:
	echo llvm[0]: "Compiling llvmTracer.cpp for Release+Asserts build" "(PIC)"
	clang++ -I$(LLVM_INCLUDE_PATH) -I./  -D_DEBUG -D_GNU_SOURCE -D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -D__STDC_LIMIT_MACROS -O3 -fomit-frame-pointer -std=c++11 -fvisibility-inlines-hidden -fno-exceptions -fno-rtti -fPIC -ffunction-sections -fdata-sections -Wcast-qual	-pedantic -Wno-long-long -Wall -W -Wno-unused-parameter -Wwrite-strings  -Wcovered-switch-default -Wno-uninitialized  -Wno-missing-field-initializers -Wno-comment -c -MMD -MP -MF "llvmTracer.d.tmp" -MT "llvmTracer.o" -MT "llvmTracer.d" src/spade/reporter/llvm/llvmTracer.cpp -o src/spade/reporter/llvm/llvmTracer.o ; \
	echo llvm[0]: Linking "Loadable Module" \
   	   LLVMTrace.so
	clang++  -O3 -Wl,-R -Wl,'$ORIGIN' -Wl,--gc-sections -rdynamic -L./ -L./  -shared -o bin/llvm/LLVMTrace.so src/spade/reporter/llvm/llvmTracer.o -lpthread -ltinfo -ldl -lm
	clang -emit-llvm -c src/spade/reporter/llvm/flushModule.c -o bin/llvm/flush.bc

 
	echo llvm[0]: "Compiling WrapperPass.cpp for Release+Asserts build" "(PIC)"
	clang++ -I$(LLVM_INCLUDE_PATH) -I./ -D_DEBUG -D_GNU_SOURCE -D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -D__STDC_LIMIT_MACROS -O3 -fomit-frame-pointer -std=c++11 -fvisibility-inlines-hidden -fno-exceptions -fno-rtti -fPIC -ffunction-sections -fdata-sections -Wcast-qual -pedantic -Wno-long-long -Wall -W -Wno-unused-parameter -Wwrite-strings -Wcovered-switch-default -Wno-uninitialized -Wno-missing-field-initializers -Wno-comment -c -MMD -MP -MT "WrapperPass.o"  src/spade/reporter/llvm/LibcWrapper.cpp -o src/spade/reporter/llvm/LibcWrapper.o ;
 
	echo llvm[0]: Linking "Loadable Module build/WrapperPass.so"
	clang++ -O3 -Wl,-R -Wl,'$ORIGIN' -Wl,--gc-sections -rdynamic -L./ -Lm./ -shared -o bin/llvm/LibcWrapper.so \
	src/spade/reporter/llvm/LibcWrapper.o -lpthread -ltinfo -ldl -lm

 
# Build for Android
android-build:
	@mkdir -p build;
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/client/Android.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/core/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/utility/*.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/filter/IORuns.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/storage/Graphviz.java
	$(JAVAC) $(JAVAC_OPTIONS) -cp '$(JAVAC_CP)' -sourcepath src -d build src/spade/reporter/Strace.java
	# @mkdir -p android-lib;
	# $(ANDROID_BUILD_TOOLS)/dx --dex --output=android-lib/lucene-core-3.5.0.jar lib/lucene-core-3.5.0.jar
	@mkdir -p android-build;
	echo "dalvikvm -cp android-spade.jar spade.client.Android" > android-build/control.sh
	@cd build; \
	$(ANDROID_BUILD_TOOLS)/dx --dex --verbose --no-strict --output=../android-build/android-spade.jar spade;
 
# Deploy to Android
android-start:
	$(ANDROID_SDK_TOOLS)/adb shell start
	$(ANDROID_SDK_TOOLS)/adb shell "rm -r /sdcard/spade"
	$(ANDROID_SDK_TOOLS)/adb shell "mkdir /sdcard/spade"
	$(ANDROID_SDK_TOOLS)/adb shell "mkdir /sdcard/spade/log"
	$(ANDROID_SDK_TOOLS)/adb shell "mkdir /sdcard/spade/conf"
	# $(ANDROID_SDK_TOOLS)/adb shell "mkdir /sdcard/spade/android-lib"
	# $(ANDROID_SDK_TOOLS)/adb shell "mkdir /sdcard/spade/android-build"
	$(ANDROID_SDK_TOOLS)/adb shell "echo -e \"filter IORuns 0\\nstorage Graphviz /sdcard/spade/audit.dot\\nreporter Strace name=zygote user=radio user=system !name=/system/bin/surfaceflinger\" > /sdcard/spade/conf/spade.config"
	# @for f in "android-build" "android-lib"; do $(ANDROID_SDK_TOOLS)/adb push $$f /sdcard/spade/$$f; done;
	$(ANDROID_SDK_TOOLS)/adb push android-build/android-spade.jar /sdcard/spade/android-spade.jar;
	$(ANDROID_SDK_TOOLS)/adb push android-build/control.sh /sdcard/spade/control.sh;
	# $(ANDROID_SDK_TOOLS)/adb shell "cd /sdcard/spade/android-build; dalvikvm -Xmx512M -cp android-spade.jar spade.core.Kernel android"
	$(ANDROID_SDK_TOOLS)/adb shell "cd /sdcard/spade; dalvikvm -Xmx512M -cp android-spade.jar spade.core.Kernel android"
 
android-stop:
	# $(ANDROID_SDK_TOOLS)/adb shell "cd /sdcard/spade/android-build; dalvikvm -cp android-spade.jar spade.client.Android shutdown"
	$(ANDROID_SDK_TOOLS)/adb shell "cd /sdcard/spade; dalvikvm -cp android-spade.jar spade.client.Android shutdown"
 
graph-utility:
	java -cp 'build:lib/*' spade.utility.GraphUtility
 
# ---------- Removing files ----------
# Remove Java classes and native libraries, executables that we compiled.
clean:
	@echo 'Removing Java classes, native libraries, executables...'
	@rm -rf build android-build android-lib
	@rm -rf src/spade/reporter/spade_reporter_LinuxFUSE.h lib/libLinuxFUSE.* lib/libMacFUSE.*
	@rm -rf lib/spade.jar
	@rm -rf lib/spadeOpenBSM lib/spadeAuditBridge
	@rm -rf cfg/ssl
	@rm -rf log
	@rm -rf tmp
	@$(MAKE) --no-print-directory -f $(AUDIT_KERNEL_MODULES_MAKEFILE) clean
 
DESTDIR = /usr/local
install:
	test -d $(DESTDIR) || mkdir $(DESTDIR)
	cp -R bin $(DESTDIR)
	cp -R lib $(DESTDIR)
	cp -R cfg $(DESTDIR)
 
 
