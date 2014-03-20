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
package org.jahia.services.logging;

import org.slf4j.Logger;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCREventIterator;
import org.jahia.services.content.JCRObservationManager;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.List;

/**
 * JCR listener that logs repository events to the metrics logging service.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2009
 */
public class MetricsLoggingJCReventListener extends DefaultEventListener {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MetricsLoggingJCReventListener.class);
    private MetricsLoggingService loggingService;
    private List<String> nodeTypesList = null;


    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void setNodeTypesList(List<String> nodeTypes) {
        if (nodeTypes != null && nodeTypes.size() > 0) {
            this.nodeTypesList = nodeTypes;
        }
    }

    public int getEventTypes() {
        return loggingService.isEnabled() ? Event.NODE_ADDED + Event.NODE_MOVED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED : 0;
    }

    public String[] getNodeTypes() {
        return nodeTypesList == null ? null : nodeTypesList.toArray(new String[nodeTypesList.size()]);
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        if (events instanceof JCREventIterator && ((JCREventIterator)events).getOperationType() == JCRObservationManager.IMPORT) {
            return;
        }
        while (events.hasNext()) {
            try {
                Event event = events.nextEvent();
                switch (event.getType()) {
                    case Event.NODE_ADDED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "nodeCreated",
                                new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.NODE_MOVED:
                        /* From the JCR 2.0 Spec :
                           12.3.3 Event Information on Move and Order

                           On a NODE_MOVED event, the Map object returned by Event.getInfo() contains parameter
                           information from the method that caused the event. There are three JCR methods that cause
                           this event type: Session.move, Workspace.move and Node.orderBefore.

                           If the method that caused the NODE_MOVE event was a Session.move or Workspace.move then the
                           returned Map has keys srcAbsPath and destAbsPath with values corresponding to the parameters
                           passed to the move method, as specified in the Javadoc.

                           If the method that caused the NODE_MOVE event was a Node.orderBefore then the returned Map
                           has keys srcChildRelPath and destChildRelPath with values corresponding to the parameters
                           passed to the orderBefore method, as specified in the Javadoc.
                        */
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "nodeMoved",
                                new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.NODE_REMOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "nodeDeleted",
                                new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.PROPERTY_ADDED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "propertyAdded",
                                new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.PROPERTY_CHANGED:
                        // @todo we might want to add the new value if available, so that we can track updates more finely ?
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "propertyChanged",
                                new JSONObject(event.getInfo()).toString());
                        break;
                    case Event.PROPERTY_REMOVED:
                        loggingService.logContentEvent(event.getUserID(), "", "", event.getIdentifier(), event.getPath(), "", "propertyRemoved",
                                new JSONObject(event.getInfo()).toString());
                        break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }
}
