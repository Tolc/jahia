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
<%@ tag body-content="empty" description="Renders the link to the folder path selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted path value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include subfolders checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include subfolders input field." %>
<%@ attribute name="useUrl" required="false" type="java.lang.Boolean"
              description="If set to true the selected folder URL will be used in the field value; otherwise the path of the selected folder will be used (default)." %>
<%@ attribute name="rootPath" required="false" type="java.lang.String"
              description="The path to start with. So the selection will be available for subfolders of the specified root directory" %>
<%@ attribute name="startPath" required="false" type="java.lang.String"
              description="The path of the directory that will be expanded by default" %>
<%@ attribute name="filters" required="false" type="java.lang.String"
              description="Comma-separated list of filter patterns to be applied on the displayed resources" %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after a location is selected. The selected path will be passed as an argument to this function. If the function returns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="displayIncludeChildren" value="${not empty displayIncludeChildren ? displayIncludeChildren : 'true'}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<c:set var="fieldIdHash"><%= Math.abs(jspContext.getAttribute("fieldId").hashCode()) %>
</c:set>
<%-- by default set includeChildren to 'true' to search in subfolders --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:set var="useUrl" value="${not empty useUrl ? useUrl : 'false'}"/>
<script type="text/javascript">
    function openFolderSelector(){
        return window.open('${pageContext.request.contextPath}/engines/webdav/filePicker.jsp?foldersOnly=true&amp;callback=setSelectedFilePath${fieldIdHash}&amp;rootPath=${rootPath}&amp;startPath=${startPath}&amp;filters=${filters}', '<%="pathSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable,height=510,width=700');
    }
</script>
&nbsp;<a href="#select"
onclick="{var pathSelector = openFolderSelector(); pathSelector.focus(); return false;}"
title='<fmt:message key="selectors.folderSelector.selectFolder"/>'><fmt:message key="selectors.select"/></a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><fmt:message key="selectors.folderSelector.selectFolder.includeChildren"/></label>
</c:if>
<script type="text/javascript">
    function setSelectedFilePath${fieldIdHash}(path, url) {
    <c:if test="${not empty onSelect}">
        if ((${onSelect})(path, url)) {
            document.getElementById('${fieldId}').value =
        ${useUrl} ?
            url : path;
        }
    </c:if>
    <c:if test="${empty onSelect}">
        document.getElementById('${fieldId}').value =
    ${useUrl} ?
        url : path;
    </c:if>
    }
</script>