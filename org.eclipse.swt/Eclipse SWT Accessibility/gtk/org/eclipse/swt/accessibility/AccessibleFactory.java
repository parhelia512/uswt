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
package org.eclipse.swt.accessibility;


import java.util.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.accessibility.gtk.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.*;

class AccessibleFactory {
	int /*long*/ handle;
	int /*long*/ objectParentType;
	int /*long*/ widgetTypeName;
  /*#if USWT
	CNICallback atkObjectFactoryCB_create_accessible;
	CNICallback gTypeInfo_base_init_factory;
    #else*/
	Callback atkObjectFactoryCB_create_accessible;
	Callback gTypeInfo_base_init_factory;
  //#endif
	Hashtable accessibles = new Hashtable (9);

  /*#if USWT
    private final Dispatcher dispatcher = new Dispatcher();
    #endif*/
	
	static final Hashtable Types = new Hashtable (9);
	static final Hashtable Factories = new Hashtable (9);	
	static final int /*long*/ DefaultParentType = OS.g_type_from_name (Converter.wcsToMbcs (null, "GtkAccessible", true)); //$NON-NLS-1$
	static final byte[] FACTORY_PARENTTYPENAME = Converter.wcsToMbcs (null, "AtkObjectFactory", true); //$NON-NLS-1$
	static final byte[] SWT_TYPE_PREFIX = Converter.wcsToMbcs (null, "SWT", false); //$NON-NLS-1$
	static final byte[] CHILD_TYPENAME = Converter.wcsToMbcs (null, "Child", false); //$NON-NLS-1$
	static final byte[] FACTORY_TYPENAME = Converter.wcsToMbcs (null, "SWTFactory", true); //$NON-NLS-1$
	static final int[] actionRoles = {
		ACC.ROLE_CHECKBUTTON, ACC.ROLE_COMBOBOX, ACC.ROLE_LINK,
		ACC.ROLE_MENUITEM, ACC.ROLE_PUSHBUTTON, ACC.ROLE_RADIOBUTTON,
	};
	static final int[] hypertextRoles = {ACC.ROLE_LINK};
	static final int[] selectionRoles = {
		ACC.ROLE_LIST, ACC.ROLE_TABFOLDER, ACC.ROLE_TABLE, ACC.ROLE_TREE,
	};
	static final int[] textRoles = {
		ACC.ROLE_COMBOBOX, ACC.ROLE_LINK, ACC.ROLE_LABEL, ACC.ROLE_TEXT,
	};

	/* AT callbacks*/
  /*#if USWT
	static final CNICallback AtkActionCB_get_keybinding;
	static final CNICallback AtkActionCB_get_name;	
	static final CNICallback AtkComponentCB_get_extents;
	static final CNICallback AtkComponentCB_get_position;
	static final CNICallback AtkComponentCB_get_size;
	static final CNICallback AtkComponentCB_ref_accessible_at_point;
	static final CNICallback AtkHypertextCB_get_link;
	static final CNICallback AtkHypertextCB_get_n_links;
	static final CNICallback AtkHypertextCB_get_link_index;
	static final CNICallback AtkObjectCB_get_description;
	static final CNICallback AtkObjectCB_get_index_in_parent;
	static final CNICallback AtkObjectCB_get_n_children;
	static final CNICallback AtkObjectCB_get_name;
	static final CNICallback AtkObjectCB_get_parent;	
	static final CNICallback AtkObjectCB_get_role;
	static final CNICallback AtkObjectCB_ref_child;
	static final CNICallback AtkObjectCB_ref_state_set;
	static final CNICallback AtkSelectionCB_is_child_selected;
	static final CNICallback AtkSelectionCB_ref_selection;
	static final CNICallback AtkTextCB_get_caret_offset;
	static final CNICallback AtkTextCB_get_n_selections;
	static final CNICallback AtkTextCB_get_selection;
	static final CNICallback AtkTextCB_get_text;
	static final CNICallback AtkTextCB_get_text_after_offset;
	static final CNICallback AtkTextCB_get_text_at_offset;
	static final CNICallback AtkTextCB_get_text_before_offset;
	static final CNICallback AtkTextCB_get_character_at_offset;
	static final CNICallback AtkTextCB_get_character_count;
	static final CNICallback GObjectClass_finalize;
    #else*/
	static final Callback AtkActionCB_get_keybinding;
	static final Callback AtkActionCB_get_name;	
	static final Callback AtkComponentCB_get_extents;
	static final Callback AtkComponentCB_get_position;
	static final Callback AtkComponentCB_get_size;
	static final Callback AtkComponentCB_ref_accessible_at_point;
	static final Callback AtkHypertextCB_get_link;
	static final Callback AtkHypertextCB_get_n_links;
	static final Callback AtkHypertextCB_get_link_index;
	static final Callback AtkObjectCB_get_description;
	static final Callback AtkObjectCB_get_index_in_parent;
	static final Callback AtkObjectCB_get_n_children;
	static final Callback AtkObjectCB_get_name;
	static final Callback AtkObjectCB_get_parent;	
	static final Callback AtkObjectCB_get_role;
	static final Callback AtkObjectCB_ref_child;
	static final Callback AtkObjectCB_ref_state_set;
	static final Callback AtkSelectionCB_is_child_selected;
	static final Callback AtkSelectionCB_ref_selection;
	static final Callback AtkTextCB_get_caret_offset;
	static final Callback AtkTextCB_get_n_selections;
	static final Callback AtkTextCB_get_selection;
	static final Callback AtkTextCB_get_text;
	static final Callback AtkTextCB_get_text_after_offset;
	static final Callback AtkTextCB_get_text_at_offset;
	static final Callback AtkTextCB_get_text_before_offset;
	static final Callback AtkTextCB_get_character_at_offset;
	static final Callback AtkTextCB_get_character_count;
	static final Callback GObjectClass_finalize;
  //endif
	/* interface initialization callbacks */
  /*#if USWT
	static final CNICallback InitActionIfaceCB;		
	static final CNICallback InitComponentIfaceCB;
	static final CNICallback InitHypertextIfaceCB;
	static final CNICallback GTypeInfo_base_init_type;
	static final CNICallback InitSelectionIfaceCB;
	static final CNICallback InitTextIfaceCB;
    #else*/
	static final Callback InitActionIfaceCB;		
	static final Callback InitComponentIfaceCB;
	static final Callback InitHypertextIfaceCB;
	static final Callback GTypeInfo_base_init_type;
	static final Callback InitSelectionIfaceCB;
	static final Callback InitTextIfaceCB;
  //#endif
	/* interface definitions */
	static int /*long*/ ObjectIfaceDefinition;
	static final int /*long*/ ActionIfaceDefinition;
	static final int /*long*/ ComponentIfaceDefinition;
	static final int /*long*/ HypertextIfaceDefinition;
	static final int /*long*/ SelectionIfaceDefinition;
	static final int /*long*/ TextIfaceDefinition;

