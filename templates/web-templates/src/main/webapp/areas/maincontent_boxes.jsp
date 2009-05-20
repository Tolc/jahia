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

<%@ include file="../common/declarations.jspf" %>

<!--start content -->
<!-- Change id to modify positioning :  #position1=areaB/mainArea/areaA  #position2=areaB/mainArea #position3=mainArea/areaA   position4=mainArea only #position5=50%areaB/50% mainArea -->

        <template:include page="common/breadcrumb.jsp"/>
        <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}" escapeXml="false"/></h2>
        <template:include page="modules/maincontent/maincontentDisplay.jsp"/>
        <div class="clear"> </div>
        <template:include page="common/box/box.jsp">
            <template:param name="name" value="mainColumn_box"/>
        </template:include>
    
<!--stopContent-->