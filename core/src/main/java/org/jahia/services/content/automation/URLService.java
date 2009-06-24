/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.automation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jahia.data.search.JahiaSearchHit;
import org.jahia.settings.SettingsBean;

public class URLService {
    private static URLService instance;
    private String encoding;

    private URLService(String encoding) {
        super();
        this.encoding = encoding;
    }

    public static URLService getInstance() {
        if (instance == null) {
            instance = new URLService(SettingsBean.getInstance()
                    .getDefaultURIEncoding());
        }
        return instance;
    }

    public String addURLPath(JahiaSearchHit searchHit, String urlPath) {
        String url = searchHit.getURL() + urlPath;
        searchHit.setURL(url);
        return url;
    }

    public String addURLQueryParameter(JahiaSearchHit searchHit, String parameterName,
            String parameterValue) {
        StringBuilder changedUrl = new StringBuilder(searchHit.getURL());
        appendParams(changedUrl, parameterName + "=" + encode(parameterValue));
        String url = changedUrl.toString();
        searchHit.setURL(url);        
        return url;
    }

    private String encode(String parameterValue) {
        String value;
        try {
            value = URLEncoder.encode(parameterValue, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return value;
    }

    protected void appendParams(final StringBuilder theUrl, String params) {

        if (params != null && (params.length() > 0)) {
            if (theUrl.toString().indexOf("?") == -1) {
                if (params.startsWith("&")) {
                    params = "?" + params.substring(1, params.length());
                } else if (!params.startsWith("?")) {
                    if (!params.startsWith("/")) {
                        params = "?" + params;
                    }
                }
            } else {
                if (!params.startsWith("&")) {
                    if (!params.startsWith("/")) {
                        params = "&" + params;
                    }
                }
            }
            theUrl.append(params);
        }

    }
}
