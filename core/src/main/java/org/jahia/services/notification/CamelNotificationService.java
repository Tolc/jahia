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
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.notification;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.log4j.Logger;
import org.jahia.settings.SettingsBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 28 juin 2010
 */
public class CamelNotificationService {
    private transient static Logger logger = Logger.getLogger(CamelNotificationService.class);

    private CamelContext camelContext;

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void sendMessagesWithBodyAndHeaders(String target,Object body, Map<String, Object> headers) {
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.sendBodyAndHeaders(target,body, headers);
    }

    public void registerRoute(RoutesBuilder routesBuilder) throws Exception {
        camelContext.addRoutes(routesBuilder);
    }

    public void sendMail(String camelURI, String subject, String htmlBody, String textBody, String from, String toList,
                         String ccList, String bcclist) {
        ProducerTemplate template = camelContext.createProducerTemplate();
         Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("To", toList);
        if("".equals(from)) {
            headers.put("From", SettingsBean.getInstance().getMail_from());
        } else {
            headers.put("From", from);
        }
        if(!"".equals(ccList)) {
            headers.put("Cc",ccList);
        }
        if(!"".equals(bcclist)) {
            headers.put("Bcc",bcclist);
        }
        headers.put("Subject", subject);
        String body;
        if(!"".equals(htmlBody)) {
            headers.put("contentType","text/html");
            headers.put("alternativeBodyHeader",textBody);
            body = htmlBody;
        } else {
            headers.put("contentType","text/plain");
            body = textBody;
        }
        template.sendBodyAndHeaders(camelURI, body, headers);
    }
}
