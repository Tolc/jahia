<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
<template:addResources type="javascript" resources="jquery.min.js"/>

<template:addResources type="css" resources="jquery.autocomplete.css" />
<template:addResources type="css" resources="thickbox.css" />
<template:addResources type="javascript" resources="jquery.ajaxQueue.js" />
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />

<script type="text/javascript">
    function addNewTag(tagForm, uuid, separator) {
        var newTag = tagForm.elements['j:newTag'];
        if (newTag.value.length > 0) {
            var tagContainer = jQuery('#jahia-tags-' + uuid);
            if (tagContainer.find("span:contains('" + newTag.value + "')").length == 0) {
	            jQuery.post(tagForm.action, jQuery(tagForm).serialize(), function (data) {
	                if (separator.length > 0 && jQuery('#jahia-tags-' + uuid + ' > span').length > 0) {
	                    tagContainer.append(separator);
	                }
	                var tagDisplay = jQuery('<span>' + newTag.value + '</span>');
	                tagDisplay.hide();
	                tagContainer.append(tagDisplay);
	                tagDisplay.fadeIn('fast');
	                newTag.value = '';
	            });
            }
        }
    }

    $(document).ready(function() {

        function getText(node) {
            return node["j:nodename"];
        }

        function format(result) {
            return getText(result["node"]);
        }

        $(".newTagInput").autocomplete("${url.find}", {
            dataType: "json",
            cacheLength: 1,
            parse: function parse(data) {
                return $.map(data, function(row) {
				    return {
					    data: row,
					    value: getText(row["node"]),
					    result: getText(row["node"])
				    }
			    });
            },
            formatItem: function(item) {
			    return format(item);
		    },
            extraParams: {
                query : "/jcr:root${renderContext.site.path}/tags//element(*, jnt:tag)[jcr:contains(.,'{$q}*')]/@j:nodename",
                language : "xpath",
                escapeColon : "false",
                propertyMatchRegexp : "{$q}.*",
                removeDuplicatePropValues : "false"
            }
        });
    });
    
</script>
<c:if test="${renderContext.user.name != 'guest'}">
    <form action="${url.base}${currentNode.path}" method="post">
        <label>Add tags:</label>
        <input type="hidden" name="methodToCall" value="put"/>
        <input type="text" name="j:newTag" class="newTagInput" value=""/>
        <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
               onclick="addNewTag(this.form, '${currentNode.identifier}', '${separator}'); return false;"/>
    </form>
</c:if>