/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set contribution template for contentLists set as editable
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class UserAgentFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(UserAgentFilter.class);

    // Views may come from user-agent, query parameters or even form parameters.
    private Map<String, String> userAgentMatchingRules;
    private Map<Pattern, String> userAgentMatchingPatterns;

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        String userAgent = context.getRequest().getHeader("user-agent");

        if (userAgent != null && !resource.getTemplateType().contains("-")) {
            for (Map.Entry<Pattern,String> entry : userAgentMatchingPatterns.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    String baseType = resource.getTemplateType();
                    resource.setTemplateType(baseType+"-"+entry.getValue());
                    // todo : opimize a little bit ..
                    try {
                        service.resolveScript(resource, context);
                    } catch (TemplateNotFoundException e) {
                        logger.debug("Template not found for "+entry.getValue());
                        resource.setTemplateType(baseType);
                    }
                }
            }
        }
        return null;
    }


    public Map<String,String> getUserAgentMatchingRules() {
        return userAgentMatchingRules;
    }

    public void setUserAgentMatchingRules(Map<String, String> userAgentMatchingRules) {
        this.userAgentMatchingRules = userAgentMatchingRules;
        userAgentMatchingPatterns = new HashMap<Pattern, String>();
        for (Map.Entry<String,String> entry : userAgentMatchingRules.entrySet()) {
            Pattern curPattern = Pattern.compile(entry.getKey());
            userAgentMatchingPatterns.put(curPattern, entry.getValue());
        }
    }

}