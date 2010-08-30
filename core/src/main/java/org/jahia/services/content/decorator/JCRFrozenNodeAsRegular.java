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

package org.jahia.services.content.decorator;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.lang.reflect.Array;
import java.util.*;

/**
 * JCR Frozen node that acts as a regular node, to be able to render them using our regular templating mechanism.
 * This node stores a date that will be used to retrieve version of the child objects "as close as possible" to that
 * date.
 *
 * @todo Implementation is not complete at all !!
 *
 * @author loom
 *         Date: Mar 12, 2010
 *         Time: 10:03:58 AM
 */
public class JCRFrozenNodeAsRegular extends JCRFrozenNode {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRFrozenNodeAsRegular.class);

    private Date versionDate;
    private boolean parentAlreadyResolved = false;
    private JCRNodeWrapper resolvedParentNode = null;

    public JCRFrozenNodeAsRegular(JCRNodeWrapper node, Date versionDate) {
        super(node);
        this.versionDate = versionDate;
    }

    private List<JCRNodeWrapper> internalGetChildren() throws RepositoryException {
        NodeIterator ni1 = node.getNodes();
        List<JCRNodeWrapper> childEntries = new ArrayList<JCRNodeWrapper>();
        while (ni1.hasNext()) {
            Node child = (Node) ni1.next();
            try {
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) node.getSession().getNodeByIdentifier(child.getProperty("jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    if (closestVersion != null) {
                        childEntries.add(new JCRFrozenNodeAsRegular((JCRNodeWrapper)closestVersion.getFrozenNode(), versionDate));
                    }
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    childEntries.add(getSession().getNodeByUUID(child.getProperty(Constants.JCR_FROZENUUID).getString()));
                } else {
                    // skip
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
                logger.debug("Item was not found in this workspace", e);
            }
        }
        return childEntries;
    }

    private JCRNodeWrapper superGetNode(String relPath) throws RepositoryException {
        return super.getNode(relPath);
    }

    @Override
    public JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException {
        if (relPath.startsWith("/")) {
            throw new IllegalArgumentException("relPath in not a relative path "+relPath);
        }
        StringTokenizer st = new StringTokenizer(relPath,"/");
        JCRNodeWrapper current = this;
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (next.equals("..")) {
                current = current.getParent();
            } else if (next.equals(".")) {

            } else {
                JCRNodeWrapper child = null;
                if (current instanceof JCRFrozenNodeAsRegular) {
                    child = ((JCRFrozenNodeAsRegular)current).superGetNode(next);
                } else {
                    child = current.getNode(next);
                }
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) node.getSession().getNodeByIdentifier(child.getProperty("jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    if (closestVersion != null) {
                        current = new JCRFrozenNodeAsRegular((JCRNodeWrapper) closestVersion.getFrozenNode(), versionDate);
                    } else {
                        throw new ItemNotFoundException(relPath);
                    }
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    current = new JCRFrozenNodeAsRegular(child, versionDate);
                } else {
                    current = child;
                }
            }
        }
        return current;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> childEntries = internalGetChildren();
        return new NodeIteratorImpl(childEntries.iterator(), childEntries.size());
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return ChildrenCollectorFilter.collectChildNodes(this, nameGlobs);
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return ChildrenCollectorFilter.collectChildNodes(this, namePattern);
    }

    @Override
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        return super.getPropertiesAsString();
    }

    @Override
    public String getPrimaryNodeTypeName() throws RepositoryException {
        String frozenPrimaryNodeType = node.getPropertyAsString(Constants.JCR_FROZENPRIMARYTYPE);
        return frozenPrimaryNodeType;
    }

    @Override
    public JCRNodeWrapper getFrozenVersion(String name) {
        return this;
    }

    @Override
    public JCRNodeWrapper getFrozenVersionAsRegular(Date versionDate) {
        return this;
    }

    @Override
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (parentAlreadyResolved) {
            return resolvedParentNode;
        }
        JCRPropertyWrapper property = getProperty("j:fullpath");
        if(property!=null) {
            String path = StringUtils.substringBeforeLast(property.getString(), "/");
            if(!"".equals(path))
            return getSession().getNode(path);
        }
        return null;
        /*
        JCRNodeWrapper parentNode = super.getParent();

        if (parentNode.isNodeType(Constants.NT_FROZENNODE)) {
            resolvedParentNode = new JCRFrozenNodeAsRegular(parentNode, versionDate);
            parentAlreadyResolved = true;
            return resolvedParentNode;
        } else if (parentNode.isNodeType(Constants.NT_VERSION)) {
            JCRNodeWrapper closestVersionedChildNode = findClosestParentVersionedChildNode((Version)parentNode);
            if (closestVersionedChildNode != null) {
                resolvedParentNode = new JCRFrozenNodeAsRegular(closestVersionedChildNode.getParent(), versionDate);
                parentAlreadyResolved = true;
                return resolvedParentNode;
            } else {
                resolvedParentNode = findRegularParentNode();
                parentAlreadyResolved = true;
                return resolvedParentNode;
            }
        } else {
            // this shouldn't happen, EVER !
            logger.error("Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
            return null;
        } */
    }

    private JCRNodeWrapper findRegularParentNode() throws RepositoryException {
        // This can happen in the case that the parent is not versioned (yet), so we must search in the regular
        // workspace.
        String frozenUUID = getProperty(Constants.JCR_FROZENUUID).getString();
        JCRNodeWrapper regularNode = getSession().getNodeByUUID(frozenUUID);
        if (regularNode != null) {
            return regularNode.getParent();
        } else {
            // this can happen in the case the node was deleted.
            return null;
        }
    }

    private JCRNodeWrapper findClosestParentVersionedChildNode(Version version) throws RepositoryException {
        Query childQuery = getSession().getWorkspace().getQueryManager().createQuery("select * from [nt:versionedChild] where [jcr:childVersionHistory] = '" + version.getContainingHistory().getIdentifier() + "'", Query.JCR_SQL2);
        QueryResult childQueryResult = childQuery.execute();
        NodeIterator childIterator = childQueryResult.getNodes();
        long shortestLapse = Long.MAX_VALUE;
        JCRNodeWrapper closestVersionedChildNode = null;
        while (childIterator.hasNext()) {
            JCRNodeWrapper childNode = (JCRNodeWrapper) childIterator.nextNode();
            JCRNodeWrapper parentFrozenNode = childNode.getParent();
            if (parentFrozenNode.getParent().isNodeType(Constants.NT_VERSION)) {
                Version parentVersion = (Version) parentFrozenNode.getParent();
                long currentLapse = versionDate.getTime() - parentVersion.getCreated().getTime().getTime();
                if ((currentLapse >= 0) && (currentLapse < shortestLapse)) {
                    shortestLapse = currentLapse;
                    closestVersionedChildNode = childNode;
                }
            } else if (parentFrozenNode.getParent().isNodeType(Constants.NT_FROZENNODE)) {
                // we must iterate up until we find the version node.
                Node currentFrozenNodeParent = parentFrozenNode.getParent();
                while ((currentFrozenNodeParent != null) && (currentFrozenNodeParent.isNodeType(Constants.NT_FROZENNODE))) {
                    currentFrozenNodeParent = currentFrozenNodeParent.getParent();
                }
                if (currentFrozenNodeParent.isNodeType(Constants.NT_VERSION)) {
                    Version parentVersion = (Version) currentFrozenNodeParent.getParent();
                    long currentLapse = versionDate.getTime() - parentVersion.getCreated().getTime().getTime();
                    if ((currentLapse >= 0) && (currentLapse < shortestLapse)) {
                        shortestLapse = currentLapse;
                        closestVersionedChildNode = childNode;
                    }
                } else {
                    // this shouldn't happen, EVER !
                    logger.error("Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
                }
            }
        }
        return closestVersionedChildNode;
    }

    @Override
    public String getName() {

        try {
            JCRPropertyWrapper property = getProperty("j:fullpath");
            if(property!=null) {
                String name = StringUtils.substringAfterLast(property.getString(), "/");
                return name;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        String frozenPrimaryNodeType = node.getPropertyAsString(Constants.JCR_FROZENPRIMARYTYPE);
        return NodeTypeRegistry.getInstance().getNodeType(frozenPrimaryNodeType);
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return super.getProperties();
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return super.getProperties(namePattern);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return super.getProperties(nameGlobs);
    }

    @Override
    public JCRPropertyWrapper getProperty(String name) throws PathNotFoundException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                try {
                    final Node localizedNode = getI18N(locale);
                    return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                            getSession(), getProvider(), getApplicablePropertyDefinition(name),
                            name);
                } catch (ItemNotFoundException e) {
                    return super.getProperty(name);
                }
            }
        }
        return super.getProperty(name);
    }

    @Override
    public String getPropertyAsString(String name) {
        return super.getPropertyAsString(name);
    }

    @Override
    public boolean isNodeType(String
        path) throws RepositoryException {
        ExtendedNodeType primaryNodeType = getPrimaryNodeType();
        boolean result = primaryNodeType.isNodeType(path);
        if (result) {
            return result;
        }
        // let's let's check the mixin types;
        ExtendedNodeType[] mixins = getMixinNodeTypes();
        for (ExtendedNodeType mixin : mixins) {
            result = mixin.isNodeType(path);
            if (result) {
                return result;
            }
        }
        return result;
    }

    @Override
    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // @todo to be implemented.
        logger.warn("Method not (yet) implemented, defaulting to calling superclass method !");
        return super.getAncestor(i);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        List<JCRItemWrapper> ancestors = new ArrayList<JCRItemWrapper>();
        JCRPropertyWrapper property = getProperty("j:fullpath");
        StringBuilder builder = new StringBuilder("/");
        if(property!=null) {
            String[] strings = property.getString().split("/");
            for(int i=0;i<strings.length-1;i++) {
                builder.append(strings[i]);
                try {
                    ancestors.add(getSession().getNode(builder.toString()));
                } catch (PathNotFoundException nfe) {
                } catch (AccessDeniedException ade) {
                    return ancestors;
                }
                if(i>0)builder.append("/");
            }
        }
        return ancestors;
    }

    @Override
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        if (node.hasProperty(Constants.JCR_FROZENMIXINTYPES)) {
            List<ExtendedNodeType> mixin = new ArrayList<ExtendedNodeType>();
            JCRPropertyWrapper property = node.getProperty(Constants.JCR_FROZENMIXINTYPES);
            Value[] values = property.getValues();
            for (Value value : values) {
                String curMixinTypeName = value.getString();
                mixin.add(NodeTypeRegistry.getInstance().getNodeType(curMixinTypeName));
            }
            return mixin.toArray(new ExtendedNodeType[mixin.size()]);
        } else {
            return new ExtendedNodeType[0];
        }
    }

    @Override
    public String getPath() {
        String currentPath = getName();
        JCRNodeWrapper currentParent = null;
        try {
            currentParent = getParent();
            while (currentParent != null) {
                currentPath = currentParent.getName() + "/" + currentPath;
                try {
                    currentParent = currentParent.getParent();
                } catch (ItemNotFoundException infe) {
                    currentParent = null;
                }
            }
            if ((currentPath != null) && (!currentPath.startsWith("/"))) {
                currentPath = "/" + currentPath;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return currentPath;
    }

    @Override
    public Node getI18N(Locale locale) throws RepositoryException {
        Node node1 = super.getI18N(locale);
        if(node1.hasProperty("jcr:childVersionHistory")) {
            JCRVersionHistory versionHistory = (JCRVersionHistory) getSession().getNodeByUUID(node1.getProperty("jcr:childVersionHistory").getString(),
                                                             true, versionDate);
            Version v = JCRVersionService.findClosestVersion(versionHistory, versionDate);
            if (v == null) {
                return null;
            }
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            node1 = frozen;
        }
        return node1;
    }
}
