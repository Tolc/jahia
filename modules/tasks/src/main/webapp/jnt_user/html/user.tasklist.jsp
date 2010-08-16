<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
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
<%--@elvariable id="task" type="org.jahia.services.workflow.WorkflowTask"--%>
<template:addResources type="css" resources="tasks.css"/>
<template:addResources type="css" resources="contentlist.css"/>

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>

<form name="myform" method="post">
    <input type="hidden" name="nodeType" value="jnt:task">
    <input type="hidden" name="redirectTo" value="${url.base}${currentNode.path}.tasklist">
    <input type="hidden" name="state">
</form>


<script type="text/javascript">
    function send(task, state) {
        form = document.forms['myform'];
        form.action = '${url.base}' + task;
        form.elements.state.value = state;
        form.submit();
    }
</script>
<div class="boxtasks">
<div class=" boxtasksgrey boxtaskspadding16 boxtasksmarginbottom16">
<div class="boxtasks-inner">
<div class="boxtasks-inner-border"><!--start boxtasks -->
<div id="${currentNode.UUID}">


<table width="100%" class="table tableTasks" summary="Tasks">
<colgroup>
    <col span="1" width="10%" class="col1"/>
    <col span="1" width="50%" class="col2"/>
    <col span="1" width="10%" class="col3"/>
    <col span="1" width="25%" class="col4"/>
    <col span="1" width="15%" class="col5"/>
</colgroup>
<thead>
<tr>
    <th class="center" id="Type" scope="col"><fmt:message key="jnt_task.type"/> <a href="#"
                                                                                   title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/> <a href="#"
                                                                           title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png"
            alt="up"/></a><a
            title="sort down" href="#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png"
            alt="down"/></a></th>
    <th class="center" id="State" scope="col"><fmt:message key="jnt_task.state"/> <a href="#"
                                                                                     title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th class="center" id="Priority" scope="col"><fmt:message key="jnt_task.priority"/> <a
            href="#" title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a
            title="sort down"
            href="#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
    <th id="Date" scope="col"><fmt:message key="jnt_task.dueDate"/> <a href="#" title="sort up"><img
            src="${url.currentModule}/images/sort-arrow-up.png"
            alt="up"/></a><a
            title="sort down" href="#"> <img
            src="${url.currentModule}/images/sort-arrow-down.png"
            alt="down"/></a></th>
</tr>
</thead>

<tbody>

<jcr:sql var="tasks"
         sql="select * from [jnt:task] as task where task.assignee='${currentNode.identifier}'"/>
<%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>
<template:initPager pageSize="25" totalSize="${tasks.nodes.size}"
                    id="${currentNode.identifier}"/>

<c:forEach items="${tasks.nodes}" var="task"
           begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
    <c:choose>
        <c:when test="${status.count % 2 == 0}">
            <tr class="odd">
        </c:when>
        <c:otherwise>
            <tr class="even">
        </c:otherwise>
    </c:choose>
    <td class="center" headers="Type"><img alt=""
                                           src="${url.currentModule}/images/flag_16.png"/>
    </td>
    <td headers="Title"><a
            href="${url.base}${task.path}.html">${fn:escapeXml(task.propertiesAsString['jcr:title'])}</a></td>
    <td class="center" headers="Priority">
            ${task.propertiesAsString.priority}
    </td>
    <td class="center" headers="State">
        <c:choose>
            <c:when test="${task.properties.state.string == 'active'}">
                <span><img alt="" src="${url.currentModule}/images/right_16.png"/></span>
                        <span>
                            <a href="javascript:send('${task.path}','suspended')"><fmt:message
                                    key="jnt_task.suspended"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','cancelled')"><fmt:message
                                    key="jnt_task.cancel"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','finished')"><fmt:message
                                    key="jnt_task.complete"/></a>
                        </span>
            </c:when>
            <c:when test="${task.properties.state.string == 'finished'}">
                <img alt="" src="${url.currentModule}/images/tick_16.png"/>
            </c:when>
            <c:when test="${task.properties.state.string == 'suspended'}">
                <span><img alt="" src="${url.currentModule}/images/bubble_16.png"/></span>
                        <span>
                            <a href="javascript:send('${task.path}','cancelled')"><fmt:message
                                    key="jnt_task.cancel"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','active')"><fmt:message
                                    key="jnt_task.continue"/></a>
                        </span>
            </c:when>
            <c:when test="${task.properties.state.string == 'canceled'}">
                <img alt="" src="${url.currentModule}/images/warning_16.png"/>
            </c:when>
        </c:choose>
    </td>
    <td headers="Date"><fmt:formatDate value="${task.properties['dueDate'].date.time}"
                                       dateStyle="short" type="date"/></td>
    </tr>
