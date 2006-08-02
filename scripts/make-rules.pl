#!/usr/bin/perl -w

use strict;

use constant {
    SWTParent => "org.eclipse.swt",
    SWTDirectories => [ "Eclipse SWT",
                        "Eclipse SWT Accessibility",
                        "Eclipse SWT Custom Widgets",
                        "Eclipse SWT Drag and Drop",
                        "Eclipse SWT PI",
                        "Eclipse SWT Printing",
                        "Eclipse SWT Program",
                        "Eclipse SWT Theme" ],
    ClassPaths => {
        "unix" => [
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
    },
    NativePaths => {
        "unix" => [
            "Eclipse SWT/common/library",
            "Eclipse SWT PI/gtk/library",
            "Eclipse SWT PI/cairo/library",
            "Eclipse SWT OpenGL/glx/library",
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
    for my $file (@files) {
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
    
    my @files = `find $dirs -regextype posix-extended -regex '.*/[A-Za-z].*\.(c|cpp|h)'`;
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

die "usage: $0 unix|win32|osx"
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
    print "\t\@perl \$(script-dir)/process.pl -DUSWT <\"\$(<)\" | \$(long-filter) >\$(@)\n\n";

    my $class = shift;
    my $object = shift;

    print "$object: $new swt-sources\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"compiling \$(\@)\"\n";
    print "\t\@\$(ugcj) -c \$(cflags) --classpath \$(build-dir)/sources \$(<) -o \$(@)\n\n";

    my $header = shift;

    print "$header: $class\n";
    print "\t\@mkdir -p \$(dir \$(\@))\n";
    print "\t\@echo \"generating \$(\@)\"\n";
    print "\t\@\$(gcjh) -d \$(build-dir)/headers --classpath \$(build-dir)/classes:\$(build-dir)/sources \$(patsubst \$(build-dir)/classes/\%.class,\%,\$(<))\n\n";
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
