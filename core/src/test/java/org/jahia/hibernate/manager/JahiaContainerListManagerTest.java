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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.hibernate.dao.JahiaContainerDefinitionDAO;
import org.jahia.hibernate.dao.JahiaContainerListDAO;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JahiaContainerListManagerTest extends MockObjectTestCase {
    private JahiaContainerListManager jahiaContainerListManager;
    private Log log = LogFactory.getLog(JahiaContainerListManagerTest.class);
    private Mock dao;
    private Integer one = new Integer(1);
    private Integer zero = new Integer(0);
    private Integer ten = new Integer(10);
    private ClassPathXmlApplicationContext ctx;

    public JahiaContainerListManagerTest() {
        super();
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

    protected void setUp() throws Exception {
        super.setUp();
        jahiaContainerListManager = new JahiaContainerListManager();
        assertNotNull(jahiaContainerListManager);
        dao = new Mock(JahiaContainerListDAO.class);
        jahiaContainerListManager.setJahiaContainerListDAO((JahiaContainerListDAO) dao.proxy());
        jahiaContainerListManager.setJahiaContainerDefinitionDAO((JahiaContainerDefinitionDAO) ctx.getBean("jahiaContainerDefinitionDAO"));
    }

    public void testGetContainerListIdsByDefinition() throws Exception {
        // Test call to dao.getStagingListByPageAndDefinitionID
        List retVal = new ArrayList(1);
        retVal.add(new Integer(1));
        dao.expects(once()).method("getStagingListByPageAndDefinitionID").with(eq(one),eq(ten)).will(returnValue(retVal));
        List retVal2 = jahiaContainerListManager.getContainerListIdsByDefinition(1,10,new EntryLoadRequest(2,0,null));
        dao.verify();
        assertEquals(retVal2,retVal);
        dao.expects(once()).method("getVersionedListByPageAndDefinitionID").with(eq(one),eq(ten),eq(one)).will(returnValue(retVal));
        List retVal3 = jahiaContainerListManager.getContainerListIdsByDefinition(1,10,new EntryLoadRequest(-1,1,null));
        dao.verify();
        assertEquals(retVal3,retVal);
        dao.expects(once()).method("getListByPageAndDefinitionID").with(eq(one),eq(ten)).will(returnValue(retVal));
        List retVal4 = jahiaContainerListManager.getContainerListIdsByDefinition(1,10,null);
        dao.verify();
        assertEquals(retVal4,retVal);
    }

    public void testGetContainerListIdsInContainer() throws Exception {
        List retVal = new ArrayList(1);
        retVal.add(new Integer(1));
        dao.expects(once()).method("getContainerListIdsInContainer").with(eq(one)).will(returnValue(retVal));
        List retVal2 = jahiaContainerListManager.getContainerListIdsInContainer(1);
        dao.verify();
        assertEquals(retVal2,retVal);
    }

    public void testGetIdByPageIdAndDefinitionName() throws Exception {
        dao.expects(once()).method("getIdByPageIdAndDefinitionName").with(eq(one),eq("")).will(returnValue(1));
        int retVal2 = jahiaContainerListManager.getIdByPageIdAndDefinitionName("",1);
        dao.verify();
        assertEquals(retVal2,1);
    }

    public void testGetSubContainerListIDs() throws Exception {
        // Test call to dao.getStagingListByPageAndDefinitionID
        List retVal = new ArrayList(1);
        retVal.add(new Integer(1));
        dao.expects(once()).method("getAllSubContainerListIds").with(eq(one)).will(returnValue(retVal));
        List retVal2 = jahiaContainerListManager.getSubContainerListIDs(1,new EntryLoadRequest(2,0,null));
        dao.verify();
        assertEquals(retVal2,retVal);
        dao.expects(once()).method("getVersionedSubContainerListIds").with(eq(one),eq(one)).will(returnValue(retVal));
        List retVal3 = jahiaContainerListManager.getSubContainerListIDs(1,new EntryLoadRequest(-1,1,null));
        dao.verify();
        assertEquals(retVal3,retVal);
        dao.expects(once()).method("getPublishedSubContainerListIds").with(eq(one)).will(returnValue(retVal));
        List retVal4 = jahiaContainerListManager.getSubContainerListIDs(1,null);
        dao.verify();
        assertEquals(retVal4,retVal);
    }

    public void testGetPageTopLevelContainerListIDs() throws Exception {
        // Test call to dao.getStagingListByPageAndDefinitionID
        List retVal = new ArrayList(1);
        retVal.add(new Integer(1));
        dao.expects(once()).method("getAllContainerListIds").with(eq(one),eq(zero)).will(returnValue(retVal));
        List retVal2 = jahiaContainerListManager.getPageTopLevelContainerListIDs(1,new EntryLoadRequest(2,0,null));
        dao.verify();
        assertEquals(retVal2,retVal);
        dao.expects(once()).method("getVersionedContainerListIds").with(eq(one),eq(zero),eq(one)).will(returnValue(retVal));
        List retVal3 = jahiaContainerListManager.getPageTopLevelContainerListIDs(1,new EntryLoadRequest(-1,1,null));
        dao.verify();
        assertEquals(retVal3,retVal);
        dao.expects(once()).method("getPublishedContainerListIds").with(eq(one),eq(zero)).will(returnValue(retVal));
        List retVal4 = jahiaContainerListManager.getPageTopLevelContainerListIDs(1,null);
        dao.verify();
        assertEquals(retVal4,retVal);
    }

    public void testGetTopLevelContainerListIDsByDefinitionID() throws Exception {
        // Test call to dao.getStagingListByPageAndDefinitionID
        List retVal = new ArrayList(1);
        retVal.add(new Integer(1));
        dao.expects(once()).method("getStagingListByDefinitionID").with(eq(one)).will(returnValue(retVal));
        List retVal2 = jahiaContainerListManager.getTopLevelContainerListIDsByDefinitionID(1,new EntryLoadRequest(2,0,null));
        dao.verify();
        assertEquals(retVal2,retVal);
        dao.expects(once()).method("getVersionedListByDefinitionID").with(eq(one),eq(one)).will(returnValue(retVal));
        List retVal3 = jahiaContainerListManager.getTopLevelContainerListIDsByDefinitionID(1,new EntryLoadRequest(-1,1,null));
        dao.verify();
        assertEquals(retVal3,retVal);
        dao.expects(once()).method("getAllListByDefinitionID").with(eq(one)).will(returnValue(retVal));
        List retVal4 = jahiaContainerListManager.getTopLevelContainerListIDsByDefinitionID(1,null);
        dao.verify();
        assertEquals(retVal4,retVal);
    }

    public void testUpdateContainerList() throws Exception {
        Mock containerList = new Mock(JahiaContainerList.class);
        containerList.expects(atLeastOnce()).method("getctndefid").withNoArguments().will(returnValue(10));
        containerList.expects(atLeastOnce()).method("getAclID").withNoArguments().will(returnValue(23));
        containerList.expects(atLeastOnce()).method("getPageID").withNoArguments().will(returnValue(1));
        containerList.expects(atLeastOnce()).method("getParentEntryID").withNoArguments().will(returnValue(0));
        containerList.expects(atLeastOnce()).method("getProperties").withNoArguments().will(returnValue(null));
        containerList.expects(atLeastOnce()).method("getID").withNoArguments().will(returnValue(2));
        Mock saveVersion = new Mock(JahiaSaveVersion.class);
        saveVersion.expects(atLeastOnce()).method("isStaging").withNoArguments().will(returnValue(true));
        saveVersion.expects(atLeastOnce()).method("getWorkflowState").withNoArguments().will(returnValue(2));
        jahiaContainerListManager.setJahiaContainerListDAO((JahiaContainerListDAO) ctx.getBean("jahiaContainerListDAO"));
        jahiaContainerListManager.updateContainerList((JahiaContainerList) containerList.proxy(),
                                                      (JahiaSaveVersion) saveVersion.proxy());
        saveVersion.verify();
        containerList.verify();
    }
}