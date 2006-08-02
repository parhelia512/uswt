ifdef win32
  build-dir = build/win32
  g++ = mingw32-g++
  gcj = mingw32-gcj
  gij = mingw32-gij
  gcjh = mingw32-gcjh
  ar = mingw32-ar
  ugcj = /usr/local/gcc-ulibgcj-w32/bin/mingw32-gcj
	classpath-mode = win32
	jptr = jint
	long-filter = cat
else
ifdef lin32
  build-dir = build/lin32
  g++ = /usr/local/gcc/bin/g++
  gcj = /usr/local/gcc/bin/gcj
  gij = /usr/local/gcc/bin/gij
  gcjh = /usr/local/gcc/bin/gcjh
  ar = ar
  ugcj = /usr/local/gcc-ulibgcj/bin/gcj
	classpath-mode = unix
	jptr = jint
	long-filter = cat
else
  build-dir = build/lin64
  g++ = /usr/local/gcc/bin/g++
  gcj = /usr/local/gcc/bin/gcj
  gij = /usr/local/gcc/bin/gij
  gcjh = /usr/local/gcc/bin/gcjh
  ar = ar
  ugcj = /usr/local/gcc-ulibgcj/bin/gcj
	classpath-mode = unix
	jptr = jlong
	long-filter = sed -e 's:int */\*long\*/:long /*int*/:g'
endif
endif

script-dir = scripts

.PHONY: default
default: $(gen-dir) $(build-dir)/swt.a

$(build-dir)/rules.mk: $(script-dir)/make-rules.pl
	@perl $(<) $(classpath-mode) >$(@)

-include $(build-dir)/rules.mk

gen-dir = $(build-dir)/generation

define gen-dir-find
	if test ! -h $(gen-dir); then
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
	@$(gcj) -C -d $(build-dir)/classes \
		--classpath $(build-dir)/sources:$(gen-dir)	$(gen-sources)

swt-cflags = \
	$$(pkg-config --cflags cairo) \
	$$(pkg-config --cflags gtk+-2.0) \
	$$(pkg-config --cflags atk gtk+-2.0) \
	-I$(build-dir)/native-sources \
	-I$(build-dir)/headers

swt-lflags += -fPIC \
	$$(pkg-config --libs-only-L cairo) -lcairo \
	$$(pkg-config --libs-only-L gtk+-2.0 gthread-2.0) \
	-lgtk-x11-2.0 -lgthread-2.0 -L/usr/X11R6/lib $(XLIB64) -lXtst \
	$$(pkg-config --libs-only-L atk gtk+-2.0) -latk-1.0 -lgtk-x11-2.0 \
	-L/usr/X11R6/lib -lGL -lGLU -lm

cflags = -Os -g -fPIC

.PHONY: swt-sources
swt-sources: $(swt-sources)

.PHONY: swt-headers
swt-headers: $(swt-headers)

.PHONY: swt-classes
swt-classes: $(swt-classes)

$(swt-classes): $(swt-sources)
	@echo "compiling swt sources"
	@mkdir -p $(build-dir)/classes
	@$(ugcj) -C -d $(build-dir)/classes --classpath $(build-dir)/sources $(^)

$(build-dir)/swt.cpp: \
		$(swt-classes) \
		$(gen-classes) \
		$(gen-properties)
	@echo "generating native code"
	@$(gij) -cp $(build-dir)/classes:$(gen-dir) \
		org.eclipse.swt.tools.internal.CNIGenerator	$(@)

$(build-dir)/swt-processed.cpp: \
		$(build-dir)/swt.cpp \
		$(swt-native-sources) \
		$(swt-headers)
	@echo "processing $(<)"
	@$(g++) $(cflags) $(swt-cflags) -E $(<) -o $(@)
	@sed -i -e 's/MacroProtect_//' $(@)

