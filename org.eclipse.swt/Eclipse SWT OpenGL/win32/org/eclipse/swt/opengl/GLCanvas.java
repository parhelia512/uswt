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
package org.eclipse.swt.opengl;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.internal.opengl.win32.*;

/**
 * GLCanvas is a widget capable of displaying OpenGL content.
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */

public class GLCanvas extends Canvas {
	int context;
	int pixelFormat;

/**
 * Create a GLCanvas widget using the attributes described in the GLData
 * object provided.
 *
 * @param parent a composite widget
 * @param style the bitwise OR'ing of widget styles
 * @param data the requested attributes of the GLCanvas
 *
 * @exception IllegalArgumentException
 * <ul><li>ERROR_NULL_ARGUMENT when the data is null
 *     <li>ERROR_UNSUPPORTED_DEPTH when the requested attributes cannot be provided</ul> 
 * </ul>
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */
public GLCanvas (Composite parent, int style, GLData data) {
	super (parent, style);
	if (data == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	PIXELFORMATDESCRIPTOR pfd = new PIXELFORMATDESCRIPTOR ();
	pfd.nSize = (short) PIXELFORMATDESCRIPTOR.sizeof;
	pfd.nVersion = 1;
	pfd.dwFlags = WGL.PFD_DRAW_TO_WINDOW | WGL.PFD_SUPPORT_OPENGL;
	pfd.dwLayerMask = WGL.PFD_MAIN_PLANE;
	pfd.iPixelType = (byte) WGL.PFD_TYPE_RGBA;
	if (data.doubleBuffer) pfd.dwFlags |= WGL.PFD_DOUBLEBUFFER;
	if (data.stereo) pfd.dwFlags |= WGL.PFD_STEREO;
	pfd.cRedBits = (byte) data.redSize;
	pfd.cGreenBits = (byte) data.greenSize;
	pfd.cBlueBits = (byte) data.blueSize;
	pfd.cAlphaBits = (byte) data.alphaSize;
	pfd.cDepthBits = (byte) data.depthSize;
	pfd.cStencilBits = (byte) data.stencilSize;
	pfd.cAccumRedBits = (byte) data.accumRedSize;
	pfd.cAccumGreenBits = (byte) data.accumGreenSize;
	pfd.cAccumBlueBits = (byte) data.accumBlueSize;
	pfd.cAccumAlphaBits = (byte) data.accumAlphaSize;
	pfd.cAccumBits = (byte) (pfd.cAccumRedBits + pfd.cAccumGreenBits + pfd.cAccumBlueBits + pfd.cAccumAlphaBits);
	//FIXME - use wglChoosePixelFormatARB
//	if (data.sampleBuffers > 0) {
//		wglAttrib [pos++] = WGL.WGL_SAMPLE_BUFFERS_ARB;
//		wglAttrib [pos++] = data.sampleBuffers;
//	}
//	if (data.samples > 0) {
//		wglAttrib [pos++] = WGL.WGL_SAMPLES_ARB;
//		wglAttrib [pos++] = data.samples;
//	}

	int hDC = OS.GetDC (handle);
	pixelFormat = WGL.ChoosePixelFormat (hDC, pfd);
	if (pixelFormat == 0 || !WGL.SetPixelFormat (hDC, pixelFormat, pfd)) {
		OS.ReleaseDC (handle, hDC);
		dispose ();
		SWT.error (SWT.ERROR_UNSUPPORTED_DEPTH);
	}
	context = WGL.wglCreateContext (hDC);
	if (context == 0) {
		OS.ReleaseDC (handle, hDC);
		SWT.error (SWT.ERROR_NO_HANDLES);
	}
	OS.ReleaseDC (handle, hDC);
	//FIXME- share lists
	//if (share != null) WGL.wglShareLists (context, share.context);
	
	Listener listener = new Listener () {
		public void handleEvent (Event event) {
			switch (event.type) {
			case SWT.Dispose:
				WGL.wglDeleteContext (context);
				break;
			}
		}
	};
	addListener (SWT.Dispose, listener);
}

/**
 * Returns a GLData object describing the created context.
 *  
 * @return GLData description of the OpenGL context attributes
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */
public GLData getGLData () {
	checkWidget ();
	GLData data = new GLData ();
	PIXELFORMATDESCRIPTOR pfd = new PIXELFORMATDESCRIPTOR ();
	pfd.nSize = (short) PIXELFORMATDESCRIPTOR.sizeof;
	int hDC = OS.GetDC (handle);
	WGL.DescribePixelFormat (hDC, pixelFormat, PIXELFORMATDESCRIPTOR.sizeof, pfd);
	OS.ReleaseDC (handle, hDC);
	data.doubleBuffer = (pfd.dwFlags & WGL.PFD_DOUBLEBUFFER) != 0;
	data.stereo = (pfd.dwFlags & WGL.PFD_STEREO) != 0;
	data.redSize = pfd.cRedBits;
	data.greenSize = pfd.cGreenBits;
	data.blueSize = pfd.cBlueBits;
	data.alphaSize = pfd.cAlphaBits;
	data.depthSize = pfd.cDepthBits;
	data.stencilSize = pfd.cStencilBits;
	data.accumRedSize = pfd.cAccumRedBits;
	data.accumGreenSize = pfd.cAccumGreenBits;
	data.accumBlueSize = pfd.cAccumBlueBits;
	data.accumAlphaSize = pfd.cAccumAlphaBits;
	return data;
}

/**
 * Returns a boolean indicating whether the receiver's OpenGL context
 * is the current context.
 *  
 * @return true if the receiver holds the current OpenGL context,
 * false otherwise
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */
public boolean isCurrent () {
	checkWidget ();
	return WGL.wglGetCurrentContext () == handle;
}

/**
 * Sets the OpenGL context associated with this GLCanvas to be the
 * current GL context.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */
public void setCurrent () {
	checkWidget ();
	if (WGL.wglGetCurrentContext () == handle) return;
	int hDC = OS.GetDC (handle);
	WGL.wglMakeCurrent (hDC, context);
	OS.ReleaseDC (handle, hDC);
}

/**
 * Swaps the front and back color buffers.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * WARNING API STILL UNDER CONSTRUCTION AND SUBJECT TO CHANGE
 * 
 * @since 3.2
 */
public void swapBuffers () {
	checkWidget ();
	int hDC = OS.GetDC (handle);
	WGL.SwapBuffers (hDC);
	OS.ReleaseDC (handle, hDC);
}
}