<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:include templateType="html" template="hidden.header"/>
<c:set var="animatedTasks" value=""/>
<c:set var="animatedWFs" value=""/>

        <c:set var="inSite" value="true"/>
        <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
            <%@include file="edit.jspf" %>
            <%@include file="workflow.jspf" %>
            <div id="edit-${child.identifier}">
                <template:module templateType="html" node="${child}"/>
            </div>
            <hr/>
        </c:forEach>
        <div class="clear"></div>
        <c:if test="${moduleMap.editable and renderContext.editMode}">
            <template:module path="*"/>
        </c:if>
        <template:include templateType="html" template="hidden.footer"/>


<c:if test="${not renderContext.ajaxRequest}">
    <%-- include add nodes forms --%>
    <c:choose>
        <c:when test="${empty restrictions}">
            <jcr:nodeProperty node="${currentNode}" name="j:contributeTypes" var="types"/>
        </c:when>
        <c:otherwise>
            <c:set var="type" value="${restrictions}"/>
        </c:otherwise>
    </c:choose>
    <h3 class="titleaddnewcontent">
        <img title="" alt="" src="${url.currentModule}/images/add.png"/><fmt:message key="label.add.new.content"/>
    </h3>
    <script language="JavaScript">
        <c:forEach items="${types}" var="type" varStatus="status">
        animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}', 'fade=1,speed=700,group=newContent');
        </c:forEach>
        animatedcollapse.init();
    </script>
    <c:if test="${types != null}">
        <div class="listEditToolbar">
            <c:forEach items="${types}" var="type" varStatus="status">
                <jcr:nodeType name="${type.string}" var="nodeType"/>
                <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');"><span
                        class="icon-contribute icon-add"></span>${jcr:label(nodeType, renderContext.mainResourceLocale)}
                </button>
            </c:forEach>
        </div>

        <c:forEach items="${types}" var="type" varStatus="status">
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}">
                <template:module node="${currentNode}" templateType="edit" template="add">
                    <template:param name="resourceNodeType" value="${type.string}"/>
                    <template:param name="currentListURL" value="${url.current}.ajax"/>
                </template:module>
            </div>
        </c:forEach>

    </c:if>
</c:if>
