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
package org.apache.jackrabbit.core.security;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.jahia.services.content.JCRContentUtils;

import javax.jcr.*;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;
import java.util.*;

/**
 * The <code>PrivilegeRegistry</code> defines the set of <code>Privilege</code>s
 * known to the repository.
 */
public final class JahiaPrivilegeRegistry {

    private static Map<Integer, String> STANDARD_PRIVILEGES = new HashMap<Integer, String>();

    static {
        STANDARD_PRIVILEGES.put(Permission.READ, Privilege.JCR_READ);
        STANDARD_PRIVILEGES.put(Permission.SET_PROPERTY, Privilege.JCR_MODIFY_PROPERTIES);
        STANDARD_PRIVILEGES.put(Permission.ADD_NODE, Privilege.JCR_ADD_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_NODE, Privilege.JCR_REMOVE_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_PROPERTY, Privilege.JCR_MODIFY_PROPERTIES);
        STANDARD_PRIVILEGES.put(Permission.READ_AC, Privilege.JCR_READ_ACCESS_CONTROL);
        STANDARD_PRIVILEGES.put(Permission.MODIFY_AC, Privilege.JCR_MODIFY_ACCESS_CONTROL);
        STANDARD_PRIVILEGES.put(Permission.NODE_TYPE_MNGMT, Privilege.JCR_NODE_TYPE_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.VERSION_MNGMT, Privilege.JCR_VERSION_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.LOCK_MNGMT, Privilege.JCR_LOCK_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.LIFECYCLE_MNGMT, Privilege.JCR_LOCK_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.RETENTION_MNGMT, Privilege.JCR_RETENTION_MANAGEMENT);
    }

    /**
     * Per instance map containing the instance specific representation of
     * the registered privileges.
     */
    private static final Map<String, Privilege> map = new LinkedHashMap<String, Privilege>();
    private static Privilege[] registeredPrivileges;

    private NamespaceRegistry ns;

    public static void init(Session session) throws RepositoryException {
        init(session, null);
    }

    public static void addModulePrivileges(Session session, String path) throws RepositoryException {
        init(session, path);
    }

    private static void init(Session session, String path) throws RepositoryException {
        Node perms = session.getNode(path != null ? (path + "/permissions") : "/permissions");

        Set<Privilege> privileges = new HashSet<Privilege>(20);

        registerPrivileges(perms, privileges);

        for (Privilege p : privileges) {
            map.put(p.getName(), p);
        }
        registeredPrivileges = map.values().toArray(new Privilege[map.size()]);
    }
    
    public JahiaPrivilegeRegistry(NamespaceRegistry ns) {
        this.ns = ns;
    }

    private static Privilege registerPrivileges(Node node, Set<Privilege> privileges) throws RepositoryException {
        Set<Privilege> subPrivileges = new HashSet<Privilege>();

        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node subNode = (Node) ni.next();
            Privilege subPriv = registerPrivileges(subNode, privileges);
            if (subPriv != null) {
                subPrivileges.add(subPriv);
            }
        }

        try {
            String expandedName = JCRContentUtils.getExpandedName(node.getName(), node.getSession().getWorkspace().getNamespaceRegistry());
            boolean isAbstract = node.hasProperty("j:isAbstract") && node.getProperty("j:isAbstract").getBoolean();
            PrivilegeImpl priv =(PrivilegeImpl) map.get(expandedName);
            if (priv == null) {
                priv = new PrivilegeImpl(node.getName(), expandedName, isAbstract, subPrivileges, node.getPath());
            } else {
                priv.addPrivileges(subPrivileges);
            }
            privileges.add(priv);
            return priv;
        } catch (NamespaceException ne) {
            // this can happen if we are trying to register a privilege who's namespace is not yet registered, as this
            // can be the case for portlet privileges. In this case we will simply ignore it for now and register it
            // at portlet registration time.
        }
        return null;
    }

    public Set<Privilege> getPrivileges(int permissions, String workspace) throws AccessControlException, RepositoryException {
        Set<Privilege> r = new HashSet<Privilege>();

        for (Map.Entry<Integer, String> entry : STANDARD_PRIVILEGES.entrySet()) {
            if ((permissions & entry.getKey()) == entry.getKey()) {
                r.add(getPrivilege(entry.getValue(),workspace));
            }
        }
        
        // special case for MODIFY_CHILD_NODE_COLLECTION: in this case we enforce both ADD_NODE and REMOVE_NODE permissions
        if ((permissions & Permission.MODIFY_CHILD_NODE_COLLECTION) == Permission.MODIFY_CHILD_NODE_COLLECTION) {
            r.add(getPrivilege(STANDARD_PRIVILEGES.get(Permission.ADD_NODE), workspace));
            r.add(getPrivilege(STANDARD_PRIVILEGES.get(Permission.REMOVE_NODE), workspace));
        }

        return r;
    }

    /**
     * Returns all registered privileges.
     *
     * @return all registered privileges.
     */
    public static Privilege[] getRegisteredPrivileges() {
        return registeredPrivileges;
    }

    /**
     * Returns the privilege with the specified <code>privilegeName</code>.
     *
     * @param privilegeName Name of the principal.
     * @return the privilege with the specified <code>privilegeName</code>.
     * @throws AccessControlException If no privilege with the given name exists.
     * @throws RepositoryException If another error occurs.
     */
    public Privilege getPrivilege(String privilegeName, String workspaceName) throws AccessControlException,
            RepositoryException {
        if (!privilegeName.contains("{") && privilegeName.contains("/")) {
            privilegeName = StringUtils.substringAfterLast(privilegeName, "/");
        }

        privilegeName = JCRContentUtils.getExpandedName(privilegeName, ns);

        String s = JahiaAccessManager.getPrivilegeName(privilegeName, workspaceName);
        Privilege privilege = map.get(s);
        if (privilege != null) {
            return privilege;
        }
        privilege = map.get(privilegeName);
        if (privilege != null) {
            return privilege;
        }
        throw new AccessControlException("Unknown privilege " + privilegeName);
    }

    public Privilege getPrivilege(Node node) throws AccessControlException, RepositoryException {
        String privilegeName = JCRContentUtils.getExpandedName(node.getName(), ns);
        Privilege privilege = map.get(privilegeName);
        if (privilege != null) {
            return privilege;
        }
        throw new AccessControlException("Unknown privilege " + privilegeName);
    }

}
