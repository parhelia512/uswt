#include <stdint.h>
#include <objc/objc-runtime.h>
#include "java/lang/UnsupportedOperationException.h"
#include "org/eclipse/swt/internal/carbon/CGRect.h"
#include "org/eclipse/swt/internal/carbon/CGPoint.h"
#include "org/eclipse/swt/internal/carbon/OS.h"
#include "org/eclipse/swt/internal/cocoa/Cocoa.h"
#include "os.h"

#ifndef NO_CGPoint
void getCGPointFields(org::eclipse::swt::internal::carbon::CGPoint* src, CGPoint* dst);
void setCGPointFields(org::eclipse::swt::internal::carbon::CGPoint* dst, CGPoint* src);
#endif
#ifndef NO_CGRect
void getCGRectFields(org::eclipse::swt::internal::carbon::CGRect* src, CGRect* dst);
void setCGRectFields(org::eclipse::swt::internal::carbon::CGRect* dst, CGRect* src);
#endif

#ifndef NO_NewGlobalRef
jint
org::eclipse::swt::internal::carbon::OS::
NewGlobalRef(jobject p0)
{
  return reinterpret_cast<jint>(p0);
}
#endif

#ifndef NO_DeleteGlobalRef
void
org::eclipse::swt::internal::carbon::OS::
DeleteGlobalRef(jint p0)
{
  // do nothing
}
#endif

#ifndef NO_JNIGetObject
jobject
org::eclipse::swt::internal::carbon::OS::
JNIGetObject(jint p0)
{
  return reinterpret_cast<jobject>(p0);
}
#endif

#ifndef NO_CGAffineTransformConcat
void
org::eclipse::swt::internal::carbon::OS::
CGAffineTransformConcat(jfloatArray p0, jfloatArray p1, jfloatArray p2)
{
  *((CGAffineTransform*) elements(p2)) = ::CGAffineTransformConcat
    (*((CGAffineTransform*) elements(p0)),
     *((CGAffineTransform*) elements(p1)));
}
#endif

#ifndef NO_CGAffineTransformInvert
void
org::eclipse::swt::internal::carbon::OS::
CGAffineTransformInvert(jfloatArray p0, jfloatArray p1)
{
  *((CGAffineTransform*) elements(p1)) = ::CGAffineTransformInvert
    (*((CGAffineTransform*) elements(p0)));
}
#endif

#ifndef NO_CGAffineTransformMake
void
org::eclipse::swt::internal::carbon::OS::
MacroProtect_CGAffineTransformMake
(jfloat p0, jfloat p1, jfloat p2, jfloat p3, jfloat p4, jfloat p5,
 jfloatArray p6)
{
  *((CGAffineTransform*) elements(p6)) = CGAffineTransformMake
    (p0, p1, p2, p3, p4, p5);
}
#endif

#ifndef NO_CGAffineTransformRotate
void
org::eclipse::swt::internal::carbon::OS::
CGAffineTransformRotate(jfloatArray p0, jfloat p1, jfloatArray p2)
{
  *((CGAffineTransform*) elements(p2)) = ::CGAffineTransformRotate
    (*((CGAffineTransform*) elements(p0)), p1);
}
#endif

#ifndef NO_CGAffineTransformScale
void
org::eclipse::swt::internal::carbon::OS::
CGAffineTransformScale(jfloatArray p0, jfloat p1, jfloat p2, jfloatArray p3)
{
  *((CGAffineTransform*) elements(p3)) = ::CGAffineTransformScale
    (*((CGAffineTransform*) elements(p0)), p1, p2);
}
#endif

#ifndef NO_CGAffineTransformTranslate
void
org::eclipse::swt::internal::carbon::OS::
CGAffineTransformTranslate(jfloatArray p0, jfloat p1, jfloat p2,
                           jfloatArray p3)
{
  *((CGAffineTransform*) elements(p3)) = ::CGAffineTransformTranslate
    (*((CGAffineTransform*) elements(p0)), p1, p2);
}
#endif

#ifndef NO_CGContextGetCTM
void
org::eclipse::swt::internal::carbon::OS::
CGContextGetCTM(jint p0, jfloatArray p1)
{
  *((CGAffineTransform*) elements(p1)) = ::CGContextGetCTM
    ((CGContextRef) p0);
}
#endif

#ifndef NO_CGContextGetPathBoundingBox
void
org::eclipse::swt::internal::carbon::OS::
CGContextGetPathBoundingBox(jint p0,
                            org::eclipse::swt::internal::carbon::CGRect* p1)
{
  ::CGRect ps1 = ::CGContextGetPathBoundingBox((CGContextRef) p0);
  setCGRectFields(p1, &ps1);
}
#endif

#ifndef NO_CGContextGetTextPosition
void
org::eclipse::swt::internal::carbon::OS::
CGContextGetTextPosition(jint p0,
                         org::eclipse::swt::internal::carbon::CGPoint* p1)
{
  ::CGPoint ps1 = ::CGContextGetTextPosition((CGContextRef) p0);
  setCGPointFields(p1, &ps1);
}
#endif

#ifndef NO_CGPathGetBoundingBox
void
org::eclipse::swt::internal::carbon::OS::
CGPathGetBoundingBox(jint p0,
                     org::eclipse::swt::internal::carbon::CGRect* p1)
{
  ::CGRect ps1 = ::CGPathGetBoundingBox((const CGPath*) p0);
  setCGRectFields(p1, &ps1);
}
#endif

#ifndef NO_CGPathGetCurrentPoint
void
org::eclipse::swt::internal::carbon::OS::
CGPathGetCurrentPoint(jint p0,
                      org::eclipse::swt::internal::carbon::CGPoint* p1)
{
  ::CGPoint ps1 = ::CGPathGetCurrentPoint((const CGPath*) p0);
  setCGPointFields(p1, &ps1);
}
#endif

#ifndef NO_CGPointApplyAffineTransform
void
org::eclipse::swt::internal::carbon::OS::
MacroProtect_CGPointApplyAffineTransform
(org::eclipse::swt::internal::carbon::CGPoint* p0, jfloatArray p1,
 org::eclipse::swt::internal::carbon::CGPoint* p2)
{
  ::CGPoint ps0;
  getCGPointFields(p0, &ps0);
  ::CGPoint ps2 = CGPointApplyAffineTransform(ps0, *((CGAffineTransform*) p1));
  setCGPointFields(p2, &ps2);
}
#endif

#ifndef NO__1_1BIG_1ENDIAN_1_1
jboolean
org::eclipse::swt::internal::carbon::OS::
bigEndian()
{
#ifdef __BIG_ENDIAN__
  return true;
#else
  return false;
#endif
}
#endif

extern id objc_msgSend(id, SEL, ...);

#ifndef NO_objc_1msgSend__IIF
jint
org::eclipse::swt::internal::cocoa::Cocoa::
objc_msgSend(jint p0, jint p1, jfloat p2)
{
  return ((jint (*) (id, SEL, float)) ::objc_msgSend)
    ((id) p0, (SEL) p1, p2);
}
#endif
