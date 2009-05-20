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
package org.jahia.views.engines;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.settings.SettingsBean;


/**
 *
 * <p>Title: Bean containing common datas for Jahia Engine interface</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public interface JahiaEngineCommonDataInterface {

    public abstract JahiaData getJahiaData();

    public abstract ProcessingContext getParamBean();

    public abstract SettingsBean getSettings();

    public abstract String getEngineTitle();

    public abstract void setEngineTitle(String title);

    public abstract String getEngineURL();

    public abstract void setEngineURL(String engineURL);

    public abstract String getEnginesJspContextPath();

    public abstract String getImagesContextPath();

    public abstract String getJavaScriptPath();

    public abstract String getHttpJsContextPath();

}

