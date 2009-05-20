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
package org.jahia.taglibs.jcr.file;

import org.apache.log4j.Logger;
import org.apache.struts.taglib.TagUtils;
import org.jahia.api.Constants;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.*;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 déc. 2007
 * Time: 18:26:25
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PropertyValueTag extends TagSupport {
    
    private static final transient Logger logger = Logger
            .getLogger(PropertyValueTag.class);
    
    private String name = "propertyDefinition";
    private String property;
    private String scope;

    protected String jspSuffix ="_view.jsp";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int doStartTag() throws JspException {
        ExtendedItemDefinition itemDef = (ExtendedItemDefinition) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        FileTag ft = (FileTag) findAncestorWithClass(this, FileTag.class);

        Node objectNode = null;
        JCRStoreProvider provider = null;
        if (ft != null) {
            JCRNodeWrapper file = ft.getFile();
            objectNode = file;
            provider = file.getJCRProvider();
        }

        try {
            if (itemDef instanceof ExtendedPropertyDefinition) {
                ExtendedPropertyDefinition propDef = (ExtendedPropertyDefinition) itemDef;
                switch (propDef.getRequiredType()) {
                    case PropertyType.STRING:
                        if (SelectorType.CATEGORY == propDef.getSelector()) {
                            handleCategory(propDef, objectNode, provider);
                            break;
                        }
                    case PropertyType.LONG:
                        // long or string
                        if (propDef.isMultiple()) {
                            handleMultipleTextField(propDef, objectNode, provider);
                        } else {
                            handleTextField(propDef, objectNode, provider);
                        }
                        break;
                    case PropertyType.DATE:
                        handleDate(propDef, objectNode, provider);
                        break;
                    case PropertyType.REFERENCE:
                        handleReference(propDef, objectNode, provider);
                        break;
                    case PropertyType.BOOLEAN:
                        handleBoolean(propDef, objectNode, provider);
                        break;
                }
            } else {
                ExtendedNodeDefinition nodeDef = (ExtendedNodeDefinition) itemDef;
                NodeType[] nts = nodeDef.getRequiredPrimaryTypes();
                List names = new ArrayList();
                for (int i = 0; i < nts.length; i++) {
                    NodeType nt = nts[i];
                    names.add(nt.getName());
                }
                if (names.contains(Constants.NT_RESOURCE) || names.contains(Constants.JAHIANT_RESOURCE)) {
                    handleContent(nodeDef, objectNode, provider);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        name = "propertyDefinition";
        property = null;
        scope = null;
        jspSuffix = "_view.jsp";

        return EVAL_PAGE;
    }

    protected void handleTextField(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        JspWriter out = pageContext.getOut();

        String name = propDef.getName();

        if (objectNode != null && objectNode.hasProperty(name)) {
            String value = objectNode.getProperty(name).getString();
            out.print(value);
        } else {
            out.print("-");
        }
    }

    protected void handleMultipleTextField(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        JspWriter out = pageContext.getOut();

        String name = propDef.getName();
        Value[] values = null;
        if (objectNode != null && objectNode.hasProperty(name)) {
            List results = new ArrayList();
            values = objectNode.getProperty(name).getValues();

            for (int i = 0; i < values.length; i++) {
                Value value = values[i];
                results.add(value.getString());
            }
            out.print(results);
        } else {
            out.print("-");
        }
    }

    protected void handleCategory(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        JspWriter out = pageContext.getOut();

        String name = propDef.getName();

        Value[] values = null;
        if (objectNode != null && objectNode.hasProperty(name)) {
            if (propDef.isMultiple()) {
                List results = new ArrayList();
                values = objectNode.getProperty(name).getValues();

                for (int i = 0; i < values.length; i++) {
                    String value = values[i].getString();
                    value = Category.getCategoryKey(value);
                    results.add(value);
                }
                out.print(results);
            } else {
                String value = objectNode.getProperty(name).getString();
                value = Category.getCategoryKey(value);
                out.print(value);
            }
        } else {
            out.print("-");
        }
    }

    protected void handleDate(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        ServletRequest request = pageContext.getRequest();

        Calendar value = null;
        String name = propDef.getName();

        if (objectNode != null && objectNode.hasProperty(name)) {
            value = objectNode.getProperty(name).getDate();
        }

        name = name.replace(':','_');

        request.setAttribute("propertyName", name);
        request.setAttribute("value", value);

        pageContext.include("/engines/filemanager/types/date"+jspSuffix);
    }

    protected void handleReference(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        ServletRequest request = pageContext.getRequest();
        String name = propDef.getName();

        if (objectNode != null && objectNode.hasProperty(name)) {
            Node target = objectNode.getProperty(name).getNode();
            String path = target.getPath();
            if (!provider.getMountPoint().equals("/")) {
                path = provider.getMountPoint() + path;
            }
            request.setAttribute("path", path);
            request.setAttribute("displayPath",path.replace("/"," /"));
        } else {
            request.setAttribute("path", null);
            request.setAttribute("displayPath",null);
        }

        name = name.replace(':','_');
        request.setAttribute("propertyName", name);

        pageContext.include("/engines/filemanager/types/reference"+jspSuffix);
    }

    protected void handleBoolean(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        handleTextField(propDef, objectNode, provider);
    }

    protected void handleContent(ExtendedNodeDefinition nodeDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        ServletRequest request = pageContext.getRequest();

        String name = nodeDef.getName();
        if (objectNode != null && objectNode.hasNode(name)) {
            Node node = objectNode.getNode(name);
            String path = provider.getWebdavPath() + node.getPath();
            request.setAttribute("path", path);
            request.setAttribute("isImage", Boolean.valueOf(node.getProperty(Constants.JCR_MIMETYPE).getString().startsWith("image/")));
        } else {
            request.setAttribute("path", null);
        }

        name = name.replace(':','_');
        request.setAttribute("nodeName", name);
        pageContext.include("/engines/filemanager/types/content"+jspSuffix);
    }
}
