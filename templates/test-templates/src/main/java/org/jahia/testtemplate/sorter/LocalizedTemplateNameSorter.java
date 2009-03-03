/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.testtemplate.sorter;

import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplateDef;
import org.jahia.utils.i18n.JahiaResourceBundle;

public class LocalizedTemplateNameSorter implements Comparator<JahiaTemplateDef> {
    private ResourceBundle res;

    public LocalizedTemplateNameSorter(ResourceBundle res) {
        super();
        this.res = res;
    }

    public int compare(JahiaTemplateDef o1, JahiaTemplateDef o2) {
        String resValue1 = res.getString(o1.getDisplayName());
        if (resValue1 == null) {
            resValue1 = o1.getDisplayName();
        }
        String resValue2 = res.getString(o2.getDisplayName());
        if (resValue2 == null) {
            resValue2 = o2.getDisplayName();
        }        
        return resValue1.compareTo(resValue2);
    }
    

}
