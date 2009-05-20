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
package org.jahia.services.audit;

import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_ADDED_CONTAINER;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_ADDED_FIELD;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_ADDED_PAGE;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_CONTAINER_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_CONTAINER_DELETION_AND_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_CONTAINER_LIST_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_CONTAINER_LIST_DELETION_AND_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_DELETED_CONTAINER;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_DELETED_FIELD;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIELD_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIELD_DELETION_AND_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIRST_CONTAINER_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIRST_CONTAINER_LIST_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIRST_FIELD_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_FIRST_PAGE_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_PAGE_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_PAGE_DELETION_AND_ACTIVATION;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_SET_PROPERTY_FOR_CONTAINER_LIST;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_SET_PROPERTY_FOR_PAGE;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_SET_RIGHTS;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_UPDATED_CONTAINER;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_UPDATED_FIELD;
import static org.jahia.services.audit.LogsBasedQueryConstant.OPERATION_UPDATED_TEMPLATE;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.utils.JahiaObjectTool;

/**
 * Implementation of the event listener to populate create Jahia audit log
 * entries.
 */
public class LoggingEventListener extends JahiaEventListener {

    private static Logger logger = Logger.getLogger(LoggingEventListener.class);

    public static final int FIELD_TYPE = JahiaObjectTool.FIELD_TYPE;
    public static final int CONTAINER_TYPE = JahiaObjectTool.CONTAINER_TYPE;
    public static final int CONTAINERLIST_TYPE = JahiaObjectTool.CONTAINERLIST_TYPE;
    public static final int PAGE_TYPE = JahiaObjectTool.PAGE_TYPE;
    public static final int ACL_TYPE = JahiaObjectTool.ACL_TYPE;

    // references to needed services.
    private JahiaAuditLogManagerService mAuditLogManager;

    public JahiaAuditLogManagerService getMAuditLogManager() {
        return mAuditLogManager;
    }

    public void setMAuditLogManager(JahiaAuditLogManagerService mAuditLogManager) {
        this.mAuditLogManager = mAuditLogManager;
    }

    /**
     * constructor
     * get an instance of the Log Manager Service
     */
    public LoggingEventListener ()
            throws JahiaException {
        // Try to get the Audit Log Manager Service
    } // end constructor


    /***
     * triggered when Jahia adds a field
     *
     * @param        je                  the associated JahiaEvent
     *
     */
    public void fieldAdded (JahiaEvent je) {
        if (je != null && je.getObject () != null) {
            mAuditLogManager.logEvent (je, FIELD_TYPE, OPERATION_ADDED_FIELD);
        }
    }


    /***
     * triggered when Jahia updates a field
     *
     * @param        je                  the associated JahiaEvent
     *
     */
    public void fieldUpdated (JahiaEvent je) {
        mAuditLogManager.logEvent (je, FIELD_TYPE, OPERATION_UPDATED_FIELD);
    }


    /***
     * triggered when Jahia deletes a field
     *
     * @param        je                  the associated JahiaEvent
     *
     */
    public void fieldDeleted (JahiaEvent je) {
        mAuditLogManager.logEvent (je, FIELD_TYPE, OPERATION_DELETED_FIELD);
    }


    /***
     * triggered when Jahia adds a container
     *
     * @param        je                  the associated JahiaEvent
     *
     */
    public void containerAdded (JahiaEvent je) {
        mAuditLogManager.logEvent (je, CONTAINER_TYPE, OPERATION_ADDED_CONTAINER);
    }


    /***
     * triggered when Jahia updates a container
     * @param        je                  the associated JahiaEvent
     */
    public void containerUpdated (JahiaEvent je) {
        mAuditLogManager.logEvent (je, CONTAINER_TYPE, OPERATION_UPDATED_CONTAINER);
    }


    /***
     * triggered when Jahia deletes a container
     * @param        je                  the associated JahiaEvent
     */
    public void containerDeleted (JahiaEvent je) {
        mAuditLogManager.logEvent (je, CONTAINER_TYPE, OPERATION_DELETED_CONTAINER);
    }


    /***
     * triggered when Jahia adds a page
     * @param        je                  the associated JahiaEvent
     */
    public void pageAdded (JahiaEvent je) {
        mAuditLogManager.logEvent (je, PAGE_TYPE, OPERATION_ADDED_PAGE);
    }


