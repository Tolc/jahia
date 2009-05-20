/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.definition;

import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.data.definition.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.*;
import com.allen_sauer.gwt.log.client.Log;

import java.util.*;


/**
 * This is a property editor that allows to edit properties of a JCR node.
 */
public class PropertiesEditor extends FormPanel {

    private List<GWTJahiaNodeType> nodeTypes = null;
    private Map<String, GWTJahiaNodeProperty> currentProperties = null;
    private Map<String, GWTJahiaNodeProperty> originalProperties = null;
    private Map<String, Field> fields;
    private ComboBox<GWTJahiaNodeType> combo;
    private GWTJahiaNodeType nodeType;
    private boolean isMultipleEdit = false;
    private boolean viewInheritedItems = false;
    private List<String> excludedItems;
    private List<String> excludedTypes;

    public PropertiesEditor(List<GWTJahiaNodeType> types, Map<String, GWTJahiaNodeProperty> properties, boolean isMultipleEdit, boolean viewInheritedItems, List<String> excludedItems, List<String> excludedTypes) {
        super();
        nodeTypes = types;
        this.isMultipleEdit = isMultipleEdit;
        originalProperties = properties;
        cloneProperties();
        this.viewInheritedItems = viewInheritedItems;
        this.excludedItems = excludedItems;
        this.excludedTypes = excludedTypes;
        renderNewFormPanel();
    }

    public PropertiesEditor(List<GWTJahiaNodeType> types, boolean isMultipleEdit, boolean viewInheritedItems) {
        super();
        nodeTypes = types;
        if (nodeTypes == null) {
            nodeTypes = new ArrayList<GWTJahiaNodeType>();
        }
        this.isMultipleEdit = isMultipleEdit;
        cloneProperties();
        isMultipleEdit = true;
        this.viewInheritedItems = viewInheritedItems;
        renderNewFormPanel();
    }

    public void renderNewFormPanel() {
        setFieldWidth(300);
        setLabelWidth(200);
        setScrollMode(Style.Scroll.AUTO);
        setBorders(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        setFrame(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);

        final ListStore<GWTJahiaNodeType> listStore = new ListStore<GWTJahiaNodeType>();
        listStore.add(nodeTypes);
        combo = new ComboBox<GWTJahiaNodeType>();
        combo.setEmptyText("Select a state...");
        combo.setDisplayField("label");
        combo.setStore(listStore);
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setWidth(200);
        combo.addStyleName("gxt-fix-topcomponent-position"); // fix static position given by setTopComponent

        combo.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<GWTJahiaNodeType>>() {
            public void handleEvent(SelectionChangedEvent<GWTJahiaNodeType> event) {
                Log.debug("event !");

                GWTJahiaNodeType selectedNodeType = event.getSelectedItem();
                Log.debug("changed ? ");
                if (nodeType != selectedNodeType) {
                    nodeType = selectedNodeType;
                    Log.debug("changed");
                    renderForm();
                    layout();
//                    m_callback.rerenderProperties();
                    Log.debug("end");
                }
                Log.debug("out");

            }
        });


        List<GWTJahiaNodeType> s = new ArrayList<GWTJahiaNodeType>();
        if (nodeTypes.size() > 0) {
            nodeType = nodeTypes.get(0);
            s.add(nodeType);
        }
        combo.setSelection(s);

        ToolBar toolBar = new ToolBar();
        toolBar.setVisible(false);

        if (nodeTypes.size() > 1) {
            toolBar.add(new AdapterToolItem(combo));
            toolBar.setVisible(true);
        }

        setTopComponent(toolBar);
        renderForm();
    }

    private void renderForm() {
        if (combo.getSelection().size() > 0) {
            removeAll();
            fields = new HashMap<String, Field>();
            if (viewInheritedItems) {
                addItems(nodeType.getInheritedItems());
            }
            addItems(nodeType.getItems());
        }
    }

    private void addItems(List<GWTJahiaItemDefinition> items) {
        for (GWTJahiaItemDefinition definition : items) {
            if ((excludedTypes != null && excludedTypes.contains(definition.getDeclaringNodeType())) ||
                    (excludedItems != null && excludedItems.contains(definition.getName()))) {
                continue;
            }
            if (!currentProperties.containsKey(definition.getName())) {
                GWTJahiaNodeProperty property = new GWTJahiaNodeProperty();
                property.setName(definition.getName());
                property.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                if (!definition.isNode()) {
                    property.setMultiple(((GWTJahiaPropertyDefinition) definition).isMultiple());
                }
                if (!definition.isNode() && ((GWTJahiaPropertyDefinition)definition).getDefaultValues() != null) {
                    property.setValues(((GWTJahiaPropertyDefinition)definition).getDefaultValues());
                }
                currentProperties.put(definition.getName(), property);
            }

            final Field field = FormFieldCreator.createField(definition, currentProperties.get(definition.getName()));
            if (field != null) {
                if (isMultipleEdit && !definition.isProtected()) {
                    field.setEnabled(false);
                    final CheckBox checkbox = new CheckBox();
                    checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent event) {
                            if (checkbox.getValue()) {
                                Log.debug("add ");
                                field.setEnabled(true);
                            } else {
                                Log.debug("remove ");
                                field.setEnabled(false);
                            }
                        }

                    });
                    checkbox.setHideLabel(true);
                    add(checkbox);
                }
                add(field);
                fields.put(field.getName(), field);
            }
        }
    }

    /**
     * Clone hashmap in order to have a modifiable one.
     */
    private void cloneProperties() {
        currentProperties = new HashMap<String, GWTJahiaNodeProperty>();
        if (originalProperties != null) {
            for (String aProp : originalProperties.keySet()) {
                currentProperties.put(aProp, originalProperties.get(aProp).cloneObject());
            }
        }
    }

    public List<GWTJahiaNodeProperty> getProperties() {
        List<GWTJahiaNodeProperty> newProps = new ArrayList<GWTJahiaNodeProperty>();

        for (GWTJahiaNodeType nodeType : nodeTypes) {
            List<GWTJahiaItemDefinition> allItems = new ArrayList<GWTJahiaItemDefinition>();
            if (viewInheritedItems) {
                allItems.addAll(nodeType.getInheritedItems());
            }
            allItems.addAll(nodeType.getItems());
            
            for (GWTJahiaItemDefinition definition : allItems) {

                if (!definition.isProtected()) {
                    Field f = fields.get(definition.getName());
                    GWTJahiaNodeProperty prop = currentProperties.get(definition.getName());
                    if (f != null && f.isDirty()) {
                        Log.debug("Set value for " + prop.getName());
                        if (!definition.isNode()) {
                            prop.setValue(new GWTJahiaNodePropertyValue(f.getValue().toString(), ((GWTJahiaPropertyDefinition) definition).getRequiredType()));
                        } else {
                            prop.setValue(new GWTJahiaNodePropertyValue(f.getValue().toString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
                        }
                    }
                    newProps.add(prop);
                }
            }
        }
        return newProps;
    }

    public void resetForm() {
        cloneProperties();
        renderForm();
        layout();
    }
}