$(build-dir)/swt.o: $(build-dir)/swt-processed.cpp
	@echo "compiling $(@) from $(<)"
	@$(g++) $(cflags) -c $(<) -o $(@)

$(build-dir)/os_custom-processed.cpp: \
		$(build-dir)/native-sources/os_custom.cpp \
		$(swt-native-sources) \
		$(swt-headers)
	@echo "processing $(<)"
	@$(g++) $(cflags) -I$(build-dir) $(swt-cflags) -E $(<) -o $(@)
	@sed -i -e 's/MacroProtect_//' $(@)

$(build-dir)/os_custom.o: $(build-dir)/os_custom-processed.cpp
	@echo "compiling $(@) from $(<)"
	@$(g++) -DJPTR=$(jptr) $(cflags) -I$(build-dir) $(swt-cflags) -c $(<) -o $(@)

$(build-dir)/cni-callback.o: $(build-dir)/native-sources/cni-callback.cpp
	@mkdir -p $(dir $(@))
	@echo "compiling $(@) from $(<)"
	@$(g++) $(cflags) -I$(build-dir) $(swt-cflags) -c $(<) -o $(@)

$(build-dir)/swt.a: \
		$(build-dir)/swt.o \
		$(build-dir)/os_custom.o \
		$(build-dir)/cni-callback.o \
		$(swt-objects)
	@rm -f $(@)
	@echo "creating $(@)"
	@$(ar) cru $(@) $(^)

# top-example-dir = ../swt/org.eclipse.swt.examples
# example-dir = $(top-example-dir)/src
# example-sources = $(shell find \
# 	$(example-dir)/org/eclipse/swt/examples/controlexample \
# 	-name '[A-Za-z]*.java')
# example-objects = $(foreach x,$(example-sources),$(patsubst \
# 	$(example-dir)/%.java,$(build-dir)/%.o,$(x)))
# example-resources = $(shell find \
# 	$(example-dir)/org/eclipse/swt/examples/controlexample \
# 	-name '[A-Za-z]*.gif' -or -name '[A-Za-z]*.png')
# example-resource-objects = $(foreach x,$(example-resources),$(patsubst \
# 	$(example-dir)/%,$(build-dir)/%.o,$(x)))

# .PHONY: example
# example: $(build-dir)/example

# $(example-objects): $(build-dir)/%.o: $(example-dir)/%.java
# 	@mkdir -p $(dir $(@))
# 	@echo "compiling $(@)"
# 	@gcj $(cflags) --classpath $(example-dir):$(swt-dir) -c $(<) -o $(@)

# $(example-resource-objects): $(build-dir)/%.o: $(example-dir)/%
# 	@mkdir -p $(dir $(@))
# 	@echo "generating $(@) from $(<)"
# 	@gcj --resource $(patsubst $(example-dir)/%,%,$(<)) -c $(<) -o $(@)

# $(build-dir)/examples_control.o: $(example-dir)/examples_control.properties
# 	@mkdir -p $(dir $(@))
# 	@echo "generating $(@) from $(<)"
# 	gcj --resource examples_control.properties -c $(<) -o $(@)

# $(build-dir)/example: \
# 		$(build-dir)/examples_control.o \
# 		$(example-resource-objects) \
# 		$(example-objects) \
# 		$(build-dir)/swt.a
# 	@echo "linking $(@)"
# 	@gcj --main=org.eclipse.swt.examples.controlexample.ControlExample \
# 		$(swt-lflags) $(^) -o $(@)

.PHONY: hello
hello: $(build-dir)/hello

$(build-dir)/hello: test/Hello.java $(build-dir)/swt.a
	@echo "compiling $(@) from $(<)"
	@$(ugcj) $(cflags) --classpath=$(build-dir)/classes \
		--main=Hello $(swt-lflags) $(<) $(build-dir)/swt.a -o $(@)

.PHONY: clean
clean:
	@echo "removing $(build-dir)"
	@rm -rf $(build-dir)
