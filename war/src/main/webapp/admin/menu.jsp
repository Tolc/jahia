<%@include file="/admin/include/header.inc" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.settings.SettingsBean" %>
<%@page import="org.jahia.services.usermanager.JahiaUser" %>
<%

    Boolean isSuperAdmin = (Boolean) request.getAttribute("isSuperAdmin");
    if (isSuperAdmin == null) {
        isSuperAdmin = new Boolean(false);
    }
    Boolean configJahia = (Boolean) request.getAttribute("configJahia");
    List sitesList = (List) request.getAttribute("sitesList");

    String sub = (String) request.getParameter("sub");

    if (sitesList == null) {
        sitesList = new ArrayList();
    }
    Iterator sitesEnum = sitesList.iterator();
    Iterator sitesJavaScript = sitesList.iterator();

    if (sub != null && !"".equals(sub) && !(isSuperAdmin && "processEdit".equals(sub))) {
        if (sub.equals("server")) {
            stretcherToOpen = 0;
        } else {
            stretcherToOpen = 1;
        }

    } else {
        stretcherToOpen = configJahia != null && configJahia.booleanValue() ? 0 : 1;
    }
    
    pageContext.setAttribute("stretcherToOpen", Integer.valueOf(stretcherToOpen));

    ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaUser user = jData.getProcessingContext().getUser();
    final int currentSiteID = jData.getProcessingContext().getSiteID();
%>
<script type="text/javascript" src="<%=URL%>../javascript/moo/prototype.lite.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.pack.js"></script>


<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></h2>

</div>

<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
    <%@ include file="include/tab_menu.inc"%>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">

<div class="tabContent">
<div id="content" class="full">

<c:if test="${stretcherToOpen == 1 && fn:length(sitesList) > 1}">
<div id="changeSite">
    <% if (sitesList.size() > 1) { %>
    <script language="javascript">
        function changeSiteNow() {
        <%
           int countJS = 0;
           while(sitesJavaScript.hasNext()) {
               JahiaSite siteJS = (JahiaSite) sitesJavaScript.next();
               String siteChangeURL = JahiaAdministration.composeActionURL(request,response,"change","&changesite=" + siteJS.getID() + "#sites");
        %>
            if (document.jahiaAdmin.changesite.options[<%=countJS%>].selected) location.href = "<%=siteChangeURL%>";
        <%
               countJS++;
           }
        %>
        }
    </script>
    <form name="jahiaAdmin">
        <nobr><b><fmt:message key="label.selectWebsite"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b>
            <select name="changesite" onChange="changeSiteNow();">
                <% while (sitesEnum.hasNext()) {
                    JahiaSite site = (JahiaSite) sitesEnum.next(); %>
                <option value="<%=site.getID()%>" <%if (siteID.intValue() == site.getID()) {%> selected <%}%>>
                    &nbsp;<%=site.getTitle()%>&nbsp;</option>
                <% }%>
            </select></nobr>
    </form>
    <% } %>
</div>
</c:if>

<table border="0" cellspacing="0" cellpadding="10" class="adminmenu">
<c:forEach items="${stretcherToOpen == 0 ? administrationServerModules : administrationSiteModules}" var="item" varStatus="status">
    <c:if test="${status.index % 3 == 0}">
    <tr>
    </c:if>
        <td>
            <c:if test="${fn:contains(item.icon, '/') || fn:contains(item.icon, '.')}" var="externalIcon">
                <c:set var="iconUrl" value="${item.icon}"/>
                <c:set var="iconUrlDisabled" value="${item.icon}"/>
            </c:if>
            <c:if test="${!externalIcon}">
                <c:set var="iconUrl"><%=URL%>images/icons/admin/adromeda/${item.icon}.png</c:set>
                <c:set var="iconUrlDisabled"><%=URL%>images/icons/admin/adromeda/${item.icon}_grey.png</c:set>
            </c:if>
            <fmt:message key="${item.label}" var="label"/>
            <c:set var="label" value="${fn:contains(label, '???') ? item.label : label}"/>
            <c:if test="${item.enabled}">
                    <span class="dex-PushButton-big">
                        <span class="first-child">
                            
                            <a href="${item.link}" ${fn:indexOf(item.link, 'http://') == 0 || fn:indexOf(item.link, 'https://') == 0 ? 'target="_blank"' : ''}><img
                                    name="${item.name}" src="${iconUrl}" width="32"
                                    height="32" border="0"><span><c:out value="${label}"/></span></a>
                        </span>
                    </span>
            </c:if>
            <c:if test="${not item.enabled}">
                    <span class="dex-PushButton-big disabled">
                        <span class="first-child" style="cursor: default">
                            <a href="#${item.name}" onclick="return false;" style="cursor: default;"><img name="${item.name}" src="${iconUrlDisabled}" width="32"
                                             height="32" border="0"><span><c:out value="${label}"/></span></a>
                        </span>
                    </span>
            </c:if>
        </td>
        <c:if test="${status.last}">
            <c:forEach begin="1" end="${2 - status.index % 3}">
            <td>&nbsp;</td>
            </c:forEach>
        </c:if>
    <c:if test="${status.last || (status.index + 1) % 3 == 0}">
    </tr>
    </c:if>
</c:forEach>
</table>
</div>
</div>

</td>
</tr>
</tbody>
</table>
</div>

<%@include file="/admin/include/footer.inc" %>
