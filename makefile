MAKEFLAGS = -s

ifdef lin64
  build-dir = build/lin64
	platform = unix
	jptr = jlong
else
ifdef lin32
  build-dir = build/lin32
	platform = unix
	jptr = jint
else
ifdef win32
  build-dir = build/win32
	platform = win32
	jptr = jint
	long-filter = cat
else
$(error please specify a one of the following: lin64=1, lin32=1, win32=1)
endif
endif
endif

ifeq "$(platform)" "unix"
  g++ = g++
  gcj = gcj
  gij = gij
  gcjh = gcjh
  ar = ar
  ugcj = /usr/local/gcc-ulibgcj/bin/gcj -L/usr/local/gcc-ulibgcj/lib
  cflags = -O0 -g -fPIC

  swt-cflags = \
		-DJPTR=$(jptr) \
		$$(pkg-config --cflags cairo) \
		$$(pkg-config --cflags gtk+-2.0) \
		$$(pkg-config --cflags atk gtk+-2.0) \
		-I$(build-dir)/native-sources \
		-I$(build-dir)/headers

	swt-lflags = -fPIC \
		$$(pkg-config --libs-only-L cairo) -lcairo \
		$$(pkg-config --libs-only-L gtk+-2.0 gthread-2.0) \
		-lgtk-x11-2.0 -lgthread-2.0 -L/usr/X11R6/lib -lXtst \
		$$(pkg-config --libs-only-L atk gtk+-2.0) -latk-1.0 -lgtk-x11-2.0 \
		-L/usr/X11R6/lib -lGL -lGLU -lm
else
ifeq "$(platform)" "win32"
  g++ = /usr/local/gcc-ulibgcj-w32/bin/mingw32-g++
  gcj = gcj
  gij = gij
  gcjh = /usr/local/gcc-ulibgcj-w32/bin/mingw32-gcjh
  ar = mingw32-ar
  ugcj = /usr/local/gcc-ulibgcj-w32/bin/mingw32-gcj -L/usr/local/gcc-ulibgcj-w32/lib
	dlltool = mingw32-dlltool -k
  cflags = -O0 -g
  msvc = cl
  msvccflags = "-Ic:\Program Files\Microsoft Platform SDK for Windows Server 2003 R2\Include" -I$(build-dir)/native-sources
	msvclflags = "-LIBPATH:c:\Program Files\Microsoft Platform SDK for Windows Server 2003 R2\Lib" gdiplus.lib gdi32.lib

  swt-cflags = \
		-DJPTR=$(jptr) \
		-D_WIN32_WINNT=0x0501 \
		-D_WIN32_IE=0x0500 \
		-DCINTERFACE \
		-I$(build-dir)/native-sources \
		-I$(build-dir)/headers

	swt-lflags = \
		-lgdi32 -lopengl32 -lole32 -lolepro32 -lusp10 -lcomdlg32 -limm32 \
		-lcomctl32 -loleaut32 -lwininet -lmsvfw32 -lopengl32 -oleaut32 \
		-mwindows -mconsole
endif
endif

ifeq "$(jptr)" "jlong"
	swt-cflags += -DJPTR_IS_JLONG
	long-filter = sed -e 's:int */\*long\*/:long /*int*/:g'
else
	long-filter = cat
endif

script-dir = scripts

stamp-dir = $(build-dir)/stamps

.PHONY: default
default: $(gen-dir) $(build-dir)/swt.a

$(build-dir)/rules.mk: $(script-dir)/make-rules.pl
	@perl $(<) $(platform) >$(@)

-include $(build-dir)/rules.mk

gen-dir = $(build-dir)/generation

define gen-dir-find
	if test ! -e $(gen-dir); then
		mkdir -p $(dir $(gen-dir));
		ln -s "../../org.eclipse.swt.tools/JNI Generation" $(gen-dir);
	fi;
  find $(gen-dir)/org -name '[A-Za-z]*.java'
endef

gen-sources = $(shell $(gen-dir-find))
gen-classes = $(foreach x,$(gen-sources),$(patsubst \
	$(gen-dir)/%.java,$(build-dir)/classes/%.class,$(x)))
gen-properties = $(shell find $(gen-dir) -name 'org*.properties')

$(gen-classes): $(gen-sources) $(swt-sources)
	@mkdir -p $(build-dir)/classes
	@echo "compiling native code generator"
	$(gcj) -C -d $(build-dir)/classes \
		--classpath $(build-dir)/sources:$(gen-dir)	$(gen-sources)

