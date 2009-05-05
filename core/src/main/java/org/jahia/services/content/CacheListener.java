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
package org.jahia.services.content;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;

/**
 * Listener for flushing container HTML cache entries.
 * User: toto
 * Date: 25 févr. 2008
 * Time: 14:36:14
 */
public class CacheListener extends DefaultEventListener {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(CacheListener.class);

    public static Cache<String, ?> cache;

    public CacheListener() {
        try {
            cache = CacheFactory.getInstance().createCacheInstance("WebdavCache");
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
        }
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(EventIterator eventIterator) {
        Set<String> nodes = new HashSet<String>();
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            try {
                if (isExternal(event)) {
                    continue;
                }

                String path = event.getPath();
                String parentPath = path.substring(0,path.lastIndexOf('/'));
                String name = path.substring(path.lastIndexOf('/')+1);
                String parentName = parentPath.substring(parentPath.lastIndexOf('/')+1);
                if ((event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED) && name.equals("j:acl")) {
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) && parentName.equals("j:acl")) {
                    parentPath = parentPath.substring(0,parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if (event.getType() == Event.PROPERTY_CHANGED && name.equals("j:fullpath")) {
                    // invalidate container HTML cache when the file is moved/renamed 
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.NODE_REMOVED) && name.indexOf(':')==-1) {
                    nodes.add(path);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!nodes.isEmpty()) {
            JCRSessionWrapper session = null;
            try {
                session = JCRStoreService.getInstance().getSystemSession();

                for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                    String s = iterator.next();
                    JCRNodeWrapper n = (JCRNodeWrapper) session.getItem(provider.decodeInternalName(s));
                    flushNodeRefs(n);
                    cache.remove(n.getPath());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
        }
//        System.out.println("----------------> "+nodes);
    }

    private void flushNodeRefs(JCRNodeWrapper n) throws RepositoryException, JahiaException {
        if (n.isValid()) {
            if (n.isFile()) {
                for (UsageEntry usageEntry : n.findUsages(false)) {
                    int id = usageEntry.getId();
                    ContentField field = ContentField.getField(id);
                    ContentContainerKey key = new ContentContainerKey(field.getContainerID());
                    ContentContainerListKey listkey = (ContentContainerListKey) key.getParent(null);
                    ContainerHTMLCache<?, ?> containerHTMLCache = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
                    if (usageEntry.getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.NORMAL, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        if (listkey != null) {
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.NORMAL, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        }
                        if (!field.hasStagingEntries()) {
                            containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            if (listkey != null) {
                                containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                                containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            }
                        }
                    } else if (usageEntry.getWorkflow() == EntryLoadRequest.STAGING_WORKFLOW_STATE ||
                            usageEntry.getWorkflow() == EntryLoadRequest.WAITING_WORKFLOW_STATE) {
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        if (listkey != null) {
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        }
                    }
                }
            } else {
                List<JCRNodeWrapper> children = n.getChildren();
                for (JCRNodeWrapper child : children) {
                    flushNodeRefs(child);
                }
            }
        }
    }
}
