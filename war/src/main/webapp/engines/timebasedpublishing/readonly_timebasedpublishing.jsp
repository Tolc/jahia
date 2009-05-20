<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.bin.Jahia,
                 org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine,
                 org.jahia.hibernate.manager.JahiaObjectDelegate,
                 org.jahia.services.timebasedpublishing.RetentionRuleDef,
                 java.util.*" %>
<%@ page import="org.jahia.services.timebasedpublishing.RetentionRule"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final Map subEngineMap = (Map) engineMap.get(TimeBasedPublishingEngine.SUB_ENGINE_MAP_NAME);
    final String theScreen = (String) engineMap.get("screen");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }

    final List ruleDefs = (List) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".ruleDefs");
    final RetentionRuleDef currentRuleDef = (RetentionRuleDef) subEngineMap.get(
            TimeBasedPublishingEngine.ENGINE_NAME + ".currentRuleDef");
    final JahiaObjectDelegate jahiaObjectDelegate = (JahiaObjectDelegate) subEngineMap.get(
            TimeBasedPublishingEngine.ENGINE_NAME + ".jahiaObjectDelegate");
    final String selectedRuleDef;
    if (currentRuleDef != null) {
        selectedRuleDef = String.valueOf(currentRuleDef.getId());
    } else {
        selectedRuleDef = "none";
    }

    final String labelResourceName = "org.jahia.engines.timebasedpublishing.timebpstatus." +
            jahiaObjectDelegate.getTimeBPState() + ".label";

    boolean inherited = false;
    final RetentionRule currentRule = (RetentionRule) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".currentRule");
    if (currentRule != null) {
        inherited = currentRule.getInherited().booleanValue();
    }
%>
<utility:setBundle basename="JahiaInternalResources"/>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
   <%@ include file="../menu.inc" %>
    <div id="content" class="fit w2">
          <div class="head">
             <div class="object-title"><fmt:message key="org.jahia.engines.timebasedpublishing.status.label"/></div>
          </div>
          <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
            <tr>
              <th width="120">
                <fmt:message key="org.jahia.engines.timebasedpublishing.currentstatus.label"/>
              </th>
              <td>
                <fmt:message key="<%=labelResourceName%>"/>
              </td>
            </tr>
            <tr>
              <th>
                <fmt:message key="org.jahia.engines.servertime.label"/>
              </th>
              <td>
                <span id="serverTime"/>
              </td>
            </tr>
          </table>

      <% if (currentRuleDef != null) { %>
        <jsp:include page="/engines/timebasedpublishing/readonly_rangerule.jsp" flush="true"/>
            <% } else { %>
            <fmt:message key="org.jahia.engines.timebasedpublishing.rule.none.label"/>
            <% } %>

    </div>

   <div class="clearing">&nbsp;</div>
</div>
</div>

<script type="text/javascript">
    var tmpDate = new Date();
    var timeOffSet = tmpDate.getTimezoneOffset();
    getServerTime('<%=Jahia.getContextPath()%>', 'serverTime', 'HH:mm:ss', timeOffSet * 60 * 1000 * -1);
    setInterval("getServerTime('<%=Jahia.getContextPath()%>','serverTime','HH:mm:ss',timeOffSet * 60 * 1000 * -1)", 5000);
</script>

