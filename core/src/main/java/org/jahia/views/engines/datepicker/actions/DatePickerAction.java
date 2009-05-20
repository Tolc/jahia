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
 package org.jahia.views.engines.datepicker.actions;

import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.jahia.exceptions.JahiaException;
import org.jahia.views.engines.JahiaEngineButtonsHelper;
import org.jahia.views.engines.JahiaEngineCommonData;
import org.jahia.views.engines.datepicker.DatePicker;
import org.jahia.views.engines.datepicker.DatePickerData;
import org.jahia.views.engines.datepicker.DatePickerEvent;

/**
 * Date Picker Action Dispatcher
 *
 */
public class DatePickerAction extends DispatchAction {
    
    private static final transient Logger logger = Logger
            .getLogger(DatePickerAction.class);

    private static final String ENGINE_TITLE = "Date Picker";
    public static final String DATE_EVENT_PARAM = "dateEventParam";

    /**
     * Display the calendar.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward displayCalendar(ActionMapping mapping,
                                         ActionForm form,
                                         HttpServletRequest request,
                                         HttpServletResponse response)
    throws IOException, ServletException {

        ActionForward forward = mapping.getInputForward();
        ActionMessages errors = new ActionMessages();
        JahiaEngineCommonData engineCommonData = null;

        try {
            init(request);

            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);

            // Prepare the Date event
            String eventObj = (String)request.getParameter(DATE_EVENT_PARAM);
            request.setAttribute(DATE_EVENT_PARAM,eventObj);

            if ( eventObj != null ){
                DatePickerEvent ev = (DatePickerEvent)request.getSession()
                                   .getAttribute(eventObj + "_dateEvent");
                if ( ev == null ){
                    ev = new DatePickerEvent(this,null,null,
                        eventObj,
                        new DatePickerData());
                }
                ev.setEventTimeToNow();
                DatePicker.getInstance().wakeupListener("beforeDisplayCalendar",ev);
                List dateErrors = ev.getDatePickerData().getErrors();
                if ( dateErrors.size()>0 ){
                    request.setAttribute("errors",dateErrors);
                }
                request.setAttribute("dateValue",DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.MEDIUM,
                        engineCommonData.getParamBean().getLocale()).format(new Date(ev.getDatePickerData().getDate())));
                request.setAttribute("dateLongValue",
                                     String.valueOf(ev.getDatePickerData().getDate()));
            }
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                       new ActionMessage("displayCalendar error"));
        }
        return continueForward(mapping,request,errors,forward);
    }

    /**
     * Save submitted date.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward save(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
    throws IOException, ServletException {

        ActionForward forward = mapping.getInputForward();
        ActionMessages errors = new ActionMessages();
        JahiaEngineCommonData engineCommonData = null;
        try {
            init(request);

            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            String dateValue = request.getParameter("datebox");
            String dateLongValue = request.getParameter("datelong");
            request.setAttribute("dateValue",dateValue);
            request.setAttribute("dateLongValue",dateLongValue);

            // Prepare the Date event
            String eventObj = request.getParameter(DATE_EVENT_PARAM);
            DatePickerEvent ev = null;
            List dateErrors = new ArrayList();
            if ( eventObj == null || dateLongValue == null ){
                errors.add(ActionMessages.GLOBAL_MESSAGE,
                           new ActionMessage("eventObj or date value is null"));
            } else {
                long dateLong = Long.parseLong(dateLongValue);
                /*
                Date choosedDate = new Date(dateLong);

                // append now hours
                Date nowDate = Calendar.getInstance().getTime();
                choosedDate.setHours(nowDate.getHours());
                choosedDate.setMinutes(nowDate.getMinutes());
                choosedDate.setSeconds(nowDate.getSeconds());
                */
                ev = new DatePickerEvent(this,null,null,eventObj,
                        new DatePickerData(dateLong));
                DatePicker.getInstance().wakeupListener("saveDate",ev);
                request.setAttribute(DATE_EVENT_PARAM,eventObj);
                dateErrors = ev.getDatePickerData().getErrors();
                request.getSession().setAttribute(eventObj + "_dateEvent",ev);
                if ( dateErrors.size()==0 ){
                    String method = request.getParameter(mapping.getParameter());
                    if ( method.equals("save") ){
                        request.setAttribute("engines.close.refresh-opener","yes");
                        request.setAttribute("engines.close.opener-url-params","&reloaded=yes");
                        forward = mapping.findForward("EnginesCloseAnyPopup");
                    } else if ( method.equals("apply") ){
                        Properties params = new Properties();
                        params.put("method","displayCalendar");
                        params.put(DATE_EVENT_PARAM,eventObj);
                        String requestURL = engineCommonData.getParamBean()
                                .composeStrutsUrl((mapping.getPath()).toString(),params,null);
                        request.setAttribute("engines.apply.new-url",requestURL);
                        request.setAttribute("engines.apply.refresh-opener","yes");
                        request.setAttribute("engines.apply.opener-url-params","&reloaded=yes");
                        forward = mapping.findForward("EnginesApplyAnyPopup");
                    }
                } else {
                    forward = displayCalendar(mapping,form,request,response);
                }
            }
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                       new ActionMessage("displayCalendar error"));
        }
        return continueForward(mapping,request,errors,forward);
    }

    /**
     * Apply Date change.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward apply(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
    throws IOException, ServletException {
        return save(mapping,form,request,response);
    }

    /**
     *
     * @param request
     * @return
     */
    private void init(HttpServletRequest request) throws JahiaException {

        // engines helpers
        JahiaEngineCommonData engineCommonData =
                new JahiaEngineCommonData(request);

        engineCommonData.setEngineTitle(DatePickerAction.ENGINE_TITLE);

        try {
            request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA,
                                 engineCommonData);

            // Prepare engine buttons helper
            JahiaEngineButtonsHelper jahiaEngineButtonsHelper =
                    new JahiaEngineButtonsHelper();
            jahiaEngineButtonsHelper.addOkButton();
            jahiaEngineButtonsHelper.addApplyButton();
            jahiaEngineButtonsHelper.addCancelButton();

            request.setAttribute(JahiaEngineButtonsHelper.JAHIA_ENGINE_BUTTONS_HELPER,
                                 jahiaEngineButtonsHelper);

            // Engine Url
            Properties params = new Properties();
            String engineURL = engineCommonData.getParamBean()
                                 .composeStrutsUrl("DatePicker",params,null);
            engineCommonData.setEngineURL(engineURL);
        } catch ( Exception e ){
            throw new JahiaException("Exception occured initializing engine's objects",
                                     "Exception occured initializing engine's objects",
                                     JahiaException.ENGINE_ERROR,
                                     JahiaException.ENGINE_ERROR, e);
        }
    }

    /**
     * Forward to errors if any or to continueForward
     *
     * @param mapping
     * @param request
     * @param errors
     * @param continueForward
     * @return
     */
    public ActionForward continueForward(ActionMapping mapping,
            HttpServletRequest request, ActionMessages errors,
            ActionForward continueForward){

        if(errors != null && !errors.isEmpty()){
            saveErrors(request,errors);
            return mapping.findForward("error");
        }
        return continueForward;
    }

}