  /*#if USWT
  private static final int ATKACTION_GET_KEYBINDING = 1;
  private static final int ATKACTION_GET_NAME = 2;
  private static final int ATKCOMPONENT_GET_EXTENTS = 3;
  private static final int ATKCOMPONENT_GET_POSITION = 4;
  private static final int ATKCOMPONENT_GET_SIZE = 5;
  private static final int ATKCOMPONENT_REF_ACCESSIBLE_AT_POINT = 6;
  private static final int ATKHYPERTEXT_GET_LINK = 7;
  private static final int ATKHYPERTEXT_GET_N_LINKS = 8;
  private static final int ATKHYPERTEXT_GET_LINK_INDEX = 9;
  private static final int ATKOBJECT_GET_NAME = 10;
  private static final int ATKOBJECT_GET_DESCRIPTION = 11;
  private static final int ATKOBJECT_GET_N_CHILDREN = 12;
  private static final int ATKOBJECT_GET_ROLE = 13;
  private static final int ATKOBJECT_GET_PARENT = 14;
  private static final int ATKOBJECT_REF_STATE_SET = 15;
  private static final int ATKOBJECT_GET_INDEX_IN_PARENT = 16;
  private static final int ATKOBJECT_REF_CHILD = 17;
  private static final int ATKSELECTION_IS_CHILD_SELECTED = 18;
  private static final int ATKSELECTION_REF_SELECTION = 19;
  private static final int ATKTEXT_GET_CARET_OFFSET = 20;
  private static final int ATKTEXT_GET_N_SELECTIONS = 21;
  private static final int ATKTEXT_GET_SELECTION = 22;
  private static final int ATKTEXT_GET_TEXT = 23;
  private static final int ATKTEXT_GET_TEXT_AFTER_OFFSET = 24;
  private static final int ATKTEXT_GET_TEXT_AT_OFFSET = 25;
  private static final int ATKTEXT_GET_TEXT_BEFORE_OFFSET = 26;
  private static final int ATKTEXT_GET_CHARACTER_AT_OFFSET = 27;
  private static final int ATKTEXT_GET_CHARACTER_COUNT = 28;
  private static final int GOBJECTCLASS_FINALIZE = 29;
  private static final int GTYPEINFO_BASE_INIT_TYPE = 30;
  private static final int INITACTIONIFACECB = 31;
  private static final int INITCOMPONENTIFACECB = 32;
  private static final int INITHYPERTEXTIFACECB = 33;
  private static final int INITSELECTIONIFACECB = 34;
  private static final int INITTEXTIFACECB = 35;
  private static final int GTYPEINFO_BASE_INIT_FACTORY = 36;
  private static final int ATKOBJECTFACTORY_CREATE_ACCESSIBLE = 37;
    #endif*/

