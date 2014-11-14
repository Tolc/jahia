/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.importexport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.utils.zip.ZipEntry;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.*;
import javax.jcr.query.Query;
import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FilesAclImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FilesAclImportHandler.class);
    private static Logger corruptedFilesLogger = org.slf4j.LoggerFactory.getLogger(FilesAclImportHandler.class.getName()+".CorruptedFiles");

    private Resource archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();
    private Map<String, File> filePath = new HashMap<String, File>();

    private JahiaSite site;
    private DefinitionsMapping mapping;
    private JCRSessionWrapper session;

    private Map<String, String> davPropertiesMapping = initializeDavPropertiesMapping();

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(ImportExportService.DATE_FORMAT);
    public static final DateTimeFormatter DATE_FORMAT_Z = DateTimeFormat.forPattern(ImportExportService.DATE_FORMAT+"'Z'");

    public FilesAclImportHandler(JahiaSite site, DefinitionsMapping mapping, Resource archive, List<String> fileList, Map<String, File> filePath) {
        this.site = site;
        this.mapping = mapping;
        this.archive = archive;
        this.fileList = fileList;
        this.filePath = filePath;
        try {
            this.session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(localName) && ImportExportBaseService.JAHIA_URI.equals(uri)) {
            String path = attributes.getValue(ImportExportBaseService.JAHIA_URI, "path");
            String acl = attributes.getValue(ImportExportBaseService.JAHIA_URI, "fileacl");

            InputStream content = null;

            try {
                boolean contentFound = false;
                if(this.filePath != null){
                    content = findExtractedContent(path);
                } else {
                    contentFound = findContent(path);
                }

                if (path.startsWith("/shared") || path.startsWith("/users")) {
                    path = "/sites/" + site.getSiteKey() + "/files" + path;
                }/*
                    // DB-HOT-28
                    else if (path.startsWith("/users/")) {
                    Matcher m = Pattern.compile("/users/([^/]+)(/.*)?").matcher(path);
                    if (m.matches()) {
                        path = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(m.group(1));
                        path = path + "/files" + ((m.group(2) != null) ? m.group(2) : "");
                    }
                }*/

                if (session.itemExists(path)) {
                    return;
                }
                JCRNodeWrapper f;
                path = JCRContentUtils.escapeNodePath(path);
                path = Patterns.COLON.matcher(path).replaceAll("_");

                String parentPath = StringUtils.substringBeforeLast(path, "/");
                try {
                    f = session.getNode(parentPath);
                } catch (PathNotFoundException e) {
                    f = ImportExportBaseService.getInstance().ensureDir(session, parentPath, site);
                }


                Calendar created = new GregorianCalendar();
                String creationDate = attributes.getValue("dav:creationdate");
                if (creationDate != null) {
                    if (creationDate.endsWith("Z")) {
                        created.setTime(DATE_FORMAT_Z.parseDateTime(creationDate).toDate());
                    } else {
                        created.setTime(DATE_FORMAT.parseDateTime(creationDate).toDate());
                    }
                }
                Calendar lastModified = new GregorianCalendar();
                String modificationDate = attributes.getValue("dav:modificationdate");
                if (modificationDate != null) {
                    if (modificationDate.endsWith("Z")) {
                        lastModified.setTime(DATE_FORMAT_Z.parseDateTime(modificationDate).toDate());
                    } else {
                        lastModified.setTime(DATE_FORMAT.parseDateTime(modificationDate).toDate());
                    }
                }
                String createdBy = attributes.getValue("dav:creationuser");
                String lastModifiedBy = attributes.getValue("dav:modificationuser");
                final String itemType = "file".equals(attributes.getValue("jahia:itemType")) || StringUtils.isNotBlank(attributes.getValue("dav:getcontenttype")) ? "file" : "folder";
                final boolean binaryAvailable = content != null && !contentFound;
                final boolean isCorruptedFile = !binaryAvailable && "file".equals(itemType);

                if (isCorruptedFile) {
                    logger.error(MessageFormat.format("Impossible to import the file {0} as its binary is missing", path));
                    corruptedFilesLogger.error(path);
                } else {
                    checkoutNode(f);
                    if (!binaryAvailable) {
                        f = f.addNode(StringUtils.substringAfterLast(path, "/"), "jnt:folder", null, created, createdBy,
                                lastModified, lastModifiedBy);
                    } else {
                        f = f.addNode(StringUtils.substringAfterLast(path, "/"), "jnt:file", null, created, createdBy, lastModified, lastModifiedBy);
                        if (content != null) {
                            f.getFileContent().uploadFile(content, attributes.getValue("dav:getcontenttype"));
                        } else {
                            f.getFileContent().uploadFile(zis, attributes.getValue("dav:getcontenttype"));
                        }
                    }
                    if (acl != null && acl.length() > 0) {
                        StringTokenizer st = new StringTokenizer(acl, "|");
                        while (st.hasMoreElements()) {
                            String s = st.nextToken();
                            int beginIndex = s.lastIndexOf(":");

                            String principal = s.substring(0, beginIndex);

                            Set<String> grantedRoles = new HashSet<String>();
                            Set<String> removedRoles = new HashSet<String>();
                            String perm = s.substring(beginIndex + 1);
                            if (perm.charAt(0) == 'r') {
                                if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_READ_ROLES)) {
                                    grantedRoles.addAll(LegacyImportHandler.READ_ROLES);
                                } else {
                                    grantedRoles.addAll(LegacyImportHandler.CUSTOM_FILES_READ_ROLES);
                                }
                            } else {
                                if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_READ_ROLES)) {
                                    removedRoles.addAll(LegacyImportHandler.READ_ROLES);
                                } else {
                                    removedRoles.addAll(LegacyImportHandler.CUSTOM_FILES_READ_ROLES);
                                }
                            }
                            if (perm.charAt(1) == 'w') {
                                if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES)) {
                                    grantedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                                } else {
                                    grantedRoles.addAll(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES);
                                }
                            } else {
                                if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES)) {
                                    removedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                                } else {
                                    removedRoles.addAll(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES);
                                }
                            }

                            if (!grantedRoles.isEmpty()) {
                                f.grantRoles(principal, grantedRoles);
                            }
                            if (!removedRoles.isEmpty()) {
                                f.denyRoles(principal, removedRoles);
                            }
                        }
                    }
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String attUri = attributes.getURI(i);
                        String attName = attributes.getLocalName(i);
                        if (!ImportExportBaseService.JAHIA_URI.equals(attUri)
                                || (!"path".equals(attName) && !"fileacl".equals(attName) && !"lastModification".equals(attName))) {
                            try {
                                setPropertyField(f.getPrimaryNodeType(), localName, f, getMappedProperty(f.getPrimaryNodeType(), attributes.getQName(i)), attributes.getValue(i));
                            } catch (RepositoryException e) {
                                logger.warn("Error importing " + localName + " " + path, e);
                            }
                        }
                    }
                    session.save(JCRObservationManager.IMPORT);
                }
            } catch (Exception e) {
                logger.error("error", e);
            } finally {
                IOUtils.closeQuietly(content);
            }
        }
    }

    private String getMappedProperty(ExtendedNodeType baseType, String qName) {
        String mappedProperty = davPropertiesMapping.get(qName);
        if (mappedProperty == null && mapping != null) {
            mappedProperty = mapping.getMappedProperty(baseType, qName);
        }
        if (mappedProperty != null) {
            return mappedProperty;
        }
        return null;
    }

    protected final Map<String, String> initializeDavPropertiesMapping() {
        Map<String, String> davPropertiesMapping = new HashMap<String, String>();
        davPropertiesMapping.put("dav:getcontentlanguage", "mix:language|jcr:content/jcr:language");
        davPropertiesMapping.put("dav:displayname", "mix:title|jcr:title");
        davPropertiesMapping.put("ged:categories", "jmix:categorized|j:defaultCategory");
        return davPropertiesMapping;
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName,
                                     JCRNodeWrapper node, String propertyName, String value) throws RepositoryException {
        if (propertyName == null || "#skip".equals(propertyName)) {
            return false;
        }
        JCRNodeWrapper parent = node;
        String mixinType = null;
        if (propertyName.contains("|")) {
            mixinType = StringUtils.substringBefore(propertyName, "|");
            propertyName = StringUtils.substringAfter(propertyName, "|");
        }
        if (StringUtils.contains(propertyName, "/")) {
            String parentPath = StringUtils.substringBeforeLast(propertyName, "/");
            if (parent.hasNode(parentPath)) {
                parent = parent.getNode(parentPath);
            }
            propertyName = StringUtils.substringAfterLast(propertyName, "/");
        }
        parent = checkoutNode(parent);
        if (!StringUtils.isEmpty(mixinType) && !parent.isNodeType(mixinType)) {
            parent.addMixin(mixinType);
        }

        ExtendedPropertyDefinition propertyDefinition = null;
        propertyDefinition = parent.getApplicablePropertyDefinition(propertyName);
        if (propertyDefinition == null) {
            return false;

        }
        if (propertyDefinition.isProtected()) {
            // System.out.println("protected : " + propertyName);
            return false;
        }
        Node n = parent;

        // System.out.println("setting " + propertyName);

        if (value != null && value.length() != 0 && !value.equals("<empty>")) {
            switch (propertyDefinition.getRequiredType()) {
                case PropertyType.DATE:
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        logger.error(e.getMessage(), e);
                    }
                    break;

                default:
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.CATEGORY: {
                            String[] cats = Patterns.COMMA.split(value);
                            List<Value> values = new ArrayList<Value>();
                            for (int i = 0; i < cats.length; i++) {
                                String cat = cats[i];
                                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:category] as cat where NAME(cat) = '" + cat + "'", Query.JCR_SQL2);
                                NodeIterator ni = q.execute().getNodes();
                                if (ni.hasNext()) {
                                    values.add(session.getValueFactory().createValue(ni.nextNode()));
                                }
                            }
                            n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                            break;
                        }
                        case SelectorType.RICHTEXT: {
                            n.setProperty(propertyName, value);
                            break;
                        }
                        default: {
                            String[] vcs = propertyDefinition.getValueConstraints();
                            List<String> constraints = Arrays.asList(vcs);
                            if (!propertyDefinition.isMultiple()) {
                                if (value.startsWith("<jahia-resource")) {
                                    value = ResourceBundleMarker.parseMarkerValue(value)
                                            .getResourceKey();
                                    if (value.startsWith(propertyDefinition.getResourceBundleKey())) {
                                        value = value.substring(propertyDefinition
                                                .getResourceBundleKey().length() + 1);
                                    }
                                }
                                value = baseType != null && mapping != null ? mapping.getMappedPropertyValue(baseType,
                                        localName, value) : value;
                                if (constraints.isEmpty() || constraints.contains(value)) {
                                    try {
                                        n.setProperty(propertyName, value);
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            } else {
                                String[] strings = Patterns.TRIPPLE_DOLLAR.split(value);
                                List<Value> values = new ArrayList<Value>();
                                for (int i = 0; i < strings.length; i++) {
                                    String string = strings[i];

                                    if (string.startsWith("<jahia-resource")) {
                                        string = ResourceBundleMarker.parseMarkerValue(string)
                                                .getResourceKey();
                                        if (string.startsWith(propertyDefinition
                                                .getResourceBundleKey())) {
                                            string = string.substring(propertyDefinition
                                                    .getResourceBundleKey().length() + 1);
                                        }
                                    }
                                    value = baseType != null ? mapping.getMappedPropertyValue(
                                            baseType, localName, value) : value;
                                    if (constraints.isEmpty() || constraints.contains(value)) {
                                        values.add(new ValueImpl(string, propertyDefinition
                                                .getRequiredType()));
                                    }
                                }
                                ;
                                n.setProperty(propertyName,
                                        values.toArray(new Value[values.size()]));
                            }
                            break;
                        }
                    }
            }
        } else {
            return false;
        }

        return true;
    }

    private JCRNodeWrapper checkoutNode(JCRNodeWrapper node) throws RepositoryException {
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        return node;
    }

    private boolean findContent(String path) throws IOException {
        if (archive == null) {
            return false;
        }
        if (zis == null) {
            zis = new NoCloseZipInputStream(new BufferedInputStream(archive.getInputStream()));
            nextEntry = zis.getNextEntry();
        }

        path = JCRContentUtils.replaceColon(path);

        int fileIndex = fileList.indexOf(path);
        if (fileIndex != -1) {
            if (fileList.indexOf("/" + nextEntry.getName().replace('\\', '/')) > fileIndex) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new BufferedInputStream(archive.getInputStream()));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(path));

            return true;
        }
        return false;
    }

    private InputStream findExtractedContent(String path) throws IOException {
        path = JCRContentUtils.replaceColon(path);

        File pathInFS = filePath.get(path);
        if(pathInFS != null && pathInFS.exists()){
            if(!pathInFS.isDirectory()){
                return new BufferedInputStream(new FileInputStream(pathInFS));
            }
        }
        return null;
    }
}
