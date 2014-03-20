/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.Field;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.GWTConstraintViolationException;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * Utility class used to collect validation result for engine decoration.
 *
 * @since Jahia 7.0
 */
public class EngineValidation {
    private NodeHolder engine;
    private TabPanel tabs;
    private String selectedLanguage;
    private Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties;

    public EngineValidation(NodeHolder engine, TabPanel tabs, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) {
        this.engine = engine;
        this.tabs = tabs;
        this.selectedLanguage = selectedLanguage;
        this.changedI18NProperties = changedI18NProperties;
    }

    public static class ValidateResult {
        public boolean canIgnore = true;
        public TabItem errorTab = null;
        public Field<?> errorField = null;
        public String errorLang = null;
        public String message = Messages.get("failure.invalid.constraint.label",
                "There are some validation errors!"
                        + " Click on the information icon next to the"
                        + " highlighted fields, correct the input and save again."
        );
    }

    public static interface ValidateCallback {
        void handleValidationResult(EngineValidation.ValidateResult result);

        void saveAnyway();

        void close();
    }


    /**
     * Generate {ValidateResult} based on the GWT fields validation.
     *
     * @return the {ValidateResult}.
     */
    public boolean validateData(final ValidateCallback callback) {
        List<ValidateResult> validateResult = new ArrayList<ValidateResult>();

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doValidate(validateResult, engine, tab, selectedLanguage, changedI18NProperties, tabs);
        }
        if (!validateResult.isEmpty()) {
            List<ValidateResult> r = new ArrayList<ValidateResult>();
            for (ValidateResult result : validateResult) {
                if (result.errorTab == tabs.getSelectedItem()) {
                    r.add(0,result);
                } else {
                    r.add(result);
                }
            }

            final Iterator<EngineValidation.ValidateResult> it = r.iterator();
            displayValidationError(it, callback);
            return false;
        } else {
            return true;
        }
    }


    private void displayValidationError(final Iterator<ValidateResult> it, final ValidateCallback callback) {
        final EngineValidation.ValidateResult result = it.next();
        callback.handleValidationResult(result);
        final Listener<MessageBoxEvent> boxCallback = new Listener<MessageBoxEvent>() {
            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (result.canIgnore && be.getButtonClicked().equals(be.getDialog().getButtonById(Dialog.YES))) {
                    //skip
                    if (it.hasNext()) {
                        displayValidationError(it, callback);
                    } else {
                        callback.saveAnyway();
                    }
                } else {
                    // close
                    callback.close();
                }
            }
        };
        if (result.canIgnore) {
            String continueMessage = Messages.get("label.continueAnyway", "Do you want to continue anyway ?");
            MessageBox.confirm(Messages.get("label.error", "Error"), result.message + "</br><b>" + continueMessage + "</b>", boxCallback);
        } else {
            MessageBox.alert(Messages.get("label.error", "Error"), result.message, boxCallback);
        }
    }

    /**
     * Generate {ValidateResult} based on the exception returned by the JCR session.
     *
     * @return the {ValidateResult}.
     */
    public ValidateResult getValidationFromException(List<GWTConstraintViolationException> errors) {
        Map<String, GWTConstraintViolationException> errorMap = new HashMap<String, GWTConstraintViolationException>();
        for (GWTConstraintViolationException error : errors) {
            if (error.getPropertyName() != null) {
                errorMap.put(error.getPropertyName(), error);
            }
        }

        ValidateResult validateResult = new ValidateResult();

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = pe.getFieldsMap();
                    for (String fieldName : fieldsMap.keySet()) {
                        if (errorMap.containsKey(fieldName)) {
                            Field<?> field = fieldsMap.get(fieldName).getField();
                            GWTConstraintViolationException error = errorMap.get(fieldName);
                            field.markInvalid(error.getConstraintMessage());

                            validateResult.errorTab = tab;
                            validateResult.errorField = field;
                            validateResult.errorLang = error.getLocale();
                            validateResult.canIgnore = false;
                        }
                    }
                }
            }
        }
        return validateResult;
    }

}
