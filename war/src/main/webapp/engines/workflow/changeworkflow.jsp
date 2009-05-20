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
<%@ page import="org.jahia.content.ContentObject,
                 org.jahia.data.viewhelper.principal.PrincipalViewHelper,
                 org.jahia.engines.EngineLanguageHelper,
                 org.jahia.engines.JahiaEngine,
                 org.jahia.services.workflow.WorkflowRole,
                 org.jahia.services.workflow.WorkflowService" %>
<%@ page import="java.security.Principal" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<internal:gwtImport module="org.jahia.ajax.gwt.module.usergroup.UserGroupSelect" />


<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final ContentObject contentObject = (ContentObject) engineMap.get("contentObject");

    final Integer userNameWidth = new Integer(15);
    request.getSession().setAttribute("userNameWidth", userNameWidth);

    final Boolean hasParent = (Boolean) engineMap.get("hasParent");
    Integer workflowMode = (Integer) engineMap.get("workflowMode");
    if (workflowMode == null) {
        workflowMode = (Integer) engineMap.get(("defaultMode"));
    }
    pageContext.setAttribute("workflowMode", workflowMode);

    final Map workflowNames = (Map) engineMap.get("workflowNames");
    final String workflowName = (String) engineMap.get("workflowName");
    pageContext.setAttribute("workflowName", workflowName);
    final Map processes = (Map) engineMap.get("processes");
    pageContext.setAttribute("processes", processes);
    final String process = (String) engineMap.get("process");
    pageContext.setAttribute("process", process);
    final String selectUsrGrp = (String) engineMap.get("selectUsrGrp");
    final Map rolesMapping = (Map) engineMap.get("roleMapping");
    final String theURL = jParams.settings().getJahiaEnginesHttpPath();
    final String theScreen = (String) engineMap.get("screen");
    Boolean aBoolean = (Boolean) engineMap.get("locksActive");
    final boolean hasLocksActivated;
    if (aBoolean != null)
        hasLocksActivated = aBoolean.booleanValue();
    else
        hasLocksActivated = true;
    int roleNb; // Store the number of application roles
    final Integer inheritedMode = (Integer) engineMap.get("inheritedMode");

%>
<utility:setBundle basename="JahiaInternalResources"/>
<!-- Begin changeworkflow.jsp -->
<script type="text/javascript" src="<%= theURL%>../javascript/selectbox.js"></script>
<script type="text/javascript">

    function sendFormUpdate() {
        if (check()) {
            document.mainForm.screen.value = "workflow";
            saveContent();
            teleportCaptainFlam(document.mainForm);
        }
    }

    var formular = document.mainForm;

    var vKey = <%= userNameWidth.intValue() + 12 %>;

    var usrgrpname = new Array();
    var index = 0;

    var selectBoxName = null;

    function addOptions(text, value) {
        if (formular.elements[selectBoxName].options[0].value == "null") {
            formular.elements[selectBoxName].options[0] = null;
        }
        var i = formular.elements[selectBoxName].length;
        var pasteValue = value;
        for (j = 0; j < i; j++) {
            var entity = formular.elements[selectBoxName].options[j].value;
            if (pasteValue == entity) {
                usrgrpname[index++] = entity;
                return;
            }
        }
//        text = value.substr(vKey, 1) + text;
        formular.elements[selectBoxName].options[i] = new Option(text, pasteValue);
        formular.elements[selectBoxName].disabled = false;
    }

    function addOptionsBalance() {
        if (index > 0) {
            var badName = "\n";
            for (i = 0; i < index; i++) {
                badName += "- ";
                if (usrgrpname[i].substr(0, 1) == "u") {
                    badName += "User name : ";
                } else {
                    badName += "Group name : ";
                }
                badName += usrgrpname[i].substr(1, usrgrpname[i].indexOf(":") - 1) + "\n";
            }
            alert("These users/groups are already defined in role" + badName);
            index = 0;
        }
    }

    function puselectUsrGrp(url, _selectBoxName) {
        selectBoxName = _selectBoxName;
        openUserGroupSelect('tabs',_selectBoxName, "Principal|Provider, 6|SiteTitle, 20|Name, <%=userNameWidth%>|Properties, 20");
    }

    function populateWorkflowOptions(wfTypeCombo) {
        var wfMode = wfTypeCombo.options[wfTypeCombo.selectedIndex].value;
        if (wfMode.indexOf('$$$') != -1) {
            var wfData = wfMode.split('$$$');
            wfMode = wfData[0];
            document.getElementById('workflowName').value = wfData[1];
            document.getElementById('process').value = wfData[2];

        }
        document.getElementById('workflowMode').value = wfMode;
    }
