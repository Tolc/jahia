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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ tag body-content="empty" description="Renders document type selection control with all node types available." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>

<c:set var="value" value="${h:default(param.src_documentType, value)}"/>
<c:set var="display" value="${h:default(display, true)}"/>
<c:if test="${display}">
    <select ${h:attributes(attributes)} name="src_documentType">
        <option value=""><fmt:message key="searchForm.any"/></option>
        <jcr:nodeType ntname="${jcr.nt_file}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeType ntname="${jcr.nt_folder}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeTypes baseType="${jcr.jahiamix_extension}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeTypes>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_documentType" value="${value}"/></c:if>