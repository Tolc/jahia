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
package org.jahia.taglibs.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.jahia.content.ContentContainerKey;
import org.jahia.data.beans.ContainerBean;
import org.jahia.engines.search.FileSearchViewHandler;
import org.jahia.engines.search.SearchViewHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.services.search.savedsearch.JahiaSavedSearchView;
import org.jahia.services.search.savedsearch.JahiaSavedSearchViewSettings;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.container.ContainerTag;

/**
 * Uses view settings for the saved search results display, i.e. displayed table
 * columns, default sorting.
 * <p>
 * View settings are based on the current saved search container ID, saved
 * search query ID and the user key. If no settings were provided for the
 * current saved search box yet, uses default view settings, configured in the
 * applicationcontext-savesearch.xml file. This tag should be nested into the
 * 'results' tag and the 'content:container' tag.
 * </p>
 * <p>
 * The retrieved view settings will be applied to child 'resultTable' tag or can
 * be exposed into the page scope under the name, provided by 'var' attribute,
 * and can be passed as an attribute 'viewSettings' into the 'resultTable' tag
 * to control the view parameters.
 * </p>
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class ResultTableSettingsTag extends AbstractJahiaTag {

    private static final String DEF_ICON = "images/columns.gif";

    private static final String DEF_TITLE = "boxContainer.savedSearchBox.customizeView";

    private static final String DEF_VAR = "vewiSettings";

    private static final transient Logger logger = Logger
            .getLogger(ResultTableSettingsTag.class);

    private boolean allowChanges = true;

    private String contextId;

    private String icon = DEF_ICON;

    private String title = DEF_TITLE;

    private String var = DEF_VAR;

    private JahiaSavedSearchView view;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {

        JahiaSavedSearch query = getStoredQuery();
        try {
            view = ServicesRegistry.getInstance().getJahiaSearchService()
                    .getSavedSearchView(
                            SearchViewHandler.SEARCH_MODE_JCR,
                            query != null ? query.getId() : 0,
                            getContextId(),
                            query != null ? query.getSearchViewHandlerClass()
                                    : FileSearchViewHandler.class.getName(),
                            getProcessingContext());
        } catch (JahiaException e) {
            logger.error("Unable to retrieve the saved search view object", e);
            throw new JspTagException(e);
        }

        if (allowChanges && ((ResultsTag) findAncestorWithClass(this,
                ResultsTag.class)).getCount() > 0) {
            renderCustomizeViewButton();
        }

        return EVAL_BODY_INCLUDE;
    }

    private String getContextId() throws JspTagException {
        if (null == contextId) {
            ContainerTag containerTag = (ContainerTag) findAncestorWithClass(
                    this, ContainerTag.class);
            if (containerTag != null) {
                contextId = ContentContainerKey
                        .toObjectKeyString(((ContainerBean) pageContext
                                .getAttribute(containerTag.getId())).getID());

            } else {
                throw new JspTagException(
                        "Parent tag not found. "
                                + "This tag must be enclosed into the '<content:container>' tag.");
            }
        }

        return contextId;
    }

    private String getIconUrl() {
        String path = icon.startsWith("/") ? icon : getJahiaBean().getIncludes().getWebPath().lookup(icon);

        return ((HttpServletRequest) pageContext.getRequest()).getContextPath()
                + (path.startsWith("/") ? path : "/" + path);
    }

    private JahiaSavedSearch getStoredQuery() throws JspTagException {
        ResultsTag resultsTag = (ResultsTag) findAncestorWithClass(this,
                ResultsTag.class);
        if (resultsTag == null) {
            throw new JspTagException("Parent tag not found. "
                    + "This tag must be enclosed into the 'results' tag");
        }
        return resultsTag.getStoredQuery();
    }

    private String getTitle() {
        return title.equals(DEF_TITLE) ? getMessage(DEF_TITLE) : title;
    }

    public String getVar() {
        return var;
    }

    public JahiaSavedSearchViewSettings getViewSettings() {
        return view.getSettings();
    }

    private void renderCustomizeViewButton() throws JspTagException {
        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("searchMode", view.getSearchMode());
        params.put("saveSearchId", view.getSavedSearchId());
        params.put("contextId", view.getContextId());
        params.put("viewConfigName", view.getName());
        String customizeViewURL = null;
        try {
            customizeViewURL = EnginesRegistry.getInstance().getEngineByBeanName("customizeSaveSearchViewEngine")
                    .renderLink(getProcessingContext(),
                            params);
        } catch (JahiaException e) {
            throw new IllegalArgumentException(e);
        }
        XhtmlOutput out = new XhtmlOutput(pageContext.getOut());

        try {
            out.emptyElem("input").attr("type", "image").attr("title",
                    getTitle()).attr("src", getIconUrl());
            out
                    .attr(
                            "onclick",
                            "var myWin=window.open('"
                                    + customizeViewURL
                                    + "','customizeSaveSearchView',"
                                    + "'width=500,height=650,"
                                    + "left=10,top=10,resizable=yes,scrollbars=no,status=no');"
                                    + " myWin.focus();").end();
        } catch (IOException e) {
            throw new JspTagException(e);
        }
    }

    @Override
    protected void resetState() {
        var = DEF_VAR;
        icon = DEF_ICON;
        title = DEF_TITLE;
        contextId = null;
        allowChanges = true;
        view = null;
        super.resetState();
    }

    public void setAllowChanges(boolean allowChanges) {
        this.allowChanges = allowChanges;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
