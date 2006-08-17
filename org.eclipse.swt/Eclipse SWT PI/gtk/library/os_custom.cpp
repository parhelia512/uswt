#include <stdint.h>
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
  static bool initialized = false;
  static void* handle = 0;
  typedef JPTR (*Procedure)
    (jbyte*, JPTR, jint, JPTR, jint, JPTR, jint, JPTR, ...);
  static Procedure procedure;

  if (not initialized) {
    if (handle = 0)
      handle = ::dlopen(gtk_file_chooser_dialog_new_LIB, RTLD_LAZY);
    if (handle)
      procedure = (Procedure) ::dlsym(handle, "gtk_file_chooser_dialog_new");
    initialized = true;
  }
  
  if (procedure) {
    return (JPTR) procedure
      ((p0 ? elements(p0) : 0), p1, p2, p3, p4, p5, p6, p7);
  } else {
    return 0;
  }
}
