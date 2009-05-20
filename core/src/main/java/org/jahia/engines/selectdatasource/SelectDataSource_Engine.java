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
//  SelectDataSource_Engine
//  EV      03.12.2000
//
//  getInstance()
//  authoriseRender()
//  renderLink()
//  needsJahiaData()
//  handleActions()
//

package org.jahia.engines.selectdatasource;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import org.jahia.data.JahiaData;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.*;
import org.jahia.engines.EngineRenderer;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;


public class SelectDataSource_Engine implements JahiaEngine {

    /** The engine's name. */
    public static final String ENGINE_NAME = "selectdatasource";

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SelectDataSource_Engine.class);


    /**
     * constructor EV    03.12.2000
     */
    public SelectDataSource_Engine () {
    }


    /**
     * authoriseRender EV    03.12.2000
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return (jParams.getOperationMode () == ProcessingContext.EDIT);
    }


    /**
     * renderLink EV    03.12.2000
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        JahiaField theField = (JahiaField) theObj;
        String params = "?mode=displaywindow&fid=" + theField.getID ();
        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }


    /**
     * needsJahiaData EV    03.12.2000
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }


    /**
     * handleActions EV    03.12.2000
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        String mode = jParams.getParameter ("mode");
        String ipAddr = jParams.getRemoteAddr ();
        if (mode != null) {
            if (mode.equals ("displaywindow")) {
                logger.debug (ipAddr + " is displaying SelectDataSource Window");
                displayFormWindow (jParams);

            } else if (mode.equals ("processform")) {
                logger.debug (ipAddr + " is processing SelectDataSource Form");
                processForm (jParams);
            }
        }

        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * displayFormWindow() EV    03.12.2000
     */
    public void displayFormWindow (ProcessingContext jParams) throws JahiaException {
        String urlParams = EMPTY_STRING;
        urlParams += "&mode=processform";
        String theUrl = jParams.composeEngineUrl (ENGINE_NAME, urlParams);

        StringBuffer html = new StringBuffer (
                "<center><table border=\"0\" width=\"90%\"><tr>\n");
        html.append ("<td width=\"100%\" colspan=\"2\" bgcolor=\"#333333\">\n");
        html.append ("<font face=\"arial\" size=\"2\">Please enter datasource id :</font>\n");
        html.append ("</td></tr>\n");
        html.append ("<tr><td width\"20%\" valign=\"top\">&nbsp;</td>\n");
        html.append ("<td width=\"80%\">");
        html.append ("<form method=\"POST\" action=\"" + theUrl + "\">\n");
        html.append (
                "<input type=\"text\" name=\"datasource_url\" size=\"20\" maxlenght=\"250\" value=\"http://\">");
        html.append ("</form>\n");
        html.append ("</td></tr></table>\n");

        EngineRenderer.getInstance ().render (jParams, ENGINE_NAME, html.toString ());
    }


    /**
     * processForm() EV    20.11.2000
     */
    public void processForm (ProcessingContext processingContext) throws JahiaException {
        String dsUrl;
        try {
            dsUrl = processingContext.getParameter ("datasource_url");
            ParamBean paramBean = (ParamBean) processingContext;

            PrintWriter out = paramBean.getResponse ().getWriter ();
            paramBean.getResponse ().setContentType ("text/html");
            out.println ("<center> Computing values... </center>");

            String encodedUrl = URLEncoder.encode (dsUrl);
            out.println (
                    "<script language=\"javascript\" src=\"" +
                    processingContext.settings ().getJsHttpPath () +
                    "\">");
            out.println ("</script>");
            out.println ("<script language=\"javascript\">");
            if (!dsUrl.equals (EMPTY_STRING)) {
                out.println (
                        "  CloseJahiaWindow('&engine_params=dsurl" + EngineParams.VALUE_TOKEN +
                        encodedUrl +
                        "');");
            } else {
                out.println ("  CloseJahiaWindow();");
            }
            out.println ("</script>");

        } catch (IOException ie) {
            String errorMsg = "Error while handling the SelectDataSource Window : " +
                    ie.getMessage () +
                    " -> BAILING OUT";
            logger.error (errorMsg, ie);
            throw new JahiaException ("Error while handling a Jahia window's content",
                    errorMsg, JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, ie);
        }
    }

}
