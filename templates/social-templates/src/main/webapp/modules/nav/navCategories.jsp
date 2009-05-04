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
<%@ include file="../../common/declarations.jspf" %>
<query:createFacetFilter facetName="defaultCategoryFacet"
    propertyName="defaultCategory" facetBeanId="defaultCategoryFacet" facetValueBeanId="categoryList"/>
<div class="categories">
    <h3><fmt:message key="article.categories"/></h3>
    
    <ul>
	<c:if test='${!query:isFacetApplied(defaultCategoryFacet, appliedFacets)}'>
    <query:getHitsPerFacetValue mainQueryBeanId="blogQuery" facetBeanId="defaultCategoryFacet" filterQueryParamName="filter" display="false"/>
</c:if>
    </ul>
</div>