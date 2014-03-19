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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.services.content.decorator;

import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the content node.
 * User: toto
 * Date: 7 févr. 2008
 * Time: 11:53:30
 */
public class JCRFileContent {
    protected static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFileContent.class);
    protected JCRNodeWrapper node;
    protected Node objectNode;
    protected Node contentNode;

    public JCRFileContent(JCRNodeWrapper node, Node objectNode) {
        this.node = node;
        this.objectNode = objectNode;
    }

    public InputStream downloadFile () {
        try {
            Property p = getContentNode().getProperty(Constants.JCR_DATA);
            return p.getBinary().getStream();
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public void uploadFile(final InputStream is, String contentType) {
        try {
            Node content;
            if (objectNode.hasNode(Constants.JCR_CONTENT)) {
                content = objectNode.getNode(Constants.JCR_CONTENT);
            } else if (!objectNode.isNodeType(Constants.JAHIANT_RESOURCE)) {
                content = objectNode.addNode(Constants.JCR_CONTENT, Constants.JAHIANT_RESOURCE);
            } else {
                content = objectNode;
            }
            if (content.hasProperty(Constants.JCR_DATA)) {
                content.getProperty(Constants.JCR_DATA).remove();
            }
            Binary bin = null;
            try {
                bin = new BinaryImpl(is);
                content.setProperty(Constants.JCR_DATA, bin);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (bin != null) {
                    bin.dispose();
                }
            }
            if (contentType == null) {
                contentType = "application/binary";
            }
            content.setProperty(Constants.JCR_MIMETYPE, contentType);
            contentNode = content;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public String getContentType() {
        try {
            return getContentNode().getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }
    
    /**
     * The encoding is an optional property, can be null. 
     * @return file encoding or null if not set
     */
    public String getEncoding() {
        try {
            Node content = getContentNode();
            if (content.hasProperty(Constants.JCR_ENCODING)) {
                return content.getProperty(Constants.JCR_ENCODING).getString();
            }
        } catch (RepositoryException e) {
        }
        return null;
    }

    public long getContentLength() {
        try {
            Node content = getContentNode();
            return content.getProperty(Constants.JCR_DATA).getLength();
        } catch (RepositoryException e) {
        }
        return 0L;
    }
    
    protected Node getContentNode() throws PathNotFoundException, RepositoryException {
        if (contentNode == null) {
            contentNode = objectNode.getNode(Constants.JCR_CONTENT);
        }
        return contentNode;
    }

    public String getExtractedText() {
        try {
            Node content = getContentNode();
            Property extractedTextProp = content.getProperty(Constants.EXTRACTED_TEXT);
            return extractedTextProp != null ? extractedTextProp.getString() : null;
        } catch (RepositoryException e) {
        }
        return null;
    }    

    public boolean isImage() {
        try {
            final String extens = node.getPath().substring(node.getPath().lastIndexOf(".")).toLowerCase();
            return (extens.indexOf("jpg") != -1) || (extens.indexOf("jpe") != -1) || (extens.indexOf("gif") != -1) ||
                    (extens.indexOf("png") != -1) || (extens.indexOf("bmp") != -1) || (extens.indexOf("tif") != -1);
        } catch (Exception e) {
            return false;
        }
    }

    public String getText() {
        try {
            Node content = getContentNode();
            Property extractedTextProp = content.getProperty(Constants.JCR_DATA);
            return extractedTextProp != null ? extractedTextProp.getString() : null;
        } catch (RepositoryException e) {
        }
        return null;
    }
}
