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

import junit.framework.*;
import org.jmock.cglib.MockObjectTestCase;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.content.ObjectLink;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JahiaLinkManagerTest extends MockObjectTestCase {
    JahiaLinkManager manager;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());
    // -------------------------- STATIC METHODS --------------------------

    public static Test suite() {
        return new TestSuite(JahiaAclManagerTest.class);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaLinkManagerTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        manager = (JahiaLinkManager) ctx.getBean(JahiaLinkManager.class.getName());
        assertNotNull(manager);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    public void testGetObjectLink() throws Exception {
        ObjectLink objectLink = manager.getObjectLink(1);
        assertNotNull(objectLink);
    }
}