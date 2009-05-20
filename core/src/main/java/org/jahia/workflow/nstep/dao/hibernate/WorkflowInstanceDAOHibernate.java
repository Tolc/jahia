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
 package org.jahia.workflow.nstep.dao.hibernate;

import org.jahia.hibernate.dao.AbstractGeneratorDAO;
import org.jahia.workflow.nstep.model.Workflow;
import org.jahia.workflow.nstep.model.WorkflowInstance;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * This class interacts with Spring and Hibernate to save and
 * retrieve User objects.
 *
 * @author Matt Raible
 */
public class WorkflowInstanceDAOHibernate extends AbstractGeneratorDAO
        implements org.jahia.workflow.nstep.dao.WorkflowInstanceDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<WorkflowInstance> getWorkflowInstances() {
        return getHibernateTemplate().find("from WorkflowInstance");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowInstanceDAO ---------------------


    public void removeWorkflowInstance(Long workflowId) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getWorkflowInstanceById(workflowId));
    }

    public void removeWorkflowInstance(String objectKey) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.deleteAll(getHibernateTemplate().find("from WorkflowInstance where objectKey=?",objectKey));
    }
    

    public void saveWorkflowInstance(org.jahia.workflow.nstep.model.WorkflowInstance workflow) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (workflow.getId() == null) {
            workflow.setId(getNextLong(workflow));
        }
        hibernateTemplate.merge(workflow);
        if (logger.isDebugEnabled()) {
            logger.debug("userId set to: " + workflow.getId());
        }
    }

    public WorkflowInstance getWorkflowInstanceById(Long workflowId) {
        org.jahia.workflow.nstep.model.WorkflowInstance workflow = (org.jahia.workflow.nstep.model.WorkflowInstance) getHibernateTemplate().get(
                org.jahia.workflow.nstep.model.WorkflowInstance.class, workflowId);
        if (workflow == null) {
            throw new ObjectRetrievalFailureException(org.jahia.workflow.nstep.model.Workflow.class, workflowId);
        }
        return workflow;
    }

    public org.jahia.workflow.nstep.model.WorkflowInstance getWorkflowInstanceByObjectKey(String objectKey,
                                                                                          String languageCode) {
        List<?> list = getHibernateTemplate().find("select max(wii.id) from WorkflowInstance wii where wii.objectKey='" +
                                                objectKey + "' and wii.languageCode='" + languageCode + "'");
        if (list.size() < 1 || list.get(0) == null) {
            throw new ObjectRetrievalFailureException(Workflow.class, objectKey + " " + languageCode);
        }
        list = getHibernateTemplate().find("from WorkflowInstance wi where wi.id = " + ((Long) list.get(0)).toString());
        WorkflowInstance instance = null;
        if (list.size() > 0) {
            instance = (WorkflowInstance) list.get(0);
        }
        if (instance == null) {
            throw new ObjectRetrievalFailureException(Workflow.class, objectKey);
        }
        return instance;
    }

    public List<WorkflowInstance> getWorkflowInstancesByUser(String login) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