</c:forEach>
<workflow:tasksForNode var="wfTasks" user="${renderContext.user}"/>
<c:forEach items="${wfTasks}" var="task" varStatus="status">
    <jcr:node var="node" uuid="${task.variables.nodeId}"/>
    <c:choose>
        <c:when test="${((status.count + 1)) % 2 == 0}">
            <tr class="odd">
        </c:when>
        <c:otherwise>
            <tr class="even">
        </c:otherwise>
    </c:choose>
    <td class="center" headers="Type">
        <img alt="" src="${url.currentModule}/images/workflow.png"/>
    </td>
    <td headers="Title">
        <a target="_blank"
           href="${url.context}/cms/render/${task.variables.workspace}/${task.variables.locale}${node.path}.html">
            <c:choose>
                <c:when test="${not empty task.formResourceName and not empty task.variables['jcr:title']}">
                    ${task.variables['jcr:title'][0].value}
                </c:when>
                <c:otherwise>
                    ${task.name}
                </c:otherwise>
            </c:choose>
        </a>
    </td>
    <td colspan="3">
        <div class="listEditToolbar">
            <c:choose>
                <c:when test="${not empty task.formResourceName}">
                    <template:addResources type="inlineJavascript">
                        animatedcollapse.addDiv('task${node.identifier}-${task.id}', 'fade=1,speed=100');
                    </template:addResources>
                    <input class="workflowaction" type="button" value="${task.name}"
                           onclick="animatedcollapse.toggle('task${node.identifier}-${task.id}');$('#taskrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${task.outcomes}" var="outcome">
                        <input class="workflowaction" type="button" value="${outcome}"
                               onclick="executeTask('${node.path}', '${task.provider}:${task.id}', '${outcome}', '${url.base}', '${currentNode.UUID}', '${url.current}','window.location=window.location;')"/>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            <template:addResources type="inlineJavascript">
                animatedcollapse.addDiv('comments${node.identifier}-${task.id}', 'fade=1,speed=100');
            </template:addResources>
            <input class="workflowaction" type="button" value="<fmt:message key="jnt_task.comments"/>"
                   onclick="animatedcollapse.toggle('comments${node.identifier}-${task.id}');$('#commentsrow${node.identifier}-${task.id}').toggleClass('hidden');"/>
        </div>
    </td>
    </tr>
    <c:if test="${not empty task.formResourceName}">
        <c:choose>
            <c:when test="${((status.count + 1)) % 2 == 0}">
                <tr class="odd hidden" id="taskrow${node.identifier}-${task.id}">
            </c:when>
            <c:otherwise>
                <tr class="even hidden" id="taskrow${node.identifier}-${task.id}">
            </c:otherwise>
        </c:choose>
        <td colspan="5">
            <div style="display:none;" id="task${node.identifier}-${task.id}" class="taskformdiv">
                <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                <template:module node="${node}" templateType="edit" template="add">
                    <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                    <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                    <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                    <template:param name="workflowTaskFormCallbackId" value="${currentNode.UUID}"/>
                    <template:param name="workflowTaskFormCallbackURL" value="${url.current}"/>
                    <template:param name="workflowTaskFormCallbackJS"
                                    value="$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=100');});animatedcollapse.reinit();"/>
                </template:module>
            </div>
        </td>
        </tr>
    </c:if>
    <c:choose>
        <c:when test="${((status.count + 1)) % 2 == 0}">
            <tr class="odd hidden" id="commentsrow${node.identifier}-${task.id}">
        </c:when>
        <c:otherwise>
            <tr class="even hidden" id="commentsrow${node.identifier}-${task.id}">
        </c:otherwise>
    </c:choose>
    <td colspan="5" class="tdTaskComments">
        <div style="display:none;" id="comments${node.identifier}-${task.id}" class="taskformdiv">
            <c:forEach items="${task.taskComments}" var="taskComment">
                <p class="TasksComment">
                    <span>${taskComment.comment}</span>
                    <span class="TasksCommentDate">&nbsp;at&nbsp;<fmt:formatDate dateStyle="medium" type="both" value="${taskComment.time}"/></span>
                </p>
            </c:forEach>
            <form class="Form-tasksComments" action="${url.base}${currentNode.path}.commentTask.do" method="post" id="commentsForm${task.id}">
                <input type="hidden" name="task" value="${task.provider}:${task.id}"/>
                <textarea rows="10" cols="80" name="comment"></textarea>
                <div class="divButton">
                    <button type="submit"><span class="icon-contribute icon-accept"></span><fmt:message
                                                key="jnt_task.comments.add"/></button>
                </div>           
            </form>
            <script type="text/javascript">
                var options${task.id} = {
                    success: function() {
                        replace('${currentNode.identifier}', '${url.current}', "$('.taskformdiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=100');});animatedcollapse.reinit();$('#commentsForm${task.id}').ajaxForm(options${task.id});");

                        $.each(richTextEditors, function(key, value) {
                            value.setData("");
                        });
                    },
                    dataType: "json",
                    resetForm : true
                };// wait for the DOM to be loaded
                $(document).ready(function() {
                    // bind 'myForm' and provide a simple callback function
                    $('#commentsForm${task.id}').ajaxForm(options${task.id});
                });
            </script>
        </div>
    </td>
    </tr>
</c:forEach>
</tbody>
</table>

<template:addResources type="inlineJavascript">
    animatedcollapse.init();
</template:addResources>
<div class="pagination"><!--start pagination-->

    <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${tasks.nodes.size} results</span>
    </div>
    <div class="paginationNavigation">
        <c:if test="${currentPage>1}">
            <a class="previousLink"
               href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}')">Previous</a>
        </c:if>
        <c:forEach begin="1" end="${nbPages}" var="i">
            <c:if test="${i != currentPage}">
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}')"> ${ i }</a></span>
            </c:if>
            <c:if test="${i == currentPage}">
                <span class="currentPage">${ i }</span>
            </c:if>
        </c:forEach>

        <c:if test="${currentPage<nbPages}">
            <a class="nextLink"
               href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}')">Next</a>
        </c:if>
    </div>

    <div class="clear"></div>
</div>
<!--stop pagination-->
<template:removePager id="${currentNode.identifier}"/>
</div>
<div class="clear"></div>
</div>
</div>
</div>
</div>