	static {
  /*#if USWT
    CNIDispatcher dispatcher = new CNIDispatcher() {
        public int /*long#eoc dispatch(int method, int /*long#eoc[] args) {
          switch (method) {
          case ATKACTION_GET_KEYBINDING:
            return AccessibleObject.atkAction_get_keybinding(args[0], args[1]);

          case ATKACTION_GET_NAME:
            return AccessibleObject.atkAction_get_name(args[0], args[1]);

          case ATKCOMPONENT_GET_EXTENTS:
            return AccessibleObject.atkComponent_get_extents(args[0], args[1], args[2], args[3], args[4], args[5]);

          case ATKCOMPONENT_GET_POSITION:
            return AccessibleObject.atkComponent_get_position(args[0], args[1], args[2], args[3]);

          case ATKCOMPONENT_GET_SIZE:
            return AccessibleObject.atkComponent_get_size(args[0], args[1], args[2], args[3]);

          case ATKCOMPONENT_REF_ACCESSIBLE_AT_POINT:
            return AccessibleObject.atkComponent_ref_accessible_at_point(args[0], args[1], args[2], args[3]);

          case ATKHYPERTEXT_GET_LINK:
            return AccessibleObject.atkHypertext_get_link(args[0], args[1]);

          case ATKHYPERTEXT_GET_N_LINKS:
            return AccessibleObject.atkHypertext_get_n_links(args[0]);

          case ATKHYPERTEXT_GET_LINK_INDEX:
            return AccessibleObject.atkHypertext_get_link_index(args[0], args[1]);

          case ATKOBJECT_GET_NAME:
            return AccessibleObject.atkObject_get_name(args[0]);

          case ATKOBJECT_GET_DESCRIPTION:
            return AccessibleObject.atkObject_get_description(args[0]);

          case ATKOBJECT_GET_N_CHILDREN:
            return AccessibleObject.atkObject_get_n_children(args[0]);

          case ATKOBJECT_GET_ROLE:
            return AccessibleObject.atkObject_get_role(args[0]);

          case ATKOBJECT_GET_PARENT:
            return AccessibleObject.atkObject_get_parent(args[0]);

          case ATKOBJECT_REF_STATE_SET:
            return AccessibleObject.atkObject_ref_state_set(args[0]);

          case ATKOBJECT_GET_INDEX_IN_PARENT:
            return AccessibleObject.atkObject_get_index_in_parent(args[0]);

          case ATKOBJECT_REF_CHILD:
            return AccessibleObject.atkObject_ref_child(args[0], args[1]);

          case ATKSELECTION_IS_CHILD_SELECTED:
            return AccessibleObject.atkSelection_is_child_selected(args[0], args[1]);

          case ATKSELECTION_REF_SELECTION:
            return AccessibleObject.atkSelection_ref_selection(args[0], args[1]);

          case ATKTEXT_GET_CARET_OFFSET:
            return AccessibleObject.atkText_get_caret_offset(args[0]);

          case ATKTEXT_GET_N_SELECTIONS:
            return AccessibleObject.atkText_get_n_selections(args[0]);

          case ATKTEXT_GET_SELECTION:
            return AccessibleObject.atkText_get_selection(args[0], args[1], args[2], args[3]);

          case ATKTEXT_GET_TEXT:
            return AccessibleObject.atkText_get_text(args[0], args[1], args[2]);

          case ATKTEXT_GET_TEXT_AFTER_OFFSET:
            return AccessibleObject.atkText_get_text_after_offset(args[0], args[1], args[2], args[3], args[4]);

          case ATKTEXT_GET_TEXT_AT_OFFSET:
            return AccessibleObject.atkText_get_text_at_offset(args[0], args[1], args[2], args[3], args[4]);

          case ATKTEXT_GET_TEXT_BEFORE_OFFSET:
            return AccessibleObject.atkText_get_text_before_offset(args[0], args[1], args[2], args[3], args[4]);

          case ATKTEXT_GET_CHARACTER_AT_OFFSET:
            return AccessibleObject.atkText_get_character_at_offset(args[0], args[1]);

          case ATKTEXT_GET_CHARACTER_COUNT:
            return AccessibleObject.atkText_get_character_count(args[0]);

          case GOBJECTCLASS_FINALIZE:
            return AccessibleObject.gObjectClass_finalize(args[0]);

          case GTYPEINFO_BASE_INIT_TYPE:
            return gTypeInfo_base_init_type(args[0]);

          case INITACTIONIFACECB:
            return initActionIfaceCB(args[0]);

          case INITCOMPONENTIFACECB:
            return initComponentIfaceCB(args[0]);

          case INITHYPERTEXTIFACECB:
            return initHypertextIfaceCB(args[0]);

          case INITSELECTIONIFACECB:
            return initSelectionIfaceCB(args[0]);

          case INITTEXTIFACECB:
            return initTextIfaceCB(args[0]);

          default: throw new IllegalArgumentException();
          }
        }
      };

		AtkActionCB_get_keybinding = newCallback (dispatcher, ATKACTION_GET_KEYBINDING, 2);
		AtkActionCB_get_name = newCallback (dispatcher, ATKACTION_GET_NAME, 2);
		AtkComponentCB_get_extents = newCallback (dispatcher, ATKCOMPONENT_GET_EXTENTS, 6);
		AtkComponentCB_get_position = newCallback (dispatcher, ATKCOMPONENT_GET_POSITION, 4);
		AtkComponentCB_get_size = newCallback (dispatcher, ATKCOMPONENT_GET_SIZE, 4);
		AtkComponentCB_ref_accessible_at_point = newCallback (dispatcher, ATKCOMPONENT_REF_ACCESSIBLE_AT_POINT, 4);
		AtkHypertextCB_get_link = newCallback (dispatcher, ATKHYPERTEXT_GET_LINK, 2);
		AtkHypertextCB_get_n_links = newCallback (dispatcher, ATKHYPERTEXT_GET_N_LINKS, 1);
		AtkHypertextCB_get_link_index = newCallback (dispatcher, ATKHYPERTEXT_GET_LINK_INDEX, 2);
		AtkObjectCB_get_name = newCallback (dispatcher, ATKOBJECT_GET_NAME, 1);
		AtkObjectCB_get_description = newCallback (dispatcher, ATKOBJECT_GET_DESCRIPTION, 1);
		AtkObjectCB_get_n_children = newCallback (dispatcher, ATKOBJECT_GET_N_CHILDREN, 1);
		AtkObjectCB_get_role = newCallback (dispatcher, ATKOBJECT_GET_ROLE, 1);
		AtkObjectCB_get_parent = newCallback (dispatcher, ATKOBJECT_GET_PARENT, 1);
		AtkObjectCB_ref_state_set = newCallback (dispatcher, ATKOBJECT_REF_STATE_SET, 1);
		AtkObjectCB_get_index_in_parent = newCallback (dispatcher, ATKOBJECT_GET_INDEX_IN_PARENT, 1);
		AtkObjectCB_ref_child = newCallback (dispatcher, ATKOBJECT_REF_CHILD, 2);
		AtkSelectionCB_is_child_selected = newCallback (dispatcher, ATKSELECTION_IS_CHILD_SELECTED, 2);
		AtkSelectionCB_ref_selection = newCallback (dispatcher, ATKSELECTION_REF_SELECTION, 2);
		AtkTextCB_get_caret_offset = newCallback (dispatcher, ATKTEXT_GET_CARET_OFFSET, 1);
		AtkTextCB_get_n_selections = newCallback (dispatcher, ATKTEXT_GET_N_SELECTIONS, 1);
		AtkTextCB_get_selection = newCallback (dispatcher, ATKTEXT_GET_SELECTION, 4);
		AtkTextCB_get_text = newCallback (dispatcher, ATKTEXT_GET_TEXT, 3);
		AtkTextCB_get_text_after_offset = newCallback (dispatcher, ATKTEXT_GET_TEXT_AFTER_OFFSET, 5);
		AtkTextCB_get_text_at_offset = newCallback ( dispatcher, ATKTEXT_GET_TEXT_AT_OFFSET, 5);
		AtkTextCB_get_text_before_offset = newCallback (dispatcher, ATKTEXT_GET_TEXT_BEFORE_OFFSET, 5);
		AtkTextCB_get_character_at_offset = newCallback (dispatcher, ATKTEXT_GET_CHARACTER_AT_OFFSET, 2);
		AtkTextCB_get_character_count = newCallback (dispatcher, ATKTEXT_GET_CHARACTER_COUNT, 1);
		GObjectClass_finalize = newCallback (dispatcher, GOBJECTCLASS_FINALIZE, 1);
		GTypeInfo_base_init_type = newCallback (dispatcher, GTYPEINFO_BASE_INIT_TYPE, 1);
    #else*/
		AtkActionCB_get_keybinding = newCallback (AccessibleObject.class, "atkAction_get_keybinding", 2); //$NON-NLS-1$
		AtkActionCB_get_name = newCallback (AccessibleObject.class, "atkAction_get_name", 2); //$NON-NLS-1$
		AtkComponentCB_get_extents = newCallback (AccessibleObject.class, "atkComponent_get_extents", 6); //$NON-NLS-1$
		AtkComponentCB_get_position = newCallback (AccessibleObject.class, "atkComponent_get_position", 4); //$NON-NLS-1$
		AtkComponentCB_get_size = newCallback (AccessibleObject.class, "atkComponent_get_size", 4); //$NON-NLS-1$
		AtkComponentCB_ref_accessible_at_point = newCallback (AccessibleObject.class, "atkComponent_ref_accessible_at_point", 4); //$NON-NLS-1$
		AtkHypertextCB_get_link = newCallback (AccessibleObject.class, "atkHypertext_get_link", 2); //$NON-NLS-1$
		AtkHypertextCB_get_n_links = newCallback (AccessibleObject.class, "atkHypertext_get_n_links", 1); //$NON-NLS-1$
		AtkHypertextCB_get_link_index = newCallback (AccessibleObject.class, "atkHypertext_get_link_index", 2); //$NON-NLS-1$
		AtkObjectCB_get_name = newCallback (AccessibleObject.class, "atkObject_get_name", 1); //$NON-NLS-1$
		AtkObjectCB_get_description = newCallback (AccessibleObject.class, "atkObject_get_description", 1); //$NON-NLS-1$
		AtkObjectCB_get_n_children = newCallback (AccessibleObject.class, "atkObject_get_n_children", 1); //$NON-NLS-1$
		AtkObjectCB_get_role = newCallback (AccessibleObject.class, "atkObject_get_role", 1); //$NON-NLS-1$
		AtkObjectCB_get_parent = newCallback (AccessibleObject.class, "atkObject_get_parent", 1); //$NON-NLS-1$
		AtkObjectCB_ref_state_set = newCallback (AccessibleObject.class, "atkObject_ref_state_set", 1); //$NON-NLS-1$
		AtkObjectCB_get_index_in_parent = newCallback (AccessibleObject.class, "atkObject_get_index_in_parent", 1); //$NON-NLS-1$
		AtkObjectCB_ref_child = newCallback (AccessibleObject.class, "atkObject_ref_child", 2); //$NON-NLS-1$
		AtkSelectionCB_is_child_selected = newCallback (AccessibleObject.class, "atkSelection_is_child_selected", 2); //$NON-NLS-1$
		AtkSelectionCB_ref_selection = newCallback (AccessibleObject.class, "atkSelection_ref_selection", 2); //$NON-NLS-1$
		AtkTextCB_get_caret_offset = newCallback (AccessibleObject.class, "atkText_get_caret_offset", 1); //$NON-NLS-1$
		AtkTextCB_get_n_selections = newCallback (AccessibleObject.class, "atkText_get_n_selections", 1); //$NON-NLS-1$
		AtkTextCB_get_selection = newCallback (AccessibleObject.class, "atkText_get_selection", 4); //$NON-NLS-1$
		AtkTextCB_get_text = newCallback (AccessibleObject.class, "atkText_get_text", 3); //$NON-NLS-1$
		AtkTextCB_get_text_after_offset = newCallback (AccessibleObject.class, "atkText_get_text_after_offset", 5); //$NON-NLS-1$
		AtkTextCB_get_text_at_offset = newCallback ( AccessibleObject.class, "atkText_get_text_at_offset", 5); //$NON-NLS-1$
		AtkTextCB_get_text_before_offset = newCallback (AccessibleObject.class, "atkText_get_text_before_offset", 5); //$NON-NLS-1$
		AtkTextCB_get_character_at_offset = newCallback (AccessibleObject.class, "atkText_get_character_at_offset", 2); //$NON-NLS-1$
		AtkTextCB_get_character_count = newCallback (AccessibleObject.class, "atkText_get_character_count", 1); //$NON-NLS-1$
		GObjectClass_finalize = newCallback (AccessibleObject.class, "gObjectClass_finalize", 1); //$NON-NLS-1$
		GTypeInfo_base_init_type = newCallback (AccessibleFactory.class, "gTypeInfo_base_init_type", 1); //$NON-NLS-1$
                //#endif
		/* Action interface */
                /*#if USWT
		InitActionIfaceCB = newCallback (dispatcher, INITACTIONIFACECB, 1);
                  #else*/
		InitActionIfaceCB = newCallback (AccessibleFactory.class, "initActionIfaceCB", 1); //$NON-NLS-1$
                //#endif
		GInterfaceInfo interfaceInfo = new GInterfaceInfo ();
		interfaceInfo.interface_init = InitActionIfaceCB.getAddress ();
		ActionIfaceDefinition = OS.g_malloc (GInterfaceInfo.sizeof);  
		OS.memmove (ActionIfaceDefinition, interfaceInfo, GInterfaceInfo.sizeof);
		/* Component interface */
                /*#if USWT
		InitComponentIfaceCB = newCallback (dispatcher, INITCOMPONENTIFACECB, 1);
                  #else*/
		InitComponentIfaceCB = newCallback (AccessibleFactory.class, "initComponentIfaceCB", 1); //$NON-NLS-1$
                //#endif
		interfaceInfo = new GInterfaceInfo ();
		interfaceInfo.interface_init = InitComponentIfaceCB.getAddress ();
		ComponentIfaceDefinition = OS.g_malloc (GInterfaceInfo.sizeof);
		OS.memmove (ComponentIfaceDefinition, interfaceInfo, GInterfaceInfo.sizeof);
		/* Hypertext interface */
                /*#if USWT
		InitHypertextIfaceCB = newCallback (dispatcher, INITHYPERTEXTIFACECB, 1);
                  #else*/
		InitHypertextIfaceCB = newCallback (AccessibleFactory.class, "initHypertextIfaceCB", 1); //$NON-NLS-1$
                //#endif
		interfaceInfo = new GInterfaceInfo ();
		interfaceInfo.interface_init = InitHypertextIfaceCB.getAddress ();
		HypertextIfaceDefinition = OS.g_malloc (GInterfaceInfo.sizeof);  
		OS.memmove (HypertextIfaceDefinition, interfaceInfo, GInterfaceInfo.sizeof);
		/* Selection interface */
                /*#if USWT
		InitSelectionIfaceCB = newCallback (dispatcher, INITSELECTIONIFACECB, 1);
                  #else*/
		InitSelectionIfaceCB = newCallback (AccessibleFactory.class, "initSelectionIfaceCB", 1); //$NON-NLS-1$
                //#endif
		interfaceInfo = new GInterfaceInfo ();
		interfaceInfo.interface_init = InitSelectionIfaceCB.getAddress ();
		SelectionIfaceDefinition = OS.g_malloc (GInterfaceInfo.sizeof);  
		OS.memmove (SelectionIfaceDefinition, interfaceInfo, GInterfaceInfo.sizeof);
		/* Text interface */
                /*#if USWT
		InitTextIfaceCB = newCallback (dispatcher, INITTEXTIFACECB, 1);
                  #else*/
		InitTextIfaceCB = newCallback (AccessibleFactory.class, "initTextIfaceCB", 1); //$NON-NLS-1$
                //#endif
		interfaceInfo = new GInterfaceInfo ();
		interfaceInfo.interface_init = InitTextIfaceCB.getAddress ();
		TextIfaceDefinition = OS.g_malloc (GInterfaceInfo.sizeof);  
		OS.memmove (TextIfaceDefinition, interfaceInfo, GInterfaceInfo.sizeof);
	}

