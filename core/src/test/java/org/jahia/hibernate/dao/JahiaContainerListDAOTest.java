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
package org.jahia.hibernate.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.hibernate.model.JahiaCtnListPK;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JahiaContainerListDAO Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/06/2005</pre>
 */
public class JahiaContainerListDAOTest extends MockObjectTestCase {
// ------------------------------ FIELDS ------------------------------

    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(JahiaContainerListDAOTest.class);
    private final Integer version = Integer.MAX_VALUE;
    private JahiaContainerListDAO dao = null;

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaContainerListDAOTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaContainerListDAO) ctx.getBean("jahiaContainerListDAO");
        assertNotNull(dao);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        dao = null;
        ctx = null;
    }

    public void testGetAclContainerListIdsInSite() throws Exception {
        List list = dao.getAclContainerListIdsInSite(new Integer(1));
        checkResult(list);
    }

    public void testGetAllContainerListIds() throws Exception {
        List list = dao.getAllContainerListIds(new Integer(1),new Integer(0));
        checkResult(list);
    }

    public void testGetAllContainerListIdsForAllContainerInPage() throws Exception {
        Map map = dao.getAllContainerListIdsForAllContainerInPage(new Integer(1));
        assertNotNull(map);
        assertFalse(map.isEmpty());
        for(int i=0; i<10000;i++) {
        map = null;
        map = dao.getAllContainerListIdsForAllContainerInPage(new Integer(1));
        assertNotNull(map);
        assertFalse(map.isEmpty());
        log.debug("Size of map = "+map.size());
        }
    }

    public void testGetAllListByDefinitionID() throws Exception {
        List list = dao.getAllListByDefinitionID(new Integer(10));
        checkResult(list);
    }

    public void testGetAllStagedContainerListIds() throws Exception {
        List list = dao.getAllStagedContainerListIds(new Integer(1));
        checkResult(list);
    }

    public void testGetAllStagingContainerListIds() throws Exception {
        List list = dao.getAllStagingContainerListIds(new Integer(1));
        checkResult(list);
    }

    public void testGetAllStagingTopLevelContainerListIds() throws Exception {
        List list = dao.getAllStagingContainerListIds(new Integer(1));
        checkResult(list);
    }

    public void testGetAllSubContainerListIds() throws Exception {
        List list = dao.getAllSubContainerListIds(new Integer(8));
        checkResult(list);
    }

    public void testGetContainerListIdsInContainer() throws Exception {
        List list = dao.getContainerListIdsInContainer(new Integer(8));
        checkResult(list);
    }

    public void testGetIdByPageIdAndDefinitionName() throws Exception {
        Integer integer = dao.getIdByPageIdAndDefinitionName(new Integer(1), "contentContainermain_1");
        assertNotNull(integer);
    }

    public void testGetListByPageAndDefinitionID() throws Exception {
        List list = dao.getListByPageAndDefinitionID(new Integer(1), new Integer(10));
        checkResult(list);
    }

    public void testGetNonDeletedStagingContainerListIds() throws Exception {
        List list = dao.getNonDeletedStagingContainerListIds(new Integer(1));
        checkResult(list);
    }

    public void testGetNonDeletedStagingListByPageAndDefinitionID() throws Exception {
        List list = dao.getNonDeletedStagingListByPageAndDefinitionID(new Integer(1), new Integer(10));
        checkResult(list);
    }

    public void testGetNonDeletedStagingSubContainerListIds() throws Exception {
        List list = dao.getNonDeletedStagingSubContainerListIds(new Integer(8));
        checkResult(list);
    }

    public void testGetNonDeletedStagingTopLevelContainerListIds() throws Exception {
        List list = dao.getNonDeletedStagingContainerListIds(new Integer(1), new Integer(0));
        checkResult(list);
    }

    public void testGetPublishedContainerListIds() throws Exception {
        List list = dao.getPublishedContainerListIds(new Integer(1),new Integer(0));
        checkResult(list);
    }

    public void testGetPublishedListByDefinitionID() throws Exception {
        List list = dao.getPublishedListByDefinitionID(new Integer(10));
        checkResult(list);
    }

    public void testGetPublishedSubContainerListIds() throws Exception {
        List list = dao.getPublishedSubContainerListIds(new Integer(8));
        checkResult(list);
    }

    public void testGetStagingListByDefinitionID() throws Exception {
        List list = dao.getStagingListByDefinitionID(new Integer(10));
        checkResult(list);
    }

    public void testGetStagingListByPageAndDefinitionID() throws Exception {
        List list = dao.getStagingListByPageAndDefinitionID(new Integer(1), new Integer(10));
        checkResult(list);
    }

    public void testGetTopLevelContainerListIds() throws Exception {
        List list = dao.getPublishedContainerListIds(new Integer(1), new Integer(0));
        checkResult(list);
    }

    public void testGetVersionedContainerListIds() throws Exception {
        List list = dao.getVersionedContainerListIds(new Integer(1),new Integer(0), version);
        checkResult(list);
    }

    public void testGetVersionedListByDefinitionID() throws Exception {
        List list = dao.getVersionedListByDefinitionID(new Integer(10),version);
        checkResult(list);
    }

    public void testGetVersionedListByPageAndDefinitionID() throws Exception {
        List list = dao.getVersionedListByPageAndDefinitionID(new Integer(1), new Integer(10), version);
        checkResult(list);
    }

    public void testGetVersionedSubContainerListIds() throws Exception {
        List list = dao.getVersionedSubContainerListIds(new Integer(8),version);
        checkResult(list);
    }

    public void testGetVersionedTopLevelContainerListIds() throws Exception {
        List list = dao.getVersionedContainerListIds(new Integer(1),new Integer(0), version);
        checkResult(list);
    }

    private void checkResult(List list) {
        assertNotNull(list);
        assertTrue(list.size() > 0);
        assertTrue(list.get(0) instanceof Integer);
    }

    public void testDelete() throws Exception {
        dao.delete(new JahiaCtnListPK(new Integer(14), version, new Integer(1)));
    }
}

