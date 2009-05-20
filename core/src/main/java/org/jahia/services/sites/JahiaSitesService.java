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
//
//  JahiaSitesService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Locale;
import java.io.File;
import java.io.IOException;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public abstract class JahiaSitesService extends JahiaService {

    public static final int ORDER_BY_SERVER_NAME = 1;
    public static final int ORDER_BY_TITLE = 2;

    /**
     *
     * @return
     */
    public abstract boolean isStarted();

    public abstract void setStarted(boolean started);
    

    /**
     * return the list of all sites
     *
     * @return Iterator an Iterator of JahiaSite bean
     */
    public abstract Iterator<JahiaSite> getSites()
            throws JahiaException;

    /**
     * return the all sites ids
     *
     * @return Iterator an Iterator of JahiaSite bean
     *
     * @todo this only returns the entries that are in the cache !! If the
     * cache was flushed in the meantime, this method is FALSE !
     */
    public abstract Integer[] getSiteIds () throws JahiaException;

    /**
     * return the site bean looking at it id
     *
     * @param int the JahiaSite id
     *
     * @return JahiaSite the JahiaSite bean
     */
    public abstract JahiaSite getSite (int id)
            throws JahiaException;


    /**
     * return a site looking at its key
     *
     * @param String site key
     *
     * @return JahiaSite the JahiaSite bean or null
     */
    public abstract JahiaSite getSiteByKey (String siteKey)
            throws JahiaException;

    /**
     * Find a site by it's server name value
     *
     * @param serverName the server name to look for
     *
     * @return if found, returns the site with the corresponding serverName,
     *         or the first one if there are multiple, or null if there are none.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database.
     */
    public abstract JahiaSite getSiteByServerName (String serverName)
            throws JahiaException;

    /**
     * return a site looking at its server name
     *
     * @param String site name
     *
     * @return JahiaSite the JahiaSite bean or null
     */
    public abstract JahiaSite getSite (String name)
            throws JahiaException;


    /**
     * Add a new site only if there is no other site with same server name
     *
     * @return boolean false if there is another site using same server name
     *
     * @auhtor NK
     */
    public abstract JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr, Properties settings,
                                      Locale selectedLocale,
                                      String selectTmplSet, String firstImport, File fileImport, String fileImportName,
                                      Boolean asAJob, Boolean doImportServerPermissions, ProcessingContext jParams) throws JahiaException, IOException;


    /**
     * remove a site
     *
     * @param JahiaSite the JahiaSite bean
     */
    public abstract void removeSite (JahiaSite site)
            throws JahiaException;


    /**
     * Update a JahiaSite definition
     *
     * @param JahiaSite the site bean object
     */
    public abstract void updateSite (JahiaSite site)
            throws JahiaException;
    
    /**
     * Update given properties of a JahiaSite definition
     *
     * @param JahiaSite the site bean object
     * @�aram Properties the properties to update
     */    
    public abstract void updateSiteProperties (JahiaSite site, Properties props) throws JahiaException;


    public abstract void removeSiteProperties (JahiaSite site, List<String> propertiesToBeRemoved) throws JahiaException;
    //-------------------------------------------------------------------------
    /**
     * Return the amount of sites in the system.
     *
     * @return Amount of sites in the system.
     */
    public abstract int getNbSites ()
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Add a site to the list of site going to be deleted
     *
     * @param int siteID
     */
    public abstract void addSiteToDelete (int siteID);

    //-------------------------------------------------------------------------
    /**
     * Remove a given site from the list of site going to be deleted
     *
     * @param int siteID
     */
    public abstract void removeSiteToDelete (int siteID);

    //-------------------------------------------------------------------------
    /**
     * Return true if the given site is going to be deleted
     *
     * @param int the site id
     *
     * @return boolean
     */
    public abstract boolean isSiteToBeDeleted (int siteID);

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of a site
     *
     * @param int siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getSiteAsDOM (int siteID) throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of a site's properties
     *
     * @param int siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getSitePropsAsDOM (int siteID) throws JahiaException;

    /**
     * Returns a properties as a String.
     *
     * @param int    siteid
     * @param String the property name
     */
    public abstract String getProperty (int siteID, String key) throws JahiaException;

    public abstract JahiaSite getDefaultSite();

    public abstract void setDefaultSite(JahiaSite site);

    public abstract List<JahiaSite> findSiteByPropertyNameAndValue(String name, String value) throws JahiaException;
}

