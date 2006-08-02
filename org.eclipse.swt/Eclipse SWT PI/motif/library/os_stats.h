/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

#ifdef NATIVE_STATS
extern int OS_nativeFunctionCount;
extern int OS_nativeFunctionCallCount[];
extern char* OS_nativeFunctionNames[];
#define OS_NATIVE_ENTER(env, that, func) OS_nativeFunctionCallCount[func]++;
#define OS_NATIVE_EXIT(env, that, func) 
#else
#define OS_NATIVE_ENTER(env, that, func) 
#define OS_NATIVE_EXIT(env, that, func) 
#endif

typedef enum {
	CODESET_FUNC,
	FD_1ISSET_FUNC,
	FD_1SET_FUNC,
	FD_1ZERO_FUNC,
	LC_1CTYPE_FUNC,
	MB_1CUR_1MAX_FUNC,
	MonitorEnter_FUNC,
	MonitorExit_FUNC,
	XRenderPictureAttributes_1sizeof_FUNC,
	_1Call_FUNC,
	_1ConnectionNumber_FUNC,
	_1XAllocColor_FUNC,
	_1XBell_FUNC,
	_1XBlackPixel_FUNC,
	_1XChangeActivePointerGrab_FUNC,
	_1XChangeProperty_FUNC,
	_1XChangeWindowAttributes_FUNC,
	_1XCheckIfEvent_FUNC,
	_1XCheckMaskEvent_FUNC,
	_1XCheckWindowEvent_FUNC,
	_1XClearArea_FUNC,
	_1XClipBox_FUNC,
	_1XCloseDisplay_FUNC,
	_1XCopyArea_FUNC,
	_1XCopyPlane_FUNC,
	_1XCreateBitmapFromData_FUNC,
	_1XCreateColormap_FUNC,
	_1XCreateFontCursor_FUNC,
	_1XCreateGC_FUNC,
	_1XCreateImage_FUNC,
	_1XCreatePixmap_FUNC,
	_1XCreatePixmapCursor_FUNC,
	_1XCreateRegion_FUNC,
	_1XCreateWindow_FUNC,
	_1XDefaultColormap_FUNC,
	_1XDefaultColormapOfScreen_FUNC,
	_1XDefaultDepthOfScreen_FUNC,
	_1XDefaultGCOfScreen_FUNC,
	_1XDefaultRootWindow_FUNC,
	_1XDefaultScreen_FUNC,
	_1XDefaultScreenOfDisplay_FUNC,
	_1XDefaultVisual_FUNC,
	_1XDefineCursor_FUNC,
	_1XDestroyImage_FUNC,
	_1XDestroyRegion_FUNC,
	_1XDestroyWindow_FUNC,
	_1XDisplayHeight_FUNC,
	_1XDisplayHeightMM_FUNC,
	_1XDisplayWidth_FUNC,
	_1XDisplayWidthMM_FUNC,
	_1XDrawArc_FUNC,
	_1XDrawLine_FUNC,
	_1XDrawLines_FUNC,
	_1XDrawPoint_FUNC,
	_1XDrawRectangle_FUNC,
	_1XEmptyRegion_FUNC,
	_1XEventsQueued_FUNC,
	_1XFillArc_FUNC,
	_1XFillPolygon_FUNC,
	_1XFillRectangle_FUNC,
	_1XFilterEvent_FUNC,
	_1XFlush_FUNC,
	_1XFontsOfFontSet_FUNC,
	_1XFree_FUNC,
	_1XFreeColormap_FUNC,
	_1XFreeColors_FUNC,
	_1XFreeCursor_FUNC,
	_1XFreeFont_FUNC,
	_1XFreeFontNames_FUNC,
	_1XFreeGC_FUNC,
	_1XFreeModifiermap_FUNC,
	_1XFreePixmap_FUNC,
	_1XFreeStringList_FUNC,
	_1XGetGCValues_FUNC,
	_1XGetGeometry_FUNC,
	_1XGetIconSizes_FUNC,
	_1XGetImage_FUNC,
	_1XGetInputFocus_FUNC,
	_1XGetModifierMapping_FUNC,
	_1XGetWindowAttributes_FUNC,
	_1XGetWindowProperty_FUNC,
	_1XGrabKeyboard_FUNC,
	_1XGrabPointer_FUNC,
	_1XInitThreads_FUNC,
	_1XInternAtom_FUNC,
	_1XIntersectRegion_FUNC,
	_1XKeysymToKeycode_FUNC,
	_1XKeysymToString_FUNC,
	_1XListFonts_FUNC,
	_1XListProperties_FUNC,
	_1XLocaleOfFontSet_FUNC,
	_1XLookupString_FUNC,
	_1XLowerWindow_FUNC,
	_1XMapWindow_FUNC,
	_1XMoveResizeWindow_FUNC,
	_1XOffsetRegion_FUNC,
	_1XOpenDisplay_FUNC,
	_1XPointInRegion_FUNC,
	_1XPolygonRegion_FUNC,
	_1XPutImage_FUNC,
	_1XQueryBestCursor_FUNC,
	_1XQueryColor_FUNC,
	_1XQueryPointer_FUNC,
	_1XQueryTree_FUNC,
	_1XRaiseWindow_FUNC,
	_1XReconfigureWMWindow_FUNC,
	_1XRectInRegion_FUNC,
	_1XRenderComposite_FUNC,
	_1XRenderCreatePicture_FUNC,
	_1XRenderFindStandardFormat_FUNC,
	_1XRenderFindVisualFormat_FUNC,
	_1XRenderFreePicture_FUNC,
	_1XRenderQueryExtension_FUNC,
	_1XRenderQueryVersion_FUNC,
	_1XRenderSetPictureClipRectangles_FUNC,
	_1XRenderSetPictureClipRegion_FUNC,
	_1XRenderSetPictureTransform_FUNC,
	_1XReparentWindow_FUNC,
	_1XResizeWindow_FUNC,
	_1XRootWindowOfScreen_FUNC,
	_1XSelectInput_FUNC,
	_1XSendEvent_FUNC,
	_1XSetBackground_FUNC,
	_1XSetClipMask_FUNC,
	_1XSetClipRectangles_FUNC,
	_1XSetDashes_FUNC,
	_1XSetErrorHandler_FUNC,
	_1XSetFillRule_FUNC,
	_1XSetFillStyle_FUNC,
	_1XSetForeground_FUNC,
	_1XSetFunction_FUNC,
	_1XSetGraphicsExposures_FUNC,
	_1XSetIOErrorHandler_FUNC,
	_1XSetInputFocus_FUNC,
	_1XSetLineAttributes_FUNC,
	_1XSetRegion_FUNC,
	_1XSetStipple_FUNC,
	_1XSetSubwindowMode_FUNC,
	_1XSetTSOrigin_FUNC,
	_1XSetTile_FUNC,
	_1XSetWMNormalHints_FUNC,
	_1XSetWindowBackgroundPixmap_FUNC,
	_1XShapeCombineMask_FUNC,
	_1XShapeCombineRegion_FUNC,
	_1XSubtractRegion_FUNC,
	_1XSync_FUNC,
	_1XSynchronize_FUNC,
	_1XTestFakeButtonEvent_FUNC,
	_1XTestFakeKeyEvent_FUNC,
	_1XTestFakeMotionEvent_FUNC,
	_1XTranslateCoordinates_FUNC,
	_1XUndefineCursor_FUNC,
	_1XUngrabKeyboard_FUNC,
	_1XUngrabPointer_FUNC,
	_1XUnionRectWithRegion_FUNC,
	_1XUnionRegion_FUNC,
	_1XUnmapWindow_FUNC,
	_1XWarpPointer_FUNC,
	_1XWhitePixel_FUNC,
	_1XWithdrawWindow_FUNC,
	_1XineramaIsActive_FUNC,
	_1XineramaQueryScreens_FUNC,
	_1XmAddWMProtocolCallback_FUNC,
	_1XmChangeColor_FUNC,
	_1XmClipboardCopy_FUNC,
	_1XmClipboardEndCopy_FUNC,
	_1XmClipboardEndRetrieve_FUNC,
	_1XmClipboardInquireCount_FUNC,
	_1XmClipboardInquireFormat_FUNC,
	_1XmClipboardInquireLength_FUNC,
	_1XmClipboardRetrieve_FUNC,
	_1XmClipboardStartCopy_FUNC,
	_1XmClipboardStartRetrieve_FUNC,
	_1XmComboBoxAddItem_FUNC,
	_1XmComboBoxDeletePos_FUNC,
	_1XmComboBoxSelectItem_FUNC,
	_1XmCreateArrowButton_FUNC,
	_1XmCreateCascadeButtonGadget_FUNC,
	_1XmCreateComboBox_FUNC,
	_1XmCreateDialogShell_FUNC,
	_1XmCreateDrawingArea_FUNC,
	_1XmCreateDrawnButton_FUNC,
	_1XmCreateErrorDialog_FUNC,
	_1XmCreateFileSelectionDialog_FUNC,
	_1XmCreateForm_FUNC,
	_1XmCreateFrame_FUNC,
	_1XmCreateInformationDialog_FUNC,
	_1XmCreateLabel_FUNC,
	_1XmCreateList_FUNC,
	_1XmCreateMainWindow_FUNC,
	_1XmCreateMenuBar_FUNC,
	_1XmCreateMessageDialog_FUNC,
	_1XmCreatePopupMenu_FUNC,
	_1XmCreatePulldownMenu_FUNC,
	_1XmCreatePushButton_FUNC,
	_1XmCreatePushButtonGadget_FUNC,
	_1XmCreateQuestionDialog_FUNC,
	_1XmCreateScale_FUNC,
	_1XmCreateScrollBar_FUNC,
	_1XmCreateScrolledList_FUNC,
	_1XmCreateScrolledText_FUNC,
	_1XmCreateSeparator_FUNC,
	_1XmCreateSeparatorGadget_FUNC,
	_1XmCreateSimpleSpinBox_FUNC,
	_1XmCreateTextField_FUNC,
	_1XmCreateToggleButton_FUNC,
	_1XmCreateToggleButtonGadget_FUNC,
	_1XmCreateWarningDialog_FUNC,
	_1XmCreateWorkingDialog_FUNC,
	_1XmDestroyPixmap_FUNC,
	_1XmDragCancel_FUNC,
	_1XmDragStart_FUNC,
	_1XmDropSiteRegister_FUNC,
	_1XmDropSiteUnregister_FUNC,
	_1XmDropSiteUpdate_FUNC,
	_1XmDropTransferAdd_FUNC,
	_1XmDropTransferStart_FUNC,
	_1XmFileSelectionBoxGetChild_FUNC,
	_1XmFontListAppendEntry_FUNC,
	_1XmFontListCopy_FUNC,
	_1XmFontListEntryFree_FUNC,
	_1XmFontListEntryGetFont_FUNC,
	_1XmFontListEntryLoad_FUNC,
	_1XmFontListFree_FUNC,
	_1XmFontListFreeFontContext_FUNC,
	_1XmFontListInitFontContext_FUNC,
	_1XmFontListNextEntry_FUNC,
	_1XmGetAtomName_FUNC,
	_1XmGetDragContext_FUNC,
	_1XmGetFocusWidget_FUNC,
	_1XmGetPixmap_FUNC,
	_1XmGetPixmapByDepth_FUNC,
	_1XmGetXmDisplay_FUNC,
	_1XmImMbLookupString_FUNC,
	_1XmImRegister_FUNC,
	_1XmImSetFocusValues_FUNC,
	_1XmImSetValues_FUNC,
	_1XmImUnregister_FUNC,
	_1XmImUnsetFocus_FUNC,
	_1XmInternAtom_FUNC,
	_1XmListAddItemUnselected_FUNC,
	_1XmListDeleteAllItems_FUNC,
	_1XmListDeleteItemsPos_FUNC,
	_1XmListDeletePos_FUNC,
	_1XmListDeletePositions_FUNC,
	_1XmListDeselectAllItems_FUNC,
	_1XmListDeselectPos_FUNC,
	_1XmListGetKbdItemPos_FUNC,
	_1XmListGetSelectedPos_FUNC,
	_1XmListItemPos_FUNC,
	_1XmListPosSelected_FUNC,
	_1XmListReplaceItemsPosUnselected_FUNC,
	_1XmListSelectPos_FUNC,
	_1XmListSetKbdItemPos_FUNC,
	_1XmListSetPos_FUNC,
	_1XmListUpdateSelectedList_FUNC,
	_1XmMainWindowSetAreas_FUNC,
	_1XmMessageBoxGetChild_FUNC,
	_1XmParseMappingCreate_FUNC,
	_1XmParseMappingFree_FUNC,
	_1XmProcessTraversal_FUNC,
	_1XmRenderTableAddRenditions_FUNC,
	_1XmRenderTableFree_FUNC,
	_1XmRenditionCreate_FUNC,
	_1XmRenditionFree_FUNC,
	_1XmStringBaseline_FUNC,
	_1XmStringCompare_FUNC,
	_1XmStringComponentCreate_FUNC,
	_1XmStringConcat_FUNC,
	_1XmStringCreate_FUNC,
	_1XmStringCreateLocalized_FUNC,
	_1XmStringDraw_FUNC,
	_1XmStringDrawImage_FUNC,
	_1XmStringDrawUnderline_FUNC,
	_1XmStringEmpty_FUNC,
	_1XmStringExtent_FUNC,
	_1XmStringFree_FUNC,
	_1XmStringGenerate_FUNC,
	_1XmStringHeight_FUNC,
	_1XmStringParseText_FUNC,
	_1XmStringUnparse_FUNC,
	_1XmStringWidth_FUNC,
	_1XmTabCreate_FUNC,
	_1XmTabFree_FUNC,
	_1XmTabListFree_FUNC,
	_1XmTabListInsertTabs_FUNC,
	_1XmTextClearSelection_FUNC,
	_1XmTextCopy_FUNC,
	_1XmTextCut_FUNC,
	_1XmTextDisableRedisplay_FUNC,
	_1XmTextEnableRedisplay_FUNC,
	_1XmTextFieldPaste_FUNC,
	_1XmTextGetInsertionPosition_FUNC,
	_1XmTextGetLastPosition_FUNC,
	_1XmTextGetMaxLength_FUNC,
	_1XmTextGetSelection_FUNC,
	_1XmTextGetSelectionPosition_FUNC,
	_1XmTextGetString_FUNC,
	_1XmTextGetSubstring_FUNC,
	_1XmTextGetSubstringWcs_FUNC,
	_1XmTextInsert_FUNC,
	_1XmTextPaste_FUNC,
	_1XmTextPosToXY_FUNC,
	_1XmTextReplace_FUNC,
	_1XmTextScroll_FUNC,
	_1XmTextSetEditable_FUNC,
	_1XmTextSetHighlight_FUNC,
	_1XmTextSetInsertionPosition_FUNC,
	_1XmTextSetMaxLength_FUNC,
	_1XmTextSetSelection_FUNC,
	_1XmTextSetString_FUNC,
	_1XmTextShowPosition_FUNC,
	_1XmTextXYToPos_FUNC,
	_1XmUpdateDisplay_FUNC,
	_1XmWidgetGetDisplayRect_FUNC,
	_1XmbTextListToTextProperty_FUNC,
	_1XmbTextPropertyToTextList_FUNC,
	_1XpCancelJob_FUNC,
	_1XpCreateContext_FUNC,
	_1XpDestroyContext_FUNC,
	_1XpEndJob_FUNC,
	_1XpEndPage_FUNC,
	_1XpFreePrinterList_FUNC,
	_1XpGetOneAttribute_FUNC,
	_1XpGetPageDimensions_FUNC,
	_1XpGetPrinterList_FUNC,
	_1XpGetScreenOfContext_FUNC,
	_1XpSetAttributes_FUNC,
	_1XpSetContext_FUNC,
	_1XpStartJob_FUNC,
	_1XpStartPage_FUNC,
	_1XtAddCallback_FUNC,
	_1XtAddEventHandler_FUNC,
	_1XtAddExposureToRegion_FUNC,
	_1XtAppAddInput_FUNC,
	_1XtAppAddTimeOut_FUNC,
	_1XtAppCreateShell_FUNC,
	_1XtAppGetSelectionTimeout_FUNC,
	_1XtAppNextEvent_FUNC,
	_1XtAppPeekEvent_FUNC,
	_1XtAppPending_FUNC,
	_1XtAppProcessEvent_FUNC,
	_1XtAppSetErrorHandler_FUNC,
	_1XtAppSetFallbackResources_FUNC,
	_1XtAppSetSelectionTimeout_FUNC,
	_1XtAppSetWarningHandler_FUNC,
	_1XtBuildEventMask_FUNC,
	_1XtCallActionProc_FUNC,
	_1XtClass_FUNC,
	_1XtConfigureWidget_FUNC,
	_1XtCreateApplicationContext_FUNC,
	_1XtCreatePopupShell_FUNC,
	_1XtDestroyApplicationContext_FUNC,
	_1XtDestroyWidget_FUNC,
	_1XtDisownSelection_FUNC,
	_1XtDispatchEvent_FUNC,
	_1XtDisplay_FUNC,
	_1XtDisplayToApplicationContext_FUNC,
	_1XtFree_FUNC,
	_1XtGetMultiClickTime_FUNC,
	_1XtGetSelectionValue_FUNC,
	_1XtGetValues_FUNC,
	_1XtInsertEventHandler_FUNC,
	_1XtIsManaged_FUNC,
	_1XtIsRealized_FUNC,
	_1XtIsSubclass_FUNC,
	_1XtIsTopLevelShell_FUNC,
	_1XtLastTimestampProcessed_FUNC,
	_1XtMalloc_FUNC,
	_1XtManageChild_FUNC,
	_1XtMapWidget_FUNC,
	_1XtMoveWidget_FUNC,
	_1XtNameToWidget_FUNC,
	_1XtOpenDisplay_FUNC,
	_1XtOverrideTranslations_FUNC,
	_1XtOwnSelection_FUNC,
	_1XtParent_FUNC,
	_1XtParseTranslationTable_FUNC,
	_1XtPopdown_FUNC,
	_1XtPopup_FUNC,
	_1XtQueryGeometry_FUNC,
	_1XtRealizeWidget_FUNC,
	_1XtRegisterDrawable_FUNC,
	_1XtRemoveEventHandler_FUNC,
	_1XtRemoveInput_FUNC,
	_1XtRemoveTimeOut_FUNC,
	_1XtResizeWidget_FUNC,
	_1XtResizeWindow_FUNC,
	_1XtSetLanguageProc_FUNC,
	_1XtSetMappedWhenManaged_FUNC,
	_1XtSetValues_FUNC,
	_1XtToolkitInitialize_FUNC,
	_1XtToolkitThreadInitialize_FUNC,
	_1XtTranslateCoords_FUNC,
	_1XtUnmanageChild_FUNC,
	_1XtUnmapWidget_FUNC,
	_1XtUnregisterDrawable_FUNC,
	_1XtWindow_FUNC,
	_1XtWindowToWidget_FUNC,
	_1_1XmSetMenuTraversal_FUNC,
	_1applicationShellWidgetClass_FUNC,
	_1dlclose_FUNC,
	_1dlopen_FUNC,
	_1dlsym_FUNC,
	_1overrideShellWidgetClass_FUNC,
	_1shellWidgetClass_FUNC,
	_1topLevelShellWidgetClass_FUNC,
	_1transientShellWidgetClass_FUNC,
	_1xmMenuShellWidgetClass_FUNC,
	close_FUNC,
	fd_1set_1sizeof_FUNC,
	getenv_FUNC,
	iconv_FUNC,
	iconv_1close_FUNC,
	iconv_1open_FUNC,
	localeconv_1decimal_1point_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XButtonEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XClientMessageEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XConfigureEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XExposeEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XImage_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XKeyEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XmDragProcCallbackStruct_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XmSpinBoxCallbackStruct_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XmTextBlockRec_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_motif_XmTextVerifyCallbackStruct_2I_FUNC,
	memmove__I_3BI_FUNC,
	memmove__I_3CI_FUNC,
	memmove__I_3II_FUNC,
	memmove__I_3SI_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_Visual_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XAnyEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XButtonEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XCharStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XClientMessageEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XConfigureEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XCreateWindowEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XCrossingEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XDestroyWindowEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XExposeEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XFocusChangeEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XFontStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XIconSize_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XImage_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XKeyEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XModifierKeymap_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XMotionEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XPropertyEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XReparentEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XineramaScreenInfo_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmAnyCallbackStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmDragProcCallbackStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmDropFinishCallbackStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmDropProcCallbackStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmSpinBoxCallbackStruct_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmTextBlockRec_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_motif_XmTextVerifyCallbackStruct_2II_FUNC,
	memmove___3BII_FUNC,
	memmove___3CII_FUNC,
	memmove___3III_FUNC,
	nl_1langinfo_FUNC,
	pipe_FUNC,
	read_FUNC,
	select_FUNC,
	setResourceMem_FUNC,
	setlocale_FUNC,
	strlen_FUNC,
	write_FUNC,
} OS_FUNCS;