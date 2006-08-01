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
(jbyteArray a0, JPTR a1, jint a2, JPTR a3, jint a4, JPTR a5, jint a6, JPTR a7)
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
      ((a0 ? elements(a0) : 0), a1, a2, a3, a4, a5, a6, a7);
  } else {
    return 0;
  }
}
