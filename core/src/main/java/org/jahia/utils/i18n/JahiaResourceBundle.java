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
package org.jahia.utils.i18n;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;

/**
 * Jahia implementation of the resource bundle, which considers module inheritance.
 * 
 * @author rincevent
 * @deprecated use {@link ResourceBundles} or {@link Messages} instead
 */
@Deprecated
public class JahiaResourceBundle extends ResourceBundle {
    private final Locale locale;
    private final List<String> bundleLookupChain;
    private ResourceBundle bundle;
    public static final String JAHIA_INTERNAL_RESOURCES = ResourceBundles.JAHIA_INTERNAL_RESOURCES;
    public static final String JAHIA_TYPES_RESOURCES = ResourceBundles.JAHIA_TYPES_RESOURCES;

    public static void flushCache() {
        ResourceBundles.flushCache();
    }
    
    public JahiaResourceBundle(Locale locale, String templatesPackageName) {
        this(null, locale, templatesPackageName, null, null);
    }

    public JahiaResourceBundle(Locale locale, String templatesPackageName, String siteTemplatesPackageName) {
        this(null, locale, templatesPackageName, null, siteTemplatesPackageName);
    }

    public JahiaResourceBundle(String basename, Locale locale) {
        this(basename, locale, null, null, null);
    }

    public JahiaResourceBundle(Locale locale, String templatesPackageName, ClassLoader classLoader) {
        this(null, locale, templatesPackageName, classLoader, null);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName) {
        this(basename, locale, templatesPackageName, null, null);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName, ClassLoader classLoader) {
        this(basename, locale, templatesPackageName, classLoader, null);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName,
            ClassLoader classLoader, String siteTemplatesPackageName) {
        this.locale = locale;
        JahiaTemplateManagerService jahiaTemplateManagerService = ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService();
        List<String> tplBundles = null;
        if (templatesPackageName != null) {
            JahiaTemplatesPackage tp = jahiaTemplateManagerService
                    .getTemplatePackage(templatesPackageName);
            if (tp != null) {
                tplBundles = tp.getResourceBundleHierarchy();
            }
        }
        String primaryBundleName = null;
        if (siteTemplatesPackageName != null
                && !siteTemplatesPackageName.equals(templatesPackageName)) {
            JahiaTemplatesPackage tp = jahiaTemplateManagerService
                    .getTemplatePackage(siteTemplatesPackageName);
            if (tp != null) {
                primaryBundleName = tp.getResourceBundleName();
            }
        }

        bundleLookupChain = new LinkedList<String>();
        if (primaryBundleName != null) {
            bundleLookupChain.add(primaryBundleName);
        }
        if (basename != null
                && (tplBundles == null || tplBundles.isEmpty() || tplBundles.get(0)
                        .equals(basename))) {
            bundleLookupChain.add(basename);
        }
        if (tplBundles != null) {
            for (String name : tplBundles) {
                if (basename == null || !basename.equals(name)) {
                    bundleLookupChain.add(name);
                }
            }
        }
        
        if (bundleLookupChain == null || bundleLookupChain.isEmpty()) {
            throw new MissingResourceException("Cannot find resource bundle for base name '"
                    + basename + "', module '" + templatesPackageName + "', site template set '"
                    + siteTemplatesPackageName + "' and locale '" + locale + "'", null, null);
        }
    }

    protected ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundles.get(bundleLookupChain, locale);
        }
        return bundle;
    }

    @Override
    public Object handleGetObject(String s) {
        return getBundle().getObject(s);
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     */
    public Enumeration<String> getKeys() {
        return getBundle().getKeys();
    }

    /**
     * Shortcut methods to call a resource key from the engine resource bundle
     *
     * @param key,          the key to search inside the JahiaInternalResources bundle
     * @param locale,       the locale in which we want to find the key
     * @param defaultValue, the defaultValue (surrounded by ???) if not found
     * @return the resource in locale language or defaultValue surrounded by (???)
     */
    public static String getJahiaInternalResource(String key, Locale locale, String defaultValue) {
        return Messages.getInternal(key, locale, defaultValue);
    }

    /**
     * Get message by key and local
     *
     * @param key
     * @param locale
     * @return
     */
    public static String getJahiaInternalResource(String key, Locale locale) {
        return Messages.getInternal(key, locale);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName) {
        return getString(bundle, key, locale, templatePackageName, null);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @param loader
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName,
                                   ClassLoader loader) {
        return Messages.get(bundle, templatePackageName != null ? ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService().getTemplatePackage(templatePackageName) : null,
                key, locale);
    }

    /**
     * Finds a ResourceBundle depending on a baseName
     *
     * @param baseName
     * @param preferredLocale
     * @return a resource bundle instance for the specified base name and locale
     */
    public static ResourceBundle lookupBundle(String baseName, Locale preferredLocale) {
        return ResourceBundles.get(baseName, preferredLocale);
    }
    
    /**
     * find  ResourceBundle depending on a baseName
     *
     * @param baseName
     * @param preferredLocale
     * @param loader
     * @return
     */
    public static ResourceBundle lookupBundle(String baseName, Locale preferredLocale, ClassLoader loader, boolean throwExeptionIfNotFound) {
        try {
            return lookupBundle(baseName, preferredLocale);
        } catch (MissingResourceException e) {
            if (throwExeptionIfNotFound) {
                throw e;
            } else {
                return null;
            }
        }
    }
    
    public static String interpolateResourceBunldeMacro(String input, Locale locale, String templatePackageName) {
        return Messages.interpolateResourceBundleMacro(input, locale,
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templatePackageName));
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String get(String key, String defaultValue) {
        return getString(key, defaultValue);
    }

    /**
     * Get formatted message
     *
     * @param key
     * @param defaultValue
     * @param arguments
     * @return
     */
    public String getFormatted(String key, String defaultValue, Object... arguments) {
        return Messages.format(get(key, defaultValue), arguments);
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        String message;
        try {
            message = getString(key);
        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }

    /**
     * Returns names of bundles where the key is looked up.
     *
     * @return list of bundles where the key is looked up
     */
    public List<String> getLookupBundles() {
        return bundleLookupChain;
    }
}