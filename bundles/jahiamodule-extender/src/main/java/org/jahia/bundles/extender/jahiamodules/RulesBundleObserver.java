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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

/**
 * A bundle observer for Jahia Modules. Used by the BundleWatcher defined in the activator.
 */
public class RulesBundleObserver implements BundleObserver<URL> {

    private static Logger logger = LoggerFactory.getLogger(RulesBundleObserver.class);

    private TemplatePackageRegistry templatePackageRegistry;

    public RulesBundleObserver() {
        super();
        templatePackageRegistry = ((JahiaTemplateManagerService) SpringContextSingleton
                .getBean("JahiaTemplateManagerService")).getTemplatePackageRegistry();
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        if (urls.size() == 0) {
            return;
        }
        String bundleName = BundleUtils.getDisplayName(bundle);
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {

                JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);

                if (url.toString().endsWith(".dsl")) {
                    module.setRulesDescriptorFile(bundleResource.getURL().getPath().substring(1));

                    for (RulesListener listener : RulesListener.getInstances()) {
                        listener.addRulesDescriptor(bundleResource);
                    }
                }

                if (url.toString().endsWith(".drl")) {
                    module.setRulesFile(bundleResource.getURL().getPath().substring(1));

                    for (RulesListener listener : RulesListener.getInstances()) {
                        List<String> filesAccepted = listener.getFilesAccepted();
                        if(filesAccepted.contains(StringUtils.substringAfterLast(url.getPath(), "/"))) {
                            listener.addRules(bundleResource, module);
                        }
                    }
                }

                logger.info("Registered rules from file {} for bundle {}", url, bundleName);
            } catch (Exception e) {
                logger.error("Error registering rules file " + url + " for bundle " + bundle, e);
            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);
        for (RulesListener listener : RulesListener.getInstances()) {
            listener.removeRules(module);
            for (URL url : urls) {
                if(url.toString().endsWith(".dsl")) {
                    if(listener.removeRulesDescriptor(new BundleResource(url, bundle))){
                        logger.info("Removing rule file descriptor " + url);
                    }
                }
            }
        }
    }
}
