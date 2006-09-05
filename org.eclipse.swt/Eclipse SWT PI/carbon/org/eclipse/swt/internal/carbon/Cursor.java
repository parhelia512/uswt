/**********************************************************************
 * Copyright (c) 2003, 2006 IBM Corp.
 * Portions Copyright (c) 1983-2002, Apple Computer, Inc.
 *
 * All rights reserved.  This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 **********************************************************************/
package org.eclipse.swt.internal.carbon;


public class Cursor {
/*#if USWT
	public short[] data = new short[16];
	public short[] mask = new short[16];
  #else*/
	public byte[] data = new byte[16 * 2];
	public byte[] mask = new byte[16 * 2];
/*#endif*/
	public short hotSpot_v;
	public short hotSpot_h;
	public static final int sizeof = 68;
}
