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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.data.JahiaData;
import org.jahia.services.toolbar.resolver.SelectedResolver;
import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

/**
 * User: jahia
 * Date: 4 ao�t 2008
 * Time: 15:23:25
 */
public class JahiaPreferenceResolver implements SelectedResolver, VisibilityResolver {
    private static final transient Logger logger = Logger.getLogger(JahiaPreferenceResolver.class);

    private static transient final JahiaPreferencesService JAHIA_PREFERENCES_SERVICE = ServicesRegistry.getInstance().getJahiaPreferencesService();

    /**
     * Return true is preference with name ${type} == true
     *
     * @param jData
     * @param type
     * @return
     */
    public boolean isSelected(JahiaData jData, String type) {
        return getPreferenceValueAsBoolean(jData, type);
    }

    /**
     * Return true is preference with name ${name} == true
     *
     * @param jData
     * @param name
     * @return
     */
    private boolean getPreferenceValueAsBoolean(JahiaData jData, String name) {
        String prefValue = getGenericPreferenceValue(jData, name);
        boolean isSelected = false;
        try {
            isSelected = Boolean.parseBoolean(prefValue);
        } catch (Exception e) {
            logger.error("Preference [" + name + "] is not a boolean");
        }
        
        return isSelected;
    }

    /**
     * Return true is preference with name ${type} == true
     *
     * @param jData
     * @param type
     * @return
     */
    public boolean isVisible(JahiaData jData, String type) {
        return getPreferenceValueAsBoolean(jData, type);
    }

   /**
     * Get the generic preference provider
     *
     * @param jahiaData
     * @param name
     * @return
     */
    private String getGenericPreferenceValue(JahiaData jahiaData, String name) {
        return JAHIA_PREFERENCES_SERVICE.getGenericPreferenceValue(name, jahiaData.getProcessingContext());
    }
}
