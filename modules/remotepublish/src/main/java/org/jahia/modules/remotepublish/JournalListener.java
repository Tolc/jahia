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

package org.jahia.modules.remotepublish;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * User: toto
 * Date: Apr 28, 2010
 * Time: 7:02:08 PM
 */
public class JournalListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger(JournalListener.class);

    private JCRSessionFactory sessionFactory;

    private RemotePublicationService remotePublication;

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            long t = System.currentTimeMillis();
            JCRSessionWrapper session = sessionFactory.getCurrentUserSession(workspace);

            ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 256);
            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out));
            Date date = null;
            Set<String> addedPath = new HashSet<String>();
            Map<Integer, List<Event>> events = new HashMap<Integer, List<Event>>();
            // Reorder events by deep of path and merging add node with add property
            while (eventIterator.hasNext()) {
                Event event = (Event) eventIterator.next();
                if (date == null) {
                    date = new Date(event.getDate());
                }
                String[] levels = event.getPath().split("/");
                int level = levels.length;
                if (isPropertyEvent(event)) {
                    level = level - 1;
                }
                List<Event> eventSet;
                if (events.get(level) == null) {
                    eventSet = new LinkedList<Event>();
                    events.put(level, eventSet);
                } else {
                    eventSet = events.get(level);
                }
                eventSet.add(event);
            }
            logger.debug("Write log ");
            ArrayList<Integer> list = new ArrayList<Integer>(events.keySet());
            Collections.sort(list);
            boolean eventsWritten = false;
            for (Integer level : list) {
                List<Event> eventList = events.get(level);
                Collections.sort(eventList, new EventComparator());
                Set<String> movedPaths = new LinkedHashSet<String>();
                for (Event event : eventList) {
                    final int type = event.getType();
                    if(type == Event.NODE_MOVED) {
                        movedPaths.add(event.getPath());
                    }
                    if((type != Event.NODE_ADDED && type != Event.NODE_REMOVED) || !movedPaths.contains(event.getPath())) {
                        boolean eventWritten = remotePublication.addEntry(event, oos, session, addedPath);
                        if (!eventsWritten && eventWritten) {
                            eventsWritten = true;
                        }
                    }
                }
            }
            oos.close();
            out.close();
            if (eventsWritten) {
                remotePublication.saveEventsBatch(new Journal(date, out.toByteArray()));                
            }
            logger.debug("Log closed, " + (System.currentTimeMillis() - t) + " ms");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private boolean isPropertyEvent(Event event) {
        return event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_REMOVED;
    }



    public void setRemotePublication(RemotePublicationService remotePublication) {
        this.remotePublication = remotePublication;
    }

    private class EventComparator implements Comparator<Event> {
        public int compare(Event event1, Event event2) {
            if (event1.getType() == event2.getType()) {
                if (isPropertyEvent(event1)) {
                    try {
                        String propertyName1 = StringUtils.substringAfterLast(event1.getPath(), "/");
                        String propertyName2 = StringUtils.substringAfterLast(event2.getPath(), "/");
                        if (propertyName1.equals(Constants.JCR_MIXINTYPES)) {
                            return -1;
                        } else if (propertyName2.equals(Constants.JCR_MIXINTYPES)) {
                            return 1;
                        } else if (propertyName1.equals(Constants.JCR_LANGUAGE)) {
                            return -1;
                        } else if (propertyName2.equals(Constants.JCR_LANGUAGE)) {
                            return 1;
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                if(event1.getType()==Event.NODE_MOVED) {
                    return -1;
                } else if(event2.getType()==Event.NODE_MOVED) {
                    return 1;
                } else if(event1.getType()==Event.NODE_REMOVED) {
                    return -1;
                } else if(event2.getType()==Event.NODE_REMOVED) {
                    return 1;
                } else if(event1.getType()==Event.NODE_ADDED) {
                    return -1;
                } else if(event2.getType()==Event.NODE_ADDED) {
                    return 1;
                }
            }
            return ((Integer)event1.getType()).compareTo(event2.getType());
        }
    }
}
