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
package org.jahia.services.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PomUtils;
import org.jahia.utils.i18n.ResourceBundles;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for SCM related module operations.
 * 
 * @author Sergiy Shyrkov
 */
public class SourceControlHelper {

    private static class ModuleInfo {
        String id;
        String groupId;
        File path;
        String version;
    }

    private static final Logger logger = LoggerFactory.getLogger(SourceControlHelper.class);

    private static final NameFileFilter POM_XML_FILE_FILTER = new NameFileFilter("pom.xml");

    private SourceControlFactory sourceControlFactory;

    private TemplatePackageRegistry templatePackageRegistry;

    public JCRNodeWrapper checkoutModule(final File moduleSources, final String scmURI, final String branchOrTag,
            final String moduleId, final String version, final JCRSessionWrapper session) throws IOException,
            RepositoryException, BundleException {
        boolean newModule = moduleSources == null;
        File sources = ensureModuleSourceFolder(moduleSources);

        try {
            // checkout sources from SCM
            SourceControlManagement scm = sourceControlFactory.checkoutRepository(sources, scmURI, branchOrTag, false);

            // verify the sources and found out module information
            ModuleInfo moduleInfo = getModuleInfo(sources, scmURI, moduleId, version, branchOrTag);
            if (templatePackageRegistry.containsId(moduleInfo.id) && !moduleInfo.groupId.equals(templatePackageRegistry.lookupById(moduleInfo.id).getGroupId())) {
                FileUtils.deleteDirectory(sources);
                throw new RepositoryException("Cannot checkout module " + moduleInfo.id + " because another module with the same artifactId exists");
            }

            if (newModule) {
                File newPath = new File(sources.getParentFile(), moduleInfo.id + "_" + moduleInfo.version);
                int i = 0;
                while (newPath.exists()) {
                    newPath = new File(sources.getParentFile(), moduleInfo.id + "_" + moduleInfo.version
                            + "_" + (++i));
                }

                FileUtils.moveDirectory(sources, newPath);
                moduleInfo.path = new File(moduleInfo.path.getPath().replace(sources.getPath(),
                        newPath.getPath()));
                sources = newPath;
                scm = sourceControlFactory.getSourceControlManagement(moduleInfo.path);
            }

            if (sources.equals(moduleInfo.path)) {
                setSCMConfigInPom(sources, scmURI);
            }

            JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .compileAndDeploy(moduleInfo.id, moduleInfo.path, session);
            if (pack != null) {
                JCRNodeWrapper node = session.getNode("/modules/" + pack.getIdWithVersion());
                pack.setSourceControl(scm);
                setSourcesFolderInPackageAndNode(pack, moduleInfo.path, node);
                session.save();

                // flush resource bundle cache
                ResourceBundles.flushCache();
                NodeTypeRegistry.getInstance().flushLabels();

                return node;
            } else {
                FileUtils.deleteDirectory(sources);
            }
        } catch (BundleException e) {
            FileUtils.deleteQuietly(sources);
            throw e;
        } catch (RepositoryException e) {
            FileUtils.deleteQuietly(sources);
            throw e;
        } catch (IOException e) {
            FileUtils.deleteQuietly(sources);
            throw e;
        } catch (DocumentException e) {
            FileUtils.deleteQuietly(sources);
            throw new IOException(e);
        } catch (XmlPullParserException e) {
            FileUtils.deleteQuietly(sources);
            throw new IOException(e);
        }

        return null;
    }

    public boolean checkValidSources(JahiaTemplatesPackage pack, File sources) {
        if (!SettingsBean.getInstance().isDevelopmentMode()) {
            return false;
        }
        if (!new File(sources, "src/main/resources").exists() && !new File(sources, "src/main/webapp").exists()) {
            return false;
        }
        File pom = new File(sources, "pom.xml");
        if (pom.exists()) {
            try {
                String sourceVersion = PomUtils.getVersion(pom);
                if (sourceVersion != null && sourceVersion.equals(pack.getVersion().toString())) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("Cannot parse pom.xml file at " + pom, e);
            }
        }
        return false;
    }

    private File ensureModuleSourceFolder(File moduleSources) throws IOException {
        File sources = moduleSources;
        if (sources == null) {
            sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources", UUID.randomUUID()
                    .toString());
        }

        if (sources.exists()) {
            throw new IOException("Sources folder " + sources + " already exist");
        }

        if (!sources.getParentFile().exists() && !sources.getParentFile().mkdirs()) {
            throw new IOException("Unable to create sources folder at: " + sources);
        }

