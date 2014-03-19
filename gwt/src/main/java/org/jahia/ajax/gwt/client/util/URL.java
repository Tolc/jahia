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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.ajax.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * URL related utility functions.
 * @author ktlili
 * Date: 5 nov. 2007
 * Time: 09:02:11
 */
public class URL {

    private static final String GWT_SCRIPT_PATH = "/gwt/";

    public static String getServerBaseURL() {
        String absoluteURLContext = GWT.getModuleBaseURL();
        String[] splittedUrl = absoluteURLContext.split("/");
        /*  url: http://www.example.com/jahia/...
        --->
        splittedUrl[0] = "http:"
        splittedUrl[1] = ""
        splittedUrl[2] = "www.example.com"  */
        if (splittedUrl.length > 2) {
            return splittedUrl[0] + "//" + splittedUrl[2];
        } else {
            return null;
        }
    }

    /**
     * Get Jahia context
     *
     * @return
     */
    public static String getJahiaContext() {
        String contextPath = "";
        String baseUrl = GWT.getModuleBaseURL();
        String serverBaseUrl = getServerBaseURL();
        baseUrl = baseUrl.substring(serverBaseUrl.length());
        int suffixPosition = baseUrl.indexOf(GWT_SCRIPT_PATH);
        if (suffixPosition != -1) {
            contextPath = baseUrl.substring(0, suffixPosition);
        }
        return contextPath;
    }

    /**
     * Get absolute url
     *
     * @param url
     * @return
     */
    public static String getAbsoluteURL(String url) {
        String server = getServerBaseURL();
        return server + url;
    }

    /**
     * Get relative url
     *
     * @return
     */
    public static String getRelativeURL() {
        return getWindowUrl().replaceAll(getJahiaContext(), "");
    }

    /**
     * Get queryString
     *
     * @return
     */
    public static String getQueryString() {
        String windowUrl = getWindowUrl();
        int separatorIndex = windowUrl.indexOf('?');
        if (separatorIndex > 0) {
            return windowUrl.substring(separatorIndex);
        }
        return null;
    }

    /**
     * Return as example /jahia/cms if the url is like http//www.mysite.com/jahia/cms/edit/....
     *
     * @return
     */
    public static String getContextServletPath() {
        return JahiaGWTParameters.getContextPath() + JahiaGWTParameters.getServletPath();
    }

    /**
     * Rewrite url (ie. used for richtext)
     *
     * @param url
     * @return
     */
    public static String rewrite(final String jahiaContextPath,final String jahiaServletPath, final String url) {
        if (url == null) {
            return null;
        } else if (url.indexOf("/{mode}/{lang}/") > 0) {
            // already rewited
            return url;
        } else {

            // absolute url are not processed
            if (isAbsoluteUrl(url)) {
                return url;
            }

            // return url like /jahia/cms/##mode##/##lang##"/content/sites/ACME/home.html
            return jahiaContextPath+ jahiaServletPath+ "/{mode}/{lang}" + url;
        }
    }

    /**
     * Chech if the url is absolute one
     *
     * @param url
     * @return
     */
    private static boolean isAbsoluteUrl(String url) {
        String[] splittedUrl = url.split("/");
        /*splittedUrl[0] = "http:"
        splittedUrl[1] = ""
        splittedUrl[2] = "www.example.com" */
        if (splittedUrl.length > 2) {
            if (splittedUrl[1].length() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get window urle
     *
     * @return
     */
    public static native String getWindowUrl() /*-{
    return $wnd.location.href;

}-*/;

    public static String replacePlaceholders(String value, final GWTJahiaNode selectedNode) {
        if (value.contains("$context")) {
            value = value.replace("$context", JahiaGWTParameters.getContextPath());
        }
        if (value.contains("$siteuuid")) {
            value = value.replace("$siteuuid", JahiaGWTParameters.getSiteUUID());
        }
        if (value.contains("$base-url")) {
            value = value.replace("$base-url", JahiaGWTParameters.getBaseUrl());
        }
        if (value.contains("$site-path")) {
            value = value.replace("$site-path", JahiaGWTParameters.getSiteNode().getPath());
        }
        if (value.contains("$site-homepage-path")) {
            String home = JahiaGWTParameters.getSiteNode().get(GWTJahiaNode.HOMEPAGE_PATH);
            if (home != null) {
                value = value.replace("$site-homepage-path", home);
            }
        }
        if (value.contains("$lang")) {
            value = value.replace("$lang", JahiaGWTParameters.getLanguage());
        }
        if (value.contains("$nodepathnoescape") && selectedNode != null) {
            value = value.replace("$nodepathnoescape", selectedNode.getPath());
        }
        if (value.contains("$nodepath") && selectedNode != null) {
            value = value.replace("$nodepath", com.google.gwt.http.client.URL.encodeQueryString(selectedNode.getPath()));
        }
        if (value.contains("$workspace")) {
            value = value.replace("$workspace", JahiaGWTParameters.getWorkspace());
        }
        if (value.contains("$location-path")) {
            value = value.replace("$location-path", com.google.gwt.http.client.URL.encodeQueryString(Window.Location.getPath()));
        }
        if (value.contains("$location-hash")) {
            value = value.replace("$location-hash", com.google.gwt.http.client.URL.encodeQueryString(Window.Location.getHash()));
        }
        return value;
    }
    
    /**
     * Appends a dummy parameter with a current timestamp to the provided URL. Used when rendering images to force server side reload.
     * 
     * @param url
     *            the URL of the image to be modified
     * @return an adjusted URL of the image with timestamp parameter added
     */
    public static String appendTimestamp(String url) {
        if (url == null || url.length() == 0) {
            return url;
        }

        return url + (url.contains("?") ? "&refresh=" : "?refresh=") + System.currentTimeMillis();
    }
}
