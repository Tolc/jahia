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
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page language = "java"%>
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.params.*"%>
<%@page import = "org.jahia.views.engines.*"%>

<%@include file="/views/engines/common/taglibs.jsp" %>

<script language="javascript">

// With Netscape 4.7, the engine popup window resizing cause a cache error
// expiration due to the document POSTing. This function prevent this error by
// automatically reloading the content.
// This function can be removed when Jahia will no more support Netscape 4.7.

window.onresize = function() {
    var browser = navigator.appName + navigator.appVersion;
    if (browser.indexOf("Netscape4.7") != -1) {
        window.location.reload();
    }
}
</script>
<br>
<table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
<form name="selector" action="">
<tr>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
    <td width="100%" align="right" valign="bottom" class="text">
    	<logic:iterate id="button" name="jahiaEngineButtonsHelper" property="buttons" type="java.lang.String">
	        <logic:equal name="button" value="OK_BUTTON">
                <span class="dex-PushButton">
                    <span class="first-child">
                        <a class="ico-ok" href="javascript:sendFormSave();"><fmt:message key="org.jahia.altApplyAndClose.label"/></a>
                    </span>
                </span>
			</logic:equal>
			<logic:equal name="button" value="SAVE_ADD_NEW_BUTTON">
                <span class="dex-PushButton">
                    <span class="first-child">
                        <a class="ico-add" href="javascript:sendFormSaveAndAddNew();"><fmt:message key="org.jahia.altApplyAndAddContainer.label"/></a>
                    </span>
                </span>
			</logic:equal>
			<logic:equal name="button" value="APPLY_BUTTON">
                <span class="dex-PushButton">
                    <span class="first-child">
                        <a class="ico-apply" href="javascript:sendFormApply();"><fmt:message key="org.jahia.altApplyWithoutClose.label"/></a>
                    </span>
                </span>
	        </logic:equal>
	        <logic:equal name="button" value="CANCEL_BUTTON">
                <span class="dex-PushButton">
                    <span class="first-child">
                        <a class="ico-cancel" href="javascript:sendFormCancel();"><fmt:message key="org.jahia.altCloseWithoutSave.label"/></a>
                    </span>
                </span>
	        </logic:equal>
	        <logic:equal name="button" value="CLOSE_BUTTON">
                <span class="dex-PushButton">
                    <span class="first-child">
                        <a class="ico-cancel" href="javascript:sendFormClose();"><fmt:message key="org.jahia.altClose.label"/></a>
                    </span>
                </span>
	        </logic:equal>
	    </logic:iterate>
    </td>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
</tr>
<!-- tab buttons -->
<logic:present name="tab-buttons">
<tr>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
    <td width="100%" class="text" valign="bottom">
	    	<tiles:insert beanName="tab-buttons"/>
    </td>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image" />" width="5" height="24"></td>
</tr>
</logic:present>
</form>
</table>