        return sources;
    }

    private ModuleInfo getModuleInfo(final File sources, final String scmURI, final String moduleId,
            final String version, final String branchOrTag) throws IOException, DocumentException,
            XmlPullParserException {
        ModuleInfo info = new ModuleInfo();
        info.id = moduleId;
        info.path = sources;
        info.version = version;

        Model pom = null;

        if (!StringUtils.isEmpty(moduleId)) {
            // Find the root folder of the module inside the repository, if in a subfolder
            Collection<File> files = FileUtils.listFiles(sources, POM_XML_FILE_FILTER, TrueFileFilter.INSTANCE);
            for (File file : files) {
                Model modulePom = PomUtils.read(file);
                if (moduleId.equals(modulePom.getArtifactId())) {
                    // we've found our candidate
                    pom = modulePom;
                    info.path = file.getParentFile();
                    break;
                }
            }
        } else {
            pom = PomUtils.read(new File(sources, "pom.xml"));
        }
        if (pom != null) {
            info.id = pom.getArtifactId();
            info.groupId = pom.getGroupId();
            info.version = pom.getVersion();
            Parent parent = pom.getParent();
            if (parent != null) {
                String v = parent.getVersion();
                if (info.version == null) {
                    info.version = v;
                }

                if (info.groupId == null) {
                    info.groupId = parent.getGroupId();
                }
            }
        } else {
            FileUtils.deleteQuietly(sources);
            String msg = "Sources were not found for " + moduleId + "  " + StringUtils.defaultIfEmpty(version, "")
                    + " in " + scmURI + " " + StringUtils.defaultIfEmpty(branchOrTag, "");
            logger.error(msg);
            throw new IOException(msg);
        }

        return info;
    }

    public SourceControlFactory getSourceControlFactory() {
        return sourceControlFactory;
    }

    public File getSources(JahiaTemplatesPackage pack, JCRSessionWrapper session) throws RepositoryException {
        if (pack.getSourcesFolder() != null) {
            return pack.getSourcesFolder();
        }
        JCRNodeWrapper n = session.getNode("/modules/" + pack.getIdWithVersion());
        if (n.hasNode("j:versionInfo")) {
            JCRNodeWrapper vi = n.getNode("j:versionInfo");
            if (vi.hasProperty("j:sourcesFolder")) {
                File sources = new File(vi.getProperty("j:sourcesFolder").getString());

                if (checkValidSources(pack, sources)) {
                    pack.setSourcesFolder(sources);
                    // templatePackageRegistry.mountSourcesProvider(pack);
                    return sources;
                }
            }
        }
        return null;
    }

    public void sendToSourceControl(String moduleId, String scmURI, String scmType, JCRSessionWrapper session)
            throws RepositoryException, IOException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupById(moduleId);
        String fullUri = "scm:" + scmType + ":" + scmURI;
        final File sources = getSources(pack, session);
        SourceControlManagement scm = sourceControlFactory.checkoutRepository(sources, fullUri, null, true);
        pack.setSourceControl(scm);
        setSCMConfigInPom(sources, fullUri);
        JCRNodeWrapper node = session.getNode("/modules/" + pack.getIdWithVersion());
        setSourcesFolderInPackageAndNode(pack, sources, node);
        session.save();
        scm.add(new File(sources, "pom.xml"));
        scm.commit("Initial commit");
    }

    private void setSCMConfigInPom(File sources, String uri) {
        try {
            PomUtils.updateScm(new File(sources, "pom.xml"), uri);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setSourceControlFactory(SourceControlFactory sourceControlFactory) {
        this.sourceControlFactory = sourceControlFactory;
    }

    public void setSourcesFolderInPackage(JahiaTemplatesPackage pack, File sources) {
        if (checkValidSources(pack, sources)) {
            pack.setSourcesFolder(sources);
            try {
                SourceControlManagement sourceControlManagement = sourceControlFactory
                        .getSourceControlManagement(sources);
                if (sourceControlManagement != null) {
                    pack.setSourceControl(sourceControlManagement);
                }
            } catch (Exception e) {
                logger.error("Cannot get source control", e);
            }
        }
    }

    public void setSourcesFolderInPackageAndNode(JahiaTemplatesPackage pack, File sources, JCRNodeWrapper node)
            throws RepositoryException {
        setSourcesFolderInPackage(pack, sources);
        if (pack.getSourcesFolder() != null) {
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", pack.getSourcesFolder().getPath());
            if (pack.getSourceControl() != null) {
                try {
                    node.getNode("j:versionInfo").setProperty("j:scmURI", pack.getSourceControl().getURI());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }
}