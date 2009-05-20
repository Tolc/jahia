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
 package org.jahia.admin.permissions;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.JahiaAdministration;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ProcessingContext;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclEntryPK;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.security.Principal;

/**
 * User: Serge Huber
 * Date: 27 d�c. 2005
 * Time: 14:16:23
 * Copyright (C) Jahia Inc.
 */
public class ManageServerPermissions extends AbstractAdministrationModule {

    public static final String SERVER_PERMISSIONS_PREFIX = "org.jahia.actions.server.";

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageServerPermissions.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service(HttpServletRequest request,
                                        HttpServletResponse response)
            throws Exception {
        String operation = request.getParameter ("sub");

        if (operation.equals ("display")) {
            displayPermissions (request, response, request.getSession());
        } else if (operation.equals ("process")) {
            processPermissions (request, response, request.getSession());
        }
    }

    /**
     * Display the server settings page, using doRedirect().
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displayPermissions (HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException {

        JahiaSite jahiaSite =  (JahiaSite) session.getAttribute( ProcessingContext.SESSION_SITE );
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        try {
            // prepare data for display.
            JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            // first let's check that all the permissions are properly created for this site if they
            // were never used before.
            aclService.checkSitePermissionsExistence(jahiaSite.getID());
            List jahiaAclNameList = aclService.getAclNamesStartingWith(SERVER_PERMISSIONS_PREFIX);

            request.setAttribute("aclNameList", jahiaAclNameList);
            request.setAttribute("selectUsrGrp", EnginesRegistry.getInstance().getEngineByBeanName("selectUGEngine").renderLink(jParams, "&selectSite=true"));
            request.setAttribute("siteID", new Integer(jahiaSite.getID()));
            request.setAttribute("sitesList", JahiaAdministration.getAdminGrantedSites(jParams.getUser()));
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "permissions/server_permissions.jsp");
        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }
    }


    /**
     * Process and check the validity of the server settings page. If they are
     * not valid, display the server settings page to the user.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processPermissions (HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException {

        // process request parameters
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        List jahiaAclNameList = aclService.getAclNamesStartingWith(SERVER_PERMISSIONS_PREFIX);

        Iterator jahiaAclNameIter = jahiaAclNameList.iterator();
        while (jahiaAclNameIter.hasNext()) {
            JahiaAclName jahiaAclName = (JahiaAclName) jahiaAclNameIter.next();
            String jsSafeJahiaAclName = jahiaAclName.getAclName().replace('.', '_');
            Set members = getFormMembers(jsSafeJahiaAclName, jParams);
            // we found the updated list of members, let's (re)set the ACL.
            if (logger.isDebugEnabled()) {
                logger.debug(jahiaAclName.getAclName() + " members=" + members);
            }

            jahiaAclName.getAcl().clearEntries(ACLInfo.GROUP_TYPE_ENTRY);
            jahiaAclName.getAcl().clearEntries(ACLInfo.USER_TYPE_ENTRY);
            JahiaAcl acl = jahiaAclName.getAcl();
            Iterator memberIter = members.iterator();
            while (memberIter.hasNext()) {
                Principal principal = (Principal) memberIter.next();
                JahiaAclEntry aclEntry = null;
                if (principal instanceof JahiaGroup) {
                    JahiaGroup jahiaGroup = (JahiaGroup) principal;
                    aclEntry = new JahiaAclEntry(
                            new JahiaAclEntryPK(acl, new Integer(ACLInfo.GROUP_TYPE_ENTRY),
                                    jahiaGroup.getGroupKey()),
                            0, 0);
                    aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
                    acl.setGroupEntry(jahiaGroup, aclEntry);
                } else if (principal instanceof JahiaUser) {
                    JahiaUser jahiaUser = (JahiaUser) principal;
                    aclEntry = new JahiaAclEntry(
                            new JahiaAclEntryPK(acl, new Integer(ACLInfo.USER_TYPE_ENTRY),
                                    jahiaUser.getUserKey()),
                            0, 0);
                    aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
                    acl.setUserEntry(jahiaUser, aclEntry);
                }
            }
            // this not only updates the cache but also persists modifications in database
            ServicesRegistry.getInstance().getJahiaACLManagerService().updateCache(acl);
        }
        ServicesRegistry.getInstance().getJahiaACLManagerService().flushCache();
        displayPermissions (request, response, session);
    }

    private Set getFormMembers(String formName, ProcessingContext ctx) {
        String[] authMembersStr = null;
        String param = ctx.getParameter(formName);
        if (param != null) {
            authMembersStr = StringUtils.split(param, ",");
        }
        Set membersSet = new HashSet();
        if (authMembersStr != null) {
            for (String member : authMembersStr) {
                String principalName = member.substring(1);
                if (member.charAt(0) == 'u') {
                    JahiaUser user = ServicesRegistry.getInstance()
                            .getJahiaUserManagerService().lookupUser(
                                    principalName);
                    if (user != null) {
                        membersSet.add(user);
                    }
                } else {
                    JahiaGroup group = ServicesRegistry.getInstance()
                            .getJahiaGroupManagerService().lookupGroup(
                                    ctx.getSiteID(), principalName);
                    if (group != null) {
                        membersSet.add(group);
                    }
                }
            }
        }
        return membersSet;
    }

}
