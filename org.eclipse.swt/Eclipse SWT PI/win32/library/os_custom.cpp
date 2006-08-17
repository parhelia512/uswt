#include <stdint.h>
#include "org/eclipse/swt/internal/win32/OS.h"
#include "os.h"

#ifndef NO_GetLibraryHandle
jint
org::eclipse::swt::internal::win32::OS::
GetLibraryHandle()
{
  return 0;
}
#endif

#ifndef NO_IsPPC
jboolean
org::eclipse::swt::internal::win32::OS::
IsPPC_()
{
#ifdef WIN32_PLATFORM_PSPC
  return (jboolean) true;
#else
  return (jboolean) false;
#endif
}
#endif

#ifndef NO_IsSP
jboolean
org::eclipse::swt::internal::win32::OS::
IsSP_()
{
#ifdef WIN32_PLATFORM_WFSP
  return (jboolean) true;
#else
  return (jboolean) false;
#endif
}
#endif

#ifndef NO_SendMessageW__II_3I_3I
jint
org::eclipse::swt::internal::win32::OS::
SendMessageW(jint p0, jint p1, jintArray p2, jintArray p3)
{
  return (jint) ::SendMessageW((HWND) p0, (int32_t) p1, (WPARAM) (p2 ? elements(p2) : 0), (LPARAM) (p3 ? elements(p3) : 0));
}
#endif
