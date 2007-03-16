#!/usr/bin/perl -w

use strict;

use constant {
    SWTParent => "org.eclipse.swt",

    Blacklist => [
        "org/eclipse/swt/internal/Callback.java",
    ],

    ClassPaths => {
        "posix-gtk" => [
            "Eclipse SWT/gtk",
            "Eclipse SWT/cairo",
            "Eclipse SWT/emulated/bidi",
            "Eclipse SWT/emulated/coolbar",
            "Eclipse SWT/common",
            "Eclipse SWT/common_j2me",
            "Eclipse SWT PI/gtk",
            "Eclipse SWT PI/cairo",
            "Eclipse SWT PI/common",
            "Eclipse SWT PI/common_j2me",
            "Eclipse SWT Accessibility/gtk",
            "Eclipse SWT Accessibility/common",
            "Eclipse SWT Drag and Drop/gtk",
            "Eclipse SWT Drag and Drop/common",
            "Eclipse SWT Printing/gtk",
            "Eclipse SWT Printing/common",
            "Eclipse SWT Program/gtk",
            "Eclipse SWT Program/common",
            "Eclipse SWT Program/gnome",
            "Eclipse SWT Program/cde",
            "Eclipse SWT Custom Widgets/common",
            "Eclipse SWT OpenGL/gtk",
            "Eclipse SWT OpenGL/glx",
            "Eclipse SWT OpenGL/common",
            "Eclipse SWT Theme/gtk",
        ],
        "posix-carbon" => [
            "Eclipse SWT/carbon",
            "Eclipse SWT/common",
            "Eclipse SWT/common_j2me",
            "Eclipse SWT/emulated/bidi",
            "Eclipse SWT/emulated/coolbar",
            "Eclipse SWT/emulated/expand",
            "Eclipse SWT PI/carbon",
            "Eclipse SWT PI/common_j2me",
            "Eclipse SWT Accessibility/common",
            "Eclipse SWT Accessibility/carbon",
            "Eclipse SWT Custom Widgets/common",
            "Eclipse SWT Drag and Drop/common",
            "Eclipse SWT Drag and Drop/emulated",
            "Eclipse SWT Printing/common",
            "Eclipse SWT Printing/carbon",
            "Eclipse SWT Program/common",
            "Eclipse SWT Program/carbon",
            "Eclipse SWT OpenGL/carbon",
            "Eclipse SWT OpenGL/common",
            "Eclipse SWT Theme/carbon",
        ],
        "win32" => [
            "Eclipse SWT/win32",
            "Eclipse SWT/common",
            "Eclipse SWT/common_j2me",
            "Eclipse SWT PI/win32",
            "Eclipse SWT PI/common_j2me",
            "Eclipse SWT Accessibility/emulated",
            "Eclipse SWT Accessibility/common",
            "Eclipse SWT Drag and Drop/emulated",
            "Eclipse SWT Drag and Drop/common",
            "Eclipse SWT Printing/win32",
            "Eclipse SWT Printing/common",
            "Eclipse SWT Program/win32",
            "Eclipse SWT Program/common",
            "Eclipse SWT Custom Widgets/common",
            "Eclipse SWT OpenGL/win32",
            "Eclipse SWT OpenGL/common",
            "Eclipse SWT Theme/win32",
        ],
    },

    NativePaths => {
        "posix-gtk" => [
            "Eclipse SWT/common/library",
            "Eclipse SWT PI/gtk/library",
            "Eclipse SWT PI/cairo/library",
            "Eclipse SWT OpenGL/glx/library",
        ],
        "posix-carbon" => [
            "Eclipse SWT/common/library",
            "Eclipse SWT PI/carbon/library",
            "Eclipse SWT OpenGL/carbon/library",
        ],
        "win32" => [
            "Eclipse SWT/common/library",
            "Eclipse SWT PI/win32/library",
            "Eclipse SWT OpenGL/win32/library",
        ],
    },
};