  /*#if USWT
	static private CNICallback newCallback (CNIDispatcher dispatcher, int method, int argCount) {
		CNICallback callback = new CNICallback (dispatcher, method, argCount);
		if (callback.getAddress () == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
		return callback;
	}
    #else*/
	static private Callback newCallback (Object object, String method, int argCount) {
		Callback callback = new Callback (object, method, argCount);
		if (callback.getAddress () == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
		return callback;
	}
  //#endif

	private AccessibleFactory (int /*long*/ widgetType) {
		super ();
		widgetTypeName = OS.g_type_name (widgetType);
		int widgetTypeNameLength = OS.strlen (widgetTypeName) + 1;
		byte[] buffer = new byte [widgetTypeNameLength];
		OS.memmove (buffer, widgetTypeName, widgetTypeNameLength);
		byte[] factoryName = new byte [FACTORY_TYPENAME.length + widgetTypeNameLength - 1];
		System.arraycopy (FACTORY_TYPENAME, 0, factoryName, 0, FACTORY_TYPENAME.length);
		System.arraycopy (buffer, 0, factoryName, FACTORY_TYPENAME.length - 1, widgetTypeNameLength);
		if (OS.g_type_from_name (factoryName) == 0) {
			/* register the factory */
			int /*long*/ registry = ATK.atk_get_default_registry ();
			int /*long*/ previousFactory = ATK.atk_registry_get_factory (registry, widgetType);
			objectParentType = ATK.atk_object_factory_get_accessible_type (previousFactory);
			if (objectParentType == 0) objectParentType = DefaultParentType;
			int /*long*/ factoryParentType = OS.g_type_from_name (FACTORY_PARENTTYPENAME);
                        /*#if USWT
			gTypeInfo_base_init_factory  = new CNICallback (dispatcher, GTYPEINFO_BASE_INIT_FACTORY, 1);
                          #else*/
			gTypeInfo_base_init_factory  = new Callback (this, "gTypeInfo_base_init_factory", 1); //$NON-NLS-1$
                        //#endif
			int /*long*/ address = gTypeInfo_base_init_factory.getAddress ();
			if (address == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
			GTypeInfo typeInfo = new GTypeInfo ();
			typeInfo.base_init = address;
			typeInfo.class_size = (short)ATK.AtkObjectFactoryClass_sizeof ();
			typeInfo.instance_size = (short)ATK.AtkObjectFactory_sizeof ();
			int /*long*/ info = OS.g_malloc (GTypeInfo.sizeof); 
			OS.memmove (info, typeInfo, GTypeInfo.sizeof); 
			int /*long*/ swtFactoryType = OS.g_type_register_static (factoryParentType, factoryName, info, 0);
			ATK.atk_registry_set_factory_type (registry, widgetType, swtFactoryType);
			handle = ATK.atk_registry_get_factory (registry, widgetType);
		}
	}

	void addAccessible (Accessible accessible) {
		int /*long*/ controlHandle = accessible.getControlHandle ();
		accessibles.put (new LONG (controlHandle), accessible);
		ATK.atk_object_factory_create_accessible (handle, controlHandle);
	}

	int /*long*/ atkObjectFactory_create_accessible (int /*long*/ widget) {
		Accessible accessible = (Accessible) accessibles.get (new LONG (widget));
		if (accessible == null) {
			/*
			* we don't care about this control, so create it with the parent's
			* type so that its accessibility callbacks will not pass though here 
			*/  
			int /*long*/ result = OS.g_object_new (objectParentType, 0);
			ATK.atk_object_initialize (result, widget);
			return result;
		}
		/* if an atk object has already been created for this widget then just return it */
		if (accessible.accessibleObject != null) {
			return accessible.accessibleObject.handle;
		}
		int typeNameLength = OS.strlen (widgetTypeName);
		byte[] buffer = new byte [typeNameLength];
		OS.memmove (buffer, widgetTypeName, typeNameLength);
		int /*long*/ type = getType (buffer, accessible, objectParentType, ACC.CHILDID_SELF);
		AccessibleObject object = new AccessibleObject (type, widget, accessible, objectParentType, false);
		accessible.accessibleObject = object;
		return object.handle;
	}
	
	static int /*long*/ getChildType (Accessible accessible, int childIndex) {
		return getType (CHILD_TYPENAME, accessible, DefaultParentType, childIndex);
	}

	static int /*long*/ getDefaultParentType () {
		return DefaultParentType;
	}

	static int /*long*/ getType (byte[] widgetTypeName, Accessible accessible, int /*long*/ parentType, int childId) {
		AccessibleControlEvent event = new AccessibleControlEvent (accessible);
		event.childID = childId;
		AccessibleControlListener[] listeners = accessible.getControlListeners ();
		for (int i = 0; i < listeners.length; i++) {
			listeners [i].getRole (event);
		}
		boolean action = false, hypertext = false, selection = false, text = false;
		if (event.detail != 0) {	/* a role was specified */
			for (int i = 0; i < actionRoles.length; i++) {
				if (event.detail == actionRoles [i]) {
					action = true;
					break;
				}
			}
			for (int i = 0; i < hypertextRoles.length; i++) {
				if (event.detail == hypertextRoles [i]) {
					hypertext = true;
					break;
				}
			}
			for (int i = 0; i < selectionRoles.length; i++) {
				if (event.detail == selectionRoles [i]) {
					selection = true;
					break;
				}
			}
			for (int i = 0; i < textRoles.length; i++) {
				if (event.detail == textRoles [i]) {
					text = true;
					break;
				}
			}
		} else {
			action = hypertext = selection = text = true;
		}
		String swtTypeName = new String (SWT_TYPE_PREFIX);
		swtTypeName += new String (widgetTypeName);
		if (action) swtTypeName += "Action"; //$NON-NLS-1$
		if (hypertext) swtTypeName += "Hypertext"; //$NON-NLS-1$
		if (selection) swtTypeName += "Selection"; //$NON-NLS-1$
		if (text) swtTypeName += "Text"; //$NON-NLS-1$

		int /*long*/ type = 0;
		LONG typeInt = (LONG)Types.get (swtTypeName);
		if (typeInt != null) {
			type = typeInt.value;
		} else {
			/* define the type */
			int /*long*/ queryPtr = OS.g_malloc (GTypeQuery.sizeof);
			OS.g_type_query (parentType, queryPtr);
			GTypeQuery query = new GTypeQuery ();
			OS.memmove (query, queryPtr, GTypeQuery.sizeof);
			OS.g_free (queryPtr);
			GTypeInfo typeInfo = new GTypeInfo ();
			typeInfo.base_init = GTypeInfo_base_init_type.getAddress ();
			typeInfo.class_size = (short) query.class_size;
			typeInfo.instance_size = (short) query.instance_size;
			ObjectIfaceDefinition = OS.g_malloc (GTypeInfo.sizeof); 
			OS.memmove (ObjectIfaceDefinition, typeInfo, GTypeInfo.sizeof);
			byte[] nameBytes = new byte [swtTypeName.length () + 1];
			System.arraycopy(swtTypeName.getBytes (), 0, nameBytes, 0, swtTypeName.length ()); 
			type = OS.g_type_register_static (parentType, nameBytes, ObjectIfaceDefinition, 0);
			OS.g_type_add_interface_static (type, AccessibleObject.ATK_COMPONENT_TYPE, ComponentIfaceDefinition);
			if (action) OS.g_type_add_interface_static (type, AccessibleObject.ATK_ACTION_TYPE, ActionIfaceDefinition);
			if (hypertext) OS.g_type_add_interface_static (type, AccessibleObject.ATK_HYPERTEXT_TYPE, HypertextIfaceDefinition);
			if (selection) OS.g_type_add_interface_static (type, AccessibleObject.ATK_SELECTION_TYPE, SelectionIfaceDefinition);
			if (text) OS.g_type_add_interface_static (type, AccessibleObject.ATK_TEXT_TYPE, TextIfaceDefinition);
			Types.put (swtTypeName, new LONG (type));
		}
		return type;
	}

	int /*long*/ gTypeInfo_base_init_factory (int /*long*/ klass) {
		int /*long*/ atkObjectFactoryClass = ATK.ATK_OBJECT_FACTORY_CLASS (klass);
		AtkObjectFactoryClass objectFactoryClassStruct = new AtkObjectFactoryClass ();
		ATK.memmove (objectFactoryClassStruct, atkObjectFactoryClass);
                /*#if USWT
		atkObjectFactoryCB_create_accessible = new CNICallback(dispatcher, ATKOBJECTFACTORY_CREATE_ACCESSIBLE, 1);
                  #else*/
		atkObjectFactoryCB_create_accessible = new Callback (this, "atkObjectFactory_create_accessible", 1); //$NON-NLS-1$
                //#endif
		int /*long*/ address = atkObjectFactoryCB_create_accessible.getAddress ();
		if (address == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
		objectFactoryClassStruct.create_accessible = address;
		ATK.memmove (atkObjectFactoryClass, objectFactoryClassStruct); 
		return 0;
	}
	
	static int /*long*/ gTypeInfo_base_init_type (int /*long*/ klass) {
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, klass);
		objectClass.get_name = AtkObjectCB_get_name.getAddress ();
		objectClass.get_description = AtkObjectCB_get_description.getAddress ();
		objectClass.get_n_children = AtkObjectCB_get_n_children.getAddress ();
		objectClass.get_role = AtkObjectCB_get_role.getAddress ();
		objectClass.get_parent = AtkObjectCB_get_parent.getAddress ();
		objectClass.ref_state_set = AtkObjectCB_ref_state_set.getAddress ();
		objectClass.get_index_in_parent = AtkObjectCB_get_index_in_parent.getAddress ();
		objectClass.ref_child = AtkObjectCB_ref_child.getAddress ();
		int /*long*/ gObjectClass = OS.G_OBJECT_CLASS (klass);
		GObjectClass objectClassStruct = new GObjectClass ();
		OS.memmove (objectClassStruct, gObjectClass);
		objectClassStruct.finalize = GObjectClass_finalize.getAddress ();
		OS.memmove (gObjectClass, objectClassStruct); 
		ATK.memmove (klass, objectClass);
		return 0;
	}
	
	static int /*long*/ initActionIfaceCB (int /*long*/ iface) {
		AtkActionIface actionIface = new AtkActionIface ();
		ATK.memmove (actionIface, iface);
		actionIface.get_keybinding = AtkActionCB_get_keybinding.getAddress (); 
		actionIface.get_name = AtkActionCB_get_name.getAddress ();
		ATK.memmove (iface, actionIface);
		return 0;
	}
	
	static int /*long*/ initComponentIfaceCB (int /*long*/ iface) {
		AtkComponentIface componentIface = new AtkComponentIface ();
		ATK.memmove (componentIface, iface);
		componentIface.get_extents = AtkComponentCB_get_extents.getAddress ();
		componentIface.get_position = AtkComponentCB_get_position.getAddress ();
		componentIface.get_size = AtkComponentCB_get_size.getAddress ();
		componentIface.ref_accessible_at_point = AtkComponentCB_ref_accessible_at_point.getAddress ();
		ATK.memmove (iface, componentIface);
		return 0;
	}

	static int /*long*/ initHypertextIfaceCB (int /*long*/ iface) {
		AtkHypertextIface hypertextIface = new AtkHypertextIface ();
		ATK.memmove (hypertextIface, iface);
		hypertextIface.get_link = AtkHypertextCB_get_link.getAddress (); 
		hypertextIface.get_link_index = AtkHypertextCB_get_link_index.getAddress ();
		hypertextIface.get_n_links = AtkHypertextCB_get_n_links.getAddress ();
		ATK.memmove (iface, hypertextIface);
		return 0;
	}

	static int /*long*/ initSelectionIfaceCB (int /*long*/ iface) {
		AtkSelectionIface selectionIface = new AtkSelectionIface ();
		ATK.memmove (selectionIface, iface);
		selectionIface.is_child_selected = AtkSelectionCB_is_child_selected.getAddress ();
		selectionIface.ref_selection = AtkSelectionCB_ref_selection.getAddress ();
		ATK.memmove (iface, selectionIface);
		return 0;
	}

	static int /*long*/ initTextIfaceCB (int /*long*/ iface) {
		AtkTextIface textInterface = new AtkTextIface ();
		ATK.memmove (textInterface, iface);
		textInterface.get_caret_offset = AtkTextCB_get_caret_offset.getAddress ();
		textInterface.get_character_at_offset = AtkTextCB_get_character_at_offset.getAddress ();
		textInterface.get_character_count = AtkTextCB_get_character_count.getAddress ();
		textInterface.get_n_selections = AtkTextCB_get_n_selections.getAddress ();
		textInterface.get_selection = AtkTextCB_get_selection.getAddress ();
		textInterface.get_text = AtkTextCB_get_text.getAddress ();
		textInterface.get_text_after_offset = AtkTextCB_get_text_after_offset.getAddress ();
		textInterface.get_text_at_offset = AtkTextCB_get_text_at_offset.getAddress ();
		textInterface.get_text_before_offset = AtkTextCB_get_text_before_offset.getAddress ();
		ATK.memmove (iface, textInterface);
		return 0;
	}

	static void registerAccessible (Accessible accessible) {
		/* If DefaultParentType is 0 then OS accessibility is not active */
		if (DefaultParentType == 0) return;
		int /*long*/ controlHandle = accessible.getControlHandle ();
		int /*long*/ widgetType = OS.G_OBJECT_TYPE (controlHandle);
		AccessibleFactory factory = (AccessibleFactory) Factories.get (new LONG (widgetType));
		if (factory == null) {
			factory = new AccessibleFactory (widgetType);
			Factories.put (new LONG (widgetType), factory);
		}
		factory.addAccessible (accessible);
	}
	
	void removeAccessible (Accessible accessible) {
		accessibles.remove (new LONG (accessible.getControlHandle ()));
	}
	
	static void unregisterAccessible (Accessible accessible) {
		int /*long*/ controlHandle = accessible.getControlHandle ();
		int /*long*/ widgetType = OS.G_OBJECT_TYPE (controlHandle);
		AccessibleFactory factory = (AccessibleFactory) Factories.get (new LONG (widgetType));
		if (factory != null) {
			factory.removeAccessible (accessible);
		}
	}

  /*#if USWT
  private class Dispatcher implements CNIDispatcher {
    public int /*long#eoc dispatch(int method, int /*long#eoc [] args) {
      switch (method) {
      case GTYPEINFO_BASE_INIT_FACTORY:
        return gTypeInfo_base_init_factory(args[0]);
        
      case ATKOBJECTFACTORY_CREATE_ACCESSIBLE:
        return atkObjectFactory_create_accessible(args[0]);
        
      default: throw new IllegalArgumentException();
      }
    }
  }
    #endif*/

}
