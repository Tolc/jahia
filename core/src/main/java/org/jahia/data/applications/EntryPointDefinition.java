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
 package org.jahia.data.applications;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import java.util.List;

/**
 * <p>Title: EntryPointDefinition for a web application</p>
 * <p>Description: A web application may contain multiple entry point
 * definitions, depending on the type of web application we are dealing with.
 * Servlet-based web application will for example have multiple servlet
 * mappings that will all be possible entry points into the application.
 * Portlet-based web applications will have multiple portlets in an application.
 * Note that the data contain in this object is generated by the
 * ApplicationManagerProvider, and it not meant to be stored in Jahia's
 * database, but provided by the implementation of each provider (in the case
 * of servlet-based web apps we parse the web.xml and for portlets we use
 * Jetspeed 2's registry sub-system).</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */
public interface EntryPointDefinition {

    public String getContext();

    public String getName ();

    public String getDisplayName();

    public String getDescription();

    public int getApplicationID();

    /**
     * Get the supported PortletMode for this entry point definition.
     * @return List a list of PortletMode objects
     */
    public List<PortletMode> getPortletModes();

    /**
     * Get the supported WindowState for this entry point definition.
     * @return List a list of WindowState objects.
     */
    public List<WindowState> getWindowStates();
}