</script>
<div class="dex-TabPanelBottom">
<div class="tabContent">
<%@ include file="../tools.inc" %>
<div id="content" class="fit w2">
<div class="head">
    <div class="object-title"><fmt:message key="org.jahia.engines.include.actionSelector.Workflow.label"/></div>
</div>

<% if (hasLocksActivated) {%>
<table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
    <tr>
        <td><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.error.locks.activated.label"/></td>
    </tr>
</table>
<% } else { %>
<table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
    <tr>
        <th><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.wfmode.label"/></th>
        <td>
            <input type="hidden" name="workflowName" id="workflowName" value="${workflowName}"/>
            <input type="hidden" name="process" id="process" value="${process}"/>
            <input type="hidden" name="workflowMode" id="workflowMode" value="${workflowMode}"/>
            <select name="workflowModeCombo" onchange="javascript:{populateWorkflowOptions(this); sendFormUpdate();}">
                <% if (hasParent.booleanValue()) { %>
                <option value="3" ${workflowMode == 3 ? 'selected="selected"' : ''}><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.inherited.label"/></option>
                <option value="4" ${workflowMode == 4 ? 'selected="selected"' : ''}><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.linked.label"/></option>
                <% } %>
                <option value="0" ${workflowMode == 0 ? 'selected="selected"' : ''}><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.noworkflow.label"/></option>
                <c:if test="${empty processes}">
                    <option value="1" ${workflowMode == 1 ? 'selected="selected"' : ''}><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.standard.label"/></option>
                </c:if>
                <c:if test="${not empty processes}">
                    <c:forEach items="${processes}" var="wfProcess">
                        <c:forEach items="${wfProcess.value}" var="currentProcess">
                            <option value="2$$$${wfProcess.key}$$$${currentProcess.key}" ${workflowMode == 2 && workflowName == wfProcess.key && process == currentProcess.key ? 'selected="selected"' : ''}>${currentProcess.value}</option>
                        </c:forEach>
                    </c:forEach>
                </c:if>
            </select>
        </td>
    </tr>
    <% if (workflowMode.intValue() == WorkflowService.LINKED || workflowMode.intValue() == WorkflowService.INHERITED) { %>
    <tr>
        <% if (workflowMode.intValue() == WorkflowService.LINKED) { %>
        <th>
            <fmt:message key="org.jahia.engines.workflow.WorkflowEngine.linkedto.label"/>
        </th>
        <td>
            <%= engineMap.get("linked") %>
            <% } else { %>
            <th>
                <fmt:message key="org.jahia.engines.workflow.WorkflowEngine.inheritfrom.label"/>:
            </th>
        <td>
            <%= engineMap.get("inheritingParent") %>
            <% } %>
            <%
                switch (inheritedMode.intValue()) {
                    case WorkflowService.INACTIVE:
            %>
            (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.noworkflow.label"/>)
            <%
                    break;
                case WorkflowService.JAHIA_INTERNAL:
            %>
            (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.standard.label"/>)
            <%
                    break;
                case WorkflowService.EXTERNAL:
            %>
        (<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.external.label"/>
         - <%= engineMap.get("inheritedProcess") %>)
            <%
                        break;
                }
            %>
        </td>
    </tr>
<% } %>
</table>
<%
    if (workflowMode.intValue() == WorkflowService.EXTERNAL || (inheritedMode != null && inheritedMode == WorkflowService.EXTERNAL)) { %>
<%
    final List roles = (List) engineMap.get("roles");
    roleNb = roles.size();
    if (roleNb > 0) { %>
<div class="head">
    <div class="object-title"><fmt:message key="org.jahia.engines.workflow.definesRoles.label"/></div>
</div>

<%
    final List<Set> roleMembersList = (List<Set>) engineMap.get("workflowRoles");
    final List<Set> roleInheritedMembersList = (List<Set>) engineMap.get("inheritedWorkflowRoles");
    for (int i = 1; i < roles.size(); i++) {

        Set membersSet = roleMembersList.get(i);
        Set inheritedMemberSet = roleInheritedMembersList.get(i);

        final String[] textPattern = {"Inheritance, 2", "Principal", "Provider, 6", "Name, " + userNameWidth, "Properties, 20"};
        final PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
        principalViewHelper.setInheritance(inheritedMemberSet);
%>
<div class="fill padded">
    <table cellpadding="5" cellspacing="0" border="0" width="98%">
        <tr>
            <td width="44%">
                <%=rolesMapping.get(roles.get(i))%><br/>
                <select class="fontfix" name="authMembers<%=i%>" size="5" multiple="multiple"
                        style="width: 100%" <%if (membersSet.size() == 0) {%> disabled="disabled" <%}%>>
                    <%
                        Iterator it = inheritedMemberSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option disabled="disabled" value=""><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        it = membersSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        if (inheritedMemberSet.size() + membersSet.size() == 0) {
                    %>
                    <option value="null">-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----
                    </option>
                    <%}%>
                </select>
            </td>
            <td width="5%">
                <a href="javascript:puselectUsrGrp('<%=selectUsrGrp%>', 'authMembers<%=i%>');"
                   title='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'>
                    <img src="${pageContext.request.contextPath}/engines/images/icons/user1_add.gif"
                         alt='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'
                         title='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'
                         border="0"/></a>
                <br/>
                <a href="javascript:removeSelectBox(document.mainForm.authMembers<%=i%>, '-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----');"
                   title='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'>
                    <img src="${pageContext.request.contextPath}/engines/images/icons/user1_delete.gif"
                         alt='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'
                         title='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'
                         border="0"/></a>
            </td>
            <% if (i > 0 && i < roles.size() - 1 && ((String) roles.get(i + 1)).endsWith("_unlock")) {
                i++;
                membersSet = roleMembersList.get(i);
                inheritedMemberSet = roleInheritedMembersList.get(i);
            %>
            <td width="44%">
                <%=rolesMapping.get(roles.get(i))%><br/>
                <select class="fontfix" name="authMembers<%=i%>" size="5" multiple="multiple"
                        style="width: 100%" <%if (membersSet.size() == 0) {%> disabled="disabled" <%}%>>
                    <%
                        it = inheritedMemberSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option disabled="disabled" value=""><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        it = membersSet.iterator();
                        while (it.hasNext()) {
                            final Principal p = (Principal) it.next();
                    %>
                    <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p)%>
                    </option>
                    <%
                        }
                        if (inheritedMemberSet.size() + membersSet.size() == 0) {
                    %>
                    <option value="null">-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----
                    </option>
                    <%}%>
                </select>
            </td>
            <td width="5%">
                <a href="javascript:puselectUsrGrp('<%=selectUsrGrp%>', 'authMembers<%=i%>');"
                   title='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'>
                    <img src="${pageContext.request.contextPath}/engines/images/icons/user1_add.gif"
                         alt='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'
                         title='<fmt:message key="org.jahia.engines.shared.Application_Field.setUsersAndGroups.label"/>'
                         border="0"/></a>
                <br/>
                <a href="javascript:removeSelectBox(document.mainForm.authMembers<%=i%>, '-----&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.noMembers.label"/>&nbsp;-----');"
                   title='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'>
                    <img src="${pageContext.request.contextPath}/engines/images/icons/user1_delete.gif"
                         alt='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'
                         title='<fmt:message key="org.jahia.engines.shared.Application_Field.removeUsersOrGroups.label"/>'
                         border="0"/></a>
            </td>
            <% } else { %>
            <td colspan="2" width="49%">&nbsp;</td>
            <% } %>
        </tr>
    </table>
</div>
<%}%>
<%} else { %>
<fmt:message key="org.jahia.engines.shared.Application_Field.notDefineRoles.label"/>
<% } %>
<script type="text/javascript">
    <!--
    function check() {
    <% for (int i = 1; i < roleNb; i++) { %>
        selectAllOptionsSelectBox(document.mainForm.authMembers<%=i%>);
        if (document.mainForm.authMembers<%=i%>unlock) {
            selectAllOptionsSelectBox(document.mainForm.authMembers<%=i%>unlock);
        }
    <% } %>
        return true;
    }

    function saveContent() {
        check();
        if (typeof workInProgressOverlay != 'undefined') workInProgressOverlay.launch();
    }
    // -->
</script>
<% } else { %>
<% } %>
<% } %>
</div>
</div>
</div>
<!-- End changeworkflow.jsp -->