sub generate {
    my $callback = shift;
    my $path = shift;

    my $dirs = "";
    for my $dir (@{$path}) {
        my $parent = SWTParent;
        $dirs .= "\"$parent/$dir\" ";
    }

    my @files = `find $dirs -name '[A-Za-z]*.java'`;
    my %map;
    loop: for my $file (@files) {
        my $blacklist = Blacklist;
        for my $blacklisted (@{$blacklist}) {
            if ($file =~ /$blacklisted$/) {
                next loop;
            }
        }

        my $new = $file;
        $new =~ s:.*/org/(.*):\$(build-dir)/sources/org/$1:;
        chomp $new;

        if (defined $map{$new}) {
            next;
        }

        $map{$new} = $file;

        my $old = $file;
        $old =~ s/ /\\ /g;
        chomp $old;
        
        my $class = $new;
        $class =~ s:(.*)/sources/(.*).java$:$1/classes/$2.class:;

        my $object = $new;
        $object =~ s:(.*)/sources/(.*).java$:$1/objects/$2.o:;

        my $header = $new;
        $header =~ s:(.*)/sources/(.*).java$:$1/headers/$2.h:;

        &$callback($old, $new, $class, $object, $header);
    }
}

sub generateNative {
    my $callback = shift;
    my $path = shift;

    my $dirs = "";
    for my $dir (@{$path}) {
        my $parent = SWTParent;
        $dirs .= "\"$parent/$dir\" ";
    }
    
    my @files = `find $dirs -regex '.*/[A-Za-z].*\.c' -or -regex '.*/[A-Za-z].*\.cpp' -or -regex '.*/[A-Za-z].*\.h'`;
    my %map;
    for my $file (@files) {
        my $new = $file;
        $new =~ s:.*/(.*):\$(build-dir)/native-sources/$1:;
        chomp $new;

        if (defined $map{$new}) {
            next;
        }

        $map{$new} = $file;

        my $old = $file;
        $old =~ s/ /\\ /g;
        chomp $old;

        &$callback($old, $new);
    }
}

die "usage: $0 posix-gtk|win32|osx"
    if ($#ARGV != 0 || ! (defined ClassPaths->{$ARGV[0]}));

my @sources;
my @classes;
my @objects;
my @headers;

my $variables = sub {
    shift;
    $sources[++$#sources] = shift;
    $classes[++$#classes] = shift;
    $objects[++$#objects] = shift;
    $headers[++$#headers] = shift;
};

generate($variables, ClassPaths->{$ARGV[0]});

print "swt-sources := @sources\n";
print "swt-classes := @classes\n";
print "swt-objects := @objects\n";
print "swt-headers := @headers\n";

my $rules = sub {
    my $old = shift;
    my $new = shift;

    print "$new: $old\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"generating \$(\@)\"\n";
    print "\tperl \$(script-dir)/process.pl -DUSWT <\"\$(<)\" | \$(long-filter) >\$(@)\n\n";

    my $class = shift;
    my $object = shift;

    print "$object: $class\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"compiling \$(\@)\"\n";
    print "\t\$(ugcj) -c \$(cflags) --classpath \$(build-dir)/classes -o \$(@) \$(<) \$\$(find \$(build-dir)/classes -path '\$(basename \$(<))\$\$*.class')\n\n";

    my $header = shift;

    print "$header: $class\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"generating \$(\@)\"\n";
    print "\t\$(gcjh) -d \$(build-dir)/headers --classpath \$(build-dir)/classes:\$(build-dir)/sources \$(patsubst \$(build-dir)/classes/\%.class,\%,\$(<))\n\n";
};

generate($rules, ClassPaths->{$ARGV[0]});

my @native_sources;

my $native_variables = sub {
    shift;
    $native_sources[++$#native_sources] = shift;
};

generateNative($native_variables, NativePaths->{$ARGV[0]});

print "swt-native-sources := @native_sources\n";

my $native_rules = sub {
    my $old = shift;
    my $new = shift;

    print "$new: $old\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"copying \"\$(<)\" to \$(\@)\"\n";
    print "\t\@cp \"\$(<)\" \$(@)\n\n";
};

generateNative($native_rules, NativePaths->{$ARGV[0]});
