/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

#ifndef INC_os_H
#define INC_os_H

/*#define NDEBUG*/

#include <Carbon/Carbon.h>

#include "os_custom.h"

#ifdef __cplusplus
extern "C" {
#endif

extern jint CPSEnableForegroundOperation(jint *, jint, jint, jint, jint);
extern jint CPSSetProcessName(jint *, jbyte *);

#ifdef __cplusplus
} // extern "C"
#endif

#endif /* INC_os_H */