swt-binding-dir = $(build-dir)/bindings
swt-processed-binding-dir = $(build-dir)/processed-bindings
swt-binding-object-dir = $(build-dir)/binding-objects
swt-foreign-binding-object-dir = $(build-dir)/foreign-binding-objects

.PHONY: swt-sources
swt-sources: $(swt-sources)

.PHONY: swt-headers
swt-headers: $(swt-headers)

.PHONY: swt-classes
swt-classes: $(swt-classes)

$(swt-classes): $(swt-sources)
	@echo "compiling swt sources"
	@mkdir -p $(build-dir)/classes
	$(ugcj) -C -d $(build-dir)/classes --classpath $(build-dir)/sources $(^)

$(stamp-dir)/swt-bindings: \
		$(swt-classes) \
		$(gen-classes) \
		$(gen-properties)
	@echo "generating bindings"
	@mkdir -p $(swt-binding-dir)
	$(gij) -cp $(build-dir)/classes:$(gen-dir) \
		org.eclipse.swt.tools.internal.CNIGenerator	-aggregate $(swt-binding-dir)/
	@mkdir -p $(stamp-dir)
	@touch $(@)

$(stamp-dir)/swt-processed-bindings: \
		$(stamp-dir)/swt-bindings \
		$(swt-native-sources) \
		$(swt-headers)
	@echo "processing bindings"
	@mkdir -p $(swt-processed-binding-dir)
	@set -e; for file in $(swt-binding-dir)/*.cpp; do \
		if ! echo $${file} | grep -q -- -foreign; then \
			echo "processing $${file}"; \
			$(g++) $(cflags) $(swt-cflags) -E $${file} \
				-o $(swt-processed-binding-dir)/$${file##*/}; \
			sed -i -e 's/MacroProtect_//g' \
				$(swt-processed-binding-dir)/$${file##*/}; \
		fi \
	 done
	@mkdir -p $(stamp-dir)
	@touch $(@)

$(build-dir)/swt-foreign.dll: $(swt-native-sources)
	@echo "linking foreign dll"
	$(msvc) $(msvccflags) -LD $(swt-binding-dir)/*-foreign*.cpp \
		$(build-dir)/native-sources/*-foreign*.cpp \
		-Fe$(@) -link $(msvclflags) -def:$(build-dir)/bindings/swt-foreign.def \
		-implib:$(build-dir)/swt-foreign-msvc.lib

$(build-dir)/swt-foreign.lib: \
		$(build-dir)/bindings/swt-foreign.def
	@echo "generating $(@)"
	@$(dlltool) --output-lib $(@) --def $(<)

# .PHONY: swt-foreign-bindings
# swt-foreign-bindings: \
# 		$(swt-native-sources)
# 	@echo "compiling foreign bindings"
# 	@mkdir -p $(swt-foreign-binding-object-dir)
# 	@set -e; for file in $(swt-binding-dir)/*-foreign*.cpp \
# 			$(build-dir)/native-sources/*-foreign*.cpp; do \
# 		echo "compiling $${file}"; \
# 		in=$$(echo $${file} | sed -e 's:/:\\:g'); \
# 		out=$$(echo $(swt-foreign-binding-object-dir)/$$(basename $${file} .cpp).o | \
# 						sed -e 's:/:\\:g'); \
# 		$(msvc) $(msvccflags) -c $${in} -Fo$${out}; \
# 	 done

# note the -O0 flag below - something breaks when the code is
# optimized which I haven't figured out, so we turn it off for these
# files.
$(stamp-dir)/swt-binding-objects: \
		$(stamp-dir)/swt-processed-bindings
	@echo "compiling bindings"
	@mkdir -p $(swt-binding-object-dir)
	@set -e; for file in $(swt-processed-binding-dir)/*.cpp; do \
		echo "compiling $${file}"; \
		$(g++) $(cflags) -O0 -fpreprocessed -c $${file} \
			-o $(swt-binding-object-dir)/$$(basename $${file} .cpp).o; \
	 done
	@mkdir -p $(stamp-dir)
	@touch $(@)

$(build-dir)/os_custom-processed.cpp: \
		$(build-dir)/native-sources/os_custom.cpp \
		$(swt-native-sources) \
		$(swt-headers)
	@echo "processing $(<)"
	@$(g++) $(cflags) -I$(build-dir) $(swt-cflags) -E $(<) -o $(@)
	@sed -i -e 's/MacroProtect_//' $(@)

$(build-dir)/os_custom.o: $(build-dir)/os_custom-processed.cpp
	@echo "compiling $(@) from $(<)"
	@$(g++) $(cflags) -I$(build-dir) $(swt-cflags) -c $(<) -o $(@)

$(build-dir)/cni-callback.o: $(build-dir)/native-sources/cni-callback.cpp
	@mkdir -p $(dir $(@))
	@echo "compiling $(@) from $(<)"
	@$(g++) $(cflags) -I$(build-dir) $(swt-cflags) -c $(<) -o $(@)

$(build-dir)/swt.a: \
		$(build-dir)/rules.mk \
		$(build-dir)/os_custom.o \
		$(build-dir)/cni-callback.o \
		$(stamp-dir)/swt-binding-objects \
		$(swt-objects)
	@rm -f $(@)
	@echo "creating $(@)"
	@$(ar) cru $(@) $(build-dir)/os_custom.o $(build-dir)/cni-callback.o \
		$(swt-objects) $(wildcard $(swt-binding-object-dir)/*.o)

.PHONY: hello
hello: $(build-dir)/hello

$(build-dir)/hello: \
		test/Hello.java \
		$(build-dir)/swt.a \
		$(build-dir)/swt-foreign.lib
	@echo "compiling $(@) from $(<)"
	$(ugcj) $(cflags) --classpath=$(build-dir)/classes \
		-Wl,--allow-multiple-definition \
		--main=Hello $(<) $(build-dir)/swt.a $(swt-lflags) \
		-o $(@)

top-example-dir = org.eclipse.swt.examples
example-dir = $(top-example-dir)/src
example-original-sources = $(shell find \
	$(example-dir)/org/eclipse/swt/examples/controlexample \
	-name '[A-Za-z]*.java')
example-sources =  $(foreach x,$(example-original-sources),$(patsubst \
	$(example-dir)/%,$(build-dir)/sources/%,$(x)))
example-classes = $(foreach x,$(example-original-sources),$(patsubst \
	$(example-dir)/%.java,$(build-dir)/classes/%.class,$(x)))
example-objects = $(foreach x,$(example-original-sources),$(patsubst \
	$(example-dir)/%.java,$(build-dir)/objects/%.o,$(x)))
example-resources = $(shell find \
	$(example-dir)/org/eclipse/swt/examples/controlexample \
	-name '[A-Za-z]*.gif' -or -name '[A-Za-z]*.png')
example-resource-objects = $(foreach x,$(example-resources),$(patsubst \
	$(example-dir)/%,$(build-dir)/%.o,$(x)))

.PHONY: example
example: $(build-dir)/example

$(example-classes): $(example-sources)
	@echo "compiling example sources"
	@mkdir -p $(build-dir)/classes
	@$(ugcj) -C -d $(build-dir)/classes \
		--classpath $(build-dir)/sources:$(build-dir)/classes $(^)

.PHONY: example-classes
example-classes: $(example-classes)

$(example-sources): $(build-dir)/sources/%: $(example-dir)/%
	@mkdir -p $(dir $(@))
	@echo "generating $(@)"
	@perl $(script-dir)/process.pl -DUSWT <$(<) >$(@)

.PHONY: example-sources
example-sources: $(example-sources)

$(example-objects): $(build-dir)/objects/%.o: \
		$(build-dir)/sources/%.java \
		$(example-sources) \
		$(swt-classes)
	@mkdir -p $(dir $(@))
	@echo "compiling $(@)"
	@$(ugcj) $(cflags) --classpath $(build-dir)/classes:$(build-dir)/sources \
		-c $(<) -o $(@)

$(example-resource-objects): $(build-dir)/%.o: $(example-dir)/%
	@mkdir -p $(dir $(@))
	@echo "generating $(@) from $(<)"
	@$(ugcj) --resource $(patsubst $(example-dir)/%,%,$(<)) -c $(<) -o $(@)

$(build-dir)/examples_control.o: $(example-dir)/examples_control.properties
	@mkdir -p $(dir $(@))
	@echo "generating $(@) from $(<)"
	@$(ugcj) --resource examples_control.properties -c $(<) -o $(@)

$(build-dir)/example: \
		$(build-dir)/examples_control.o \
		$(example-resource-objects) \
		$(example-objects) \
		$(build-dir)/swt.a
	@echo "linking $(@)"
	$(ugcj) -Wl,--allow-multiple-definition \
		--main=org.eclipse.swt.examples.controlexample.ControlExample \
		 $(^) $(swt-lflags) -o $(@)

.PHONY: clean
clean:
	@echo "removing $(build-dir)"
	@rm -rf $(build-dir)