    /***
     * triggered when Jahia sets properties on a page
     * @param        je                  the associated JahiaEvent
     */
    public void pagePropertiesSet (JahiaEvent je) {
        mAuditLogManager.logEvent (je, PAGE_TYPE, OPERATION_SET_PROPERTY_FOR_PAGE);
    }


    /***
     * triggered when template has been processed
     *
     * @param        je                  the associated JahiaEvent
     */
    public void templateUpdated (JahiaEvent je) {
        if (je.getObject () != null) {
            mAuditLogManager.logEvent (je, JahiaObjectTool.TEMPLATE_TYPE, OPERATION_UPDATED_TEMPLATE);
        }
    }


    /***
     * triggered when Jahia sets properties on a container list
     * @param        je                  the associated JahiaEvent
     */
    public void containerListPropertiesSet (JahiaEvent je) {
        mAuditLogManager.logEvent (je, CONTAINERLIST_TYPE, OPERATION_SET_PROPERTY_FOR_CONTAINER_LIST);
    }


    /***
     * triggered when Jahia sets ACL rights
     * @param        je                  the associated JahiaEvent
     */
    public void rightsSet (JahiaEvent je) {
        Object theObject = je.getObject ();
        if (theObject instanceof JahiaField) {
            mAuditLogManager.logEvent (je, FIELD_TYPE, OPERATION_SET_RIGHTS);
        } else if (theObject instanceof JahiaContainer) {
            mAuditLogManager.logEvent (je, CONTAINER_TYPE, OPERATION_SET_RIGHTS);
        } else if (theObject instanceof JahiaContainerList) {
            mAuditLogManager.logEvent (je, CONTAINERLIST_TYPE, OPERATION_SET_RIGHTS);
        } else if (theObject instanceof JahiaPage) {
            mAuditLogManager.logEvent (je, PAGE_TYPE, OPERATION_SET_RIGHTS);
        }
    }

    public void contentActivation(ContentActivationEvent theEvent) {
        if (theEvent.getActivationTestResults().getStatus() == ActivationTestResults.COMPLETED_OPERATION_STATUS) {
            ContentObject contentObject = theEvent.getContentObject();
            boolean isDeleted = false;
            try {
                isDeleted = contentObject.isDeleted(theEvent.getSaveVersion().getVersionID());
            } catch (JahiaException e) {
            }
            if (contentObject instanceof ContentField) {
                mAuditLogManager.logEvent(theEvent, FIELD_TYPE, isDeleted ? OPERATION_FIELD_DELETION_AND_ACTIVATION : OPERATION_FIELD_ACTIVATION);
            } else if (contentObject instanceof ContentContainer) {
                mAuditLogManager.logEvent(theEvent, CONTAINER_TYPE, isDeleted ? OPERATION_CONTAINER_DELETION_AND_ACTIVATION : OPERATION_CONTAINER_ACTIVATION);
            } else if (contentObject instanceof ContentContainerList) {
                mAuditLogManager.logEvent(theEvent, CONTAINERLIST_TYPE, isDeleted ? OPERATION_CONTAINER_LIST_DELETION_AND_ACTIVATION : OPERATION_CONTAINER_LIST_ACTIVATION);
            } else if (contentObject instanceof ContentPage) {
                mAuditLogManager.logEvent(theEvent, PAGE_TYPE, isDeleted ? OPERATION_PAGE_DELETION_AND_ACTIVATION : OPERATION_PAGE_ACTIVATION);
            }
            try{
                if ( !isDeleted && !contentObject.hasActiveEntries()
                        && !contentObject.hasArchiveEntryState(theEvent.getSaveVersion().getVersionID()) ){
                    if (contentObject instanceof ContentField) {
                        mAuditLogManager.logEvent(theEvent, FIELD_TYPE, OPERATION_FIRST_FIELD_ACTIVATION);
                    } else if (contentObject instanceof ContentContainer) {
                        mAuditLogManager.logEvent(theEvent, CONTAINER_TYPE, OPERATION_FIRST_CONTAINER_ACTIVATION);
                    } else if (contentObject instanceof ContentContainerList) {
                        mAuditLogManager.logEvent(theEvent, CONTAINERLIST_TYPE, OPERATION_FIRST_CONTAINER_LIST_ACTIVATION);
                    } else if (contentObject instanceof ContentPage) {
                        mAuditLogManager.logEvent(theEvent, PAGE_TYPE, OPERATION_FIRST_PAGE_ACTIVATION);
                    }
                }
            } catch ( Exception t ){
                logger.debug("Exception occurent logging first validation of content",t);
            }
        }
    }
}
