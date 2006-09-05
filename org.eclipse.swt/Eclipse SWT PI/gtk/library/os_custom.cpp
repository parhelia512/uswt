#include <stdint.h>
#include <stdio.h>
#include "org/eclipse/swt/internal/gtk/OS.h"
#include "os.h"

jboolean
org::eclipse::swt::internal::gtk::OS::
MacroProtect_GDK_WINDOWING_X11()
{
  return 1;
}

JPTR
org::eclipse::swt::internal::gtk::OS::
MacroProtect__gtk_file_chooser_dialog_new
(jbyteArray p0, JPTR p1, jint p2, JPTR p3, jint p4, JPTR p5, jint p6, JPTR p7)
{
  return (JPTR) ::gtk_file_chooser_dialog_new
    ((const gchar*) (p0 ? elements(p0) : 0), (GtkWindow*) p1,
     (GtkFileChooserAction) p2, (const gchar*) p3, p4, p5, p6, p7);
}
