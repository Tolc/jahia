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

import javax.jcr.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 5, 2008
 * Time: 5:18:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryImpl implements Repository {
    public String[] getDescriptorKeys() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDescriptor(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session login(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session login() throws LoginException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
