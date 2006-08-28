#include <stdint.h>
#include <stdio.h>
#include <gcj/cni.h>
#include "java/io/PrintStream.h"
#include "java/lang/System.h"
#include "java/lang/StringBuffer.h"
#include "java/lang/Throwable.h"
#include "org/eclipse/swt/internal/CNICallback.h"
#include "org/eclipse/swt/internal/CNIDispatcher.h"
#include "os.h"

#if defined (_WIN32) || defined (_WIN32_WCE)
#include "windows.h"
#define RETURN_TYPE LRESULT CALLBACK
#define RETURN_CAST (LRESULT)
#endif

#ifndef RETURN_TYPE
#define RETURN_TYPE JPTR
#endif

#ifndef RETURN_CAST
#define RETURN_CAST
#endif

namespace {

bool enabled = true;
const int MaxArgs = 12;
const int MaxCallbacks = 128;
org::eclipse::swt::internal::CNICallback* callbacks[MaxCallbacks];

JPTR
callback(int index, ...)
{
  org::eclipse::swt::internal::CNICallback* cb = callbacks[index];
#ifdef JPTR_IS_JLONG
  JArray<JPTR>* args = JvNewLongArray(cb->argCount);
#else
  JArray<JPTR>* args = JvNewIntArray(cb->argCount);
#endif
  va_list al;

  JPTR* array = elements(args);
  va_start(al, index);
  for (unsigned i = 0; i < cb->argCount; ++i) {
    array[i] = va_arg(al, JPTR);
  }
  va_end(al);

//   try {
    return cb->dispatcher->dispatch(cb->method, args);
//   } catch (java::lang::Throwable* e) {
//     e->printStackTrace();
//   }
}

/* Function name from index and number of arguments */
#define FN(index, args) fn##index##_##args

/**
 * Functions templates
 *
 * NOTE: If the maximum number of arguments changes (MaxArgs), the number
 *       of function templates has to change accordinglly.
 */

/* Function template with no arguments */
#define FN_0(index) RETURN_TYPE FN(index, 0)() { return RETURN_CAST callback(index); }

/* Function template with 1 argument */
#define FN_1(index) RETURN_TYPE FN(index, 1)(JPTR p1) { return RETURN_CAST callback(index, p1); }

/* Function template with 2 arguments */
#define FN_2(index) RETURN_TYPE FN(index, 2)(JPTR p1, JPTR p2) { return RETURN_CAST callback(index, p1, p2); }

/* Function template with 3 arguments */
#define FN_3(index) RETURN_TYPE FN(index, 3)(JPTR p1, JPTR p2, JPTR p3) { return RETURN_CAST callback(index, p1, p2, p3); }

/* Function template with 4 arguments */
#define FN_4(index) RETURN_TYPE FN(index, 4)(JPTR p1, JPTR p2, JPTR p3, JPTR p4) { return RETURN_CAST callback(index, p1, p2, p3, p4); }

/* Function template with 5 arguments */
#define FN_5(index) RETURN_TYPE FN(index, 5)(JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5); }

/* Function template with 6 arguments */
#define FN_6(index) RETURN_TYPE FN(index, 6)(JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6); }

/* Function template with 7 arguments */
#define FN_7(index) RETURN_TYPE FN(index, 7)(JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7); }

/* Function template with 8 arguments */
#define FN_8(index) RETURN_TYPE FN(index, 8)(JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7, JPTR p8) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7, p8); }

/* Function template with 9 arguments */
#define FN_9(index) RETURN_TYPE FN(index, 9)(JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7, JPTR p8, JPTR p9) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7, p8, p9); }

/* Function template with 10 arguments */
#define FN_10(index) RETURN_TYPE FN(index, 10) (JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7, JPTR p8, JPTR p9, JPTR p10) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10); }

/* Function template with 11 arguments */
#define FN_11(index) RETURN_TYPE FN(index, 11) (JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7, JPTR p8, JPTR p9, JPTR p10, JPTR p11) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11); }

/* Function template with 12 arguments */
#define FN_12(index) RETURN_TYPE FN(index, 12) (JPTR p1, JPTR p2, JPTR p3, JPTR p4, JPTR p5, JPTR p6, JPTR p7, JPTR p8, JPTR p9, JPTR p10, JPTR p11, JPTR p12) { return RETURN_CAST callback(index, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12); }

/**
 * Define all functions with the specified number of arguments.
 *
 * NOTE: If the maximum number of callbacks changes (MaxCallbacks),
 *       this macro has to be updated. 
 */
#define FN_BLOCK(args) FN_##args(0) FN_##args(1) FN_##args(2) FN_##args(3) FN_##args(4) FN_##args(5) FN_##args(6) FN_##args(7) FN_##args(8) FN_##args(9) FN_##args(10) FN_##args(11) FN_##args(12) FN_##args(13) FN_##args(14) FN_##args(15) FN_##args(16) FN_##args(17) FN_##args(18) FN_##args(19) FN_##args(20) FN_##args(21) FN_##args(22) FN_##args(23) FN_##args(24) FN_##args(25) FN_##args(26) FN_##args(27) FN_##args(28) FN_##args(29) FN_##args(30) FN_##args(31) FN_##args(32) FN_##args(33) FN_##args(34) FN_##args(35) FN_##args(36) FN_##args(37) FN_##args(38) FN_##args(39) FN_##args(40) FN_##args(41) FN_##args(42) FN_##args(43) FN_##args(44) FN_##args(45) FN_##args(46) FN_##args(47) FN_##args(48) FN_##args(49) FN_##args(50) FN_##args(51) FN_##args(52) FN_##args(53) FN_##args(54) FN_##args(55) FN_##args(56) FN_##args(57) FN_##args(58) FN_##args(59) FN_##args(60) FN_##args(61) FN_##args(62) FN_##args(63) FN_##args(64) FN_##args(65) FN_##args(66) FN_##args(67) FN_##args(68) FN_##args(69) FN_##args(70) FN_##args(71) FN_##args(72) FN_##args(73) FN_##args(74) FN_##args(75) FN_##args(76) FN_##args(77) FN_##args(78) FN_##args(79) FN_##args(80) FN_##args(81) FN_##args(82) FN_##args(83) FN_##args(84) FN_##args(85) FN_##args(86) FN_##args(87) FN_##args(88) FN_##args(89) FN_##args(90) FN_##args(91) FN_##args(92) FN_##args(93) FN_##args(94) FN_##args(95) FN_##args(96) FN_##args(97) FN_##args(98) FN_##args(99) FN_##args(100) FN_##args(101) FN_##args(102) FN_##args(103) FN_##args(104) FN_##args(105) FN_##args(106) FN_##args(107) FN_##args(108) FN_##args(109) FN_##args(110) FN_##args(111) FN_##args(112) FN_##args(113) FN_##args(114) FN_##args(115) FN_##args(116) FN_##args(117) FN_##args(118) FN_##args(119) FN_##args(120) FN_##args(121) FN_##args(122) FN_##args(123) FN_##args(124) FN_##args(125) FN_##args(126) FN_##args(127)

/**
 * Define all callback functions.
 *
 * NOTE: If the maximum number of arguments changes (MaxArgs), the following
 *       has to change accordinglly.
 */
FN_BLOCK(0)
FN_BLOCK(1)
FN_BLOCK(2)
FN_BLOCK(3)
FN_BLOCK(4)
FN_BLOCK(5)
FN_BLOCK(6)
FN_BLOCK(7)
FN_BLOCK(8)
FN_BLOCK(9)
FN_BLOCK(10)
FN_BLOCK(11)
FN_BLOCK(12)

/**
 * Initialize the function pointers for the callback routines.
 *
 * NOTE: If MaxArgs or MaxCallbacks changes, the following has to be updated.
 */
#define FN_A_BLOCK(args) {(void*) FN(0, args),(void*) FN(1, args),(void*) FN(2, args),(void*) FN(3, args),(void*) FN(4, args),(void*) FN(5, args),(void*) FN(6, args),(void*) FN(7, args),(void*) FN(8, args),(void*) FN(9, args),(void*) FN(10, args),(void*) FN(11, args),(void*) FN(12, args),(void*) FN(13, args),(void*) FN(14, args),(void*) FN(15, args),(void*) FN(16, args),(void*) FN(17, args),(void*) FN(18, args),(void*) FN(19, args),(void*) FN(20, args),(void*) FN(21, args),(void*) FN(22, args),(void*) FN(23, args),(void*) FN(24, args),(void*) FN(25, args),(void*) FN(26, args),(void*) FN(27, args),(void*) FN(28, args),(void*) FN(29, args),(void*) FN(30, args),(void*) FN(31, args),(void*) FN(32, args),(void*) FN(33, args),(void*) FN(34, args),(void*) FN(35, args),(void*) FN(36, args),(void*) FN(37, args),(void*) FN(38, args),(void*) FN(39, args),(void*) FN(40, args),(void*) FN(41, args),(void*) FN(42, args),(void*) FN(43, args),(void*) FN(44, args),(void*) FN(45, args),(void*) FN(46, args),(void*) FN(47, args),(void*) FN(48, args),(void*) FN(49, args),(void*) FN(50, args),(void*) FN(51, args),(void*) FN(52, args),(void*) FN(53, args),(void*) FN(54, args),(void*) FN(55, args),(void*) FN(56, args),(void*) FN(57, args),(void*) FN(58, args),(void*) FN(59, args),(void*) FN(60, args),(void*) FN(61, args),(void*) FN(62, args),(void*) FN(63, args),(void*) FN(64, args),(void*) FN(65, args),(void*) FN(66, args),(void*) FN(67, args),(void*) FN(68, args),(void*) FN(69, args),(void*) FN(70, args),(void*) FN(71, args),(void*) FN(72, args),(void*) FN(73, args),(void*) FN(74, args),(void*) FN(75, args),(void*) FN(76, args),(void*) FN(77, args),(void*) FN(78, args),(void*) FN(79, args),(void*) FN(80, args),(void*) FN(81, args),(void*) FN(82, args),(void*) FN(83, args),(void*) FN(84, args),(void*) FN(85, args),(void*) FN(86, args),(void*) FN(87, args),(void*) FN(88, args),(void*) FN(89, args),(void*) FN(90, args),(void*) FN(91, args),(void*) FN(92, args),(void*) FN(93, args),(void*) FN(94, args),(void*) FN(95, args),(void*) FN(96, args),(void*) FN(97, args),(void*) FN(98, args),(void*) FN(99, args),(void*) FN(100, args),(void*) FN(101, args),(void*) FN(102, args),(void*) FN(103, args),(void*) FN(104, args),(void*) FN(105, args),(void*) FN(106, args),(void*) FN(107, args),(void*) FN(108, args),(void*) FN(109, args),(void*) FN(110, args),(void*) FN(111, args),(void*) FN(112, args),(void*) FN(113, args),(void*) FN(114, args),(void*) FN(115, args),(void*) FN(116, args),(void*) FN(117, args),(void*) FN(118, args),(void*) FN(119, args),(void*) FN(120, args),(void*) FN(121, args),(void*) FN(122, args),(void*) FN(123, args),(void*) FN(124, args),(void*) FN(125, args),(void*) FN(126, args),(void*) FN(127, args)},

void*
procedures[MaxArgs + 1][MaxCallbacks] = { 
	FN_A_BLOCK(0)    
	FN_A_BLOCK(1)    
	FN_A_BLOCK(2)    
	FN_A_BLOCK(3)    
	FN_A_BLOCK(4)    
	FN_A_BLOCK(5)    
	FN_A_BLOCK(6)    
	FN_A_BLOCK(7)    
	FN_A_BLOCK(8)    
	FN_A_BLOCK(9)    
	FN_A_BLOCK(10)    
	FN_A_BLOCK(11)    
	FN_A_BLOCK(12)    
};

} // namespace

JPTR
org::eclipse::swt::internal::CNICallback::
bind(org::eclipse::swt::internal::CNICallback* callback)
{
  for (unsigned i = 0; i < MaxCallbacks; ++i) {
    if (callbacks[i] == 0) {
      callbacks[i] = callback;
      return (JPTR) procedures[callback->argCount][i];
    }
  }
  return 0;
}

JPTR
org::eclipse::swt::internal::CNICallback::
unbind(org::eclipse::swt::internal::CNICallback* callback)
{
  for (unsigned i = 0; i < MaxCallbacks; ++i) {
    if (callback == callbacks[i]) {
      callbacks[i] = 0;
      break;
    }
  }  
}

void
org::eclipse::swt::internal::CNICallback::
setEnabled(jboolean enable)
{
  enabled = enable;
}

jboolean
org::eclipse::swt::internal::CNICallback::
getEnabled()
{
  return enabled;
}
