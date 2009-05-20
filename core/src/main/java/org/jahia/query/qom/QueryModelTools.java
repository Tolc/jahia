/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.qom;

import org.apache.lucene.search.SortField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.filtercreator.FilterCreator;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.lucene.JahiaLuceneSort;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.JahiaTools;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import java.util.*;

/**
 * Query Object Model Tools class
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 14:39:57
 * To change this template use File | Settings | File Templates.
 */
public class QueryModelTools {
    public static final int NO_TYPE = 0;
    public static final int SORTING_TYPE = 1;
    public static final int FACETING_TYPE = 2;

    public static void appendPropertyValue(Properties properties, String propertyName,
                                            String value){
        if ( properties == null || !isNotEmptyStringOrNull(value) ||
                !isNotEmptyStringOrNull(propertyName)){
            return;
        }
        String propValue = properties.getProperty(propertyName);
        if (propValue==null||propValue.trim().equals("")){
            properties.setProperty(propertyName,value);
        } else{
            propValue += "," + value;
        }
    }

    public static boolean isNotEmptyStringOrNull(String value){
        return (value != null && !"".equals(value.trim()));
    }

    public static JahiaLuceneSort getSorter(OrderingImpl[] orderings, ProcessingContext context, ContainerQueryContext queryContext)
    throws JahiaException {
        JahiaLuceneSort sorter = null;
        Locale locale = null;
        List<SortField> sortFieldsList = new ArrayList<SortField>();
        for (OrderingImpl ordering : orderings){
            PropertyValueImpl operand = (PropertyValueImpl)ordering.getOperand();
            String propertyName = operand.getPropertyName();
            if ( JahiaQueryObjectModelConstants.PUBLICATION_DATE.equals(propertyName) ){
                //@todo complete
                return null;
            } else if ( JahiaQueryObjectModelConstants.EXPIRATION_DATE.equals(propertyName) ){
                //@todo complete
                return null;
            }
            if (operand.getValueProviderClass() != null){
                return null;
            }
            propertyName = QueryModelTools
                    .getFieldNameForSearchEngine(propertyName, operand.isMetadata(),
                            queryContext.getContainerDefinitionNames(), context, QueryModelTools.SORTING_TYPE);
            if (propertyName == null) {
                return null;
            }

            if (ordering.isLocaleSensitive() && locale == null) {
                locale = context.getEntryLoadRequest().getFirstLocale(true);
            }
            
            SortField sortField = new SortField(
                    propertyName,
                    ordering.isLocaleSensitive() ? locale : null,
                    (ordering.getOrder() == JahiaQueryObjectModelConstants.ORDER_DESCENDING));
            sortFieldsList.add(sortField);
        }
        if (sortFieldsList.isEmpty()){
            return null;
        } else {
            SortField[] sortFields = new SortField[]{};
            sortFields = (SortField[])sortFieldsList.toArray(sortFields);
            sorter = new JahiaLuceneSort(sortFields);
        }
        return sorter;
    }

    public static String[] getSortPropertyNames(Ordering[] orderings) {
        String[] propertyNames = new String[]{};
        if (orderings != null){
            List<String> propertyNamesList = new ArrayList<String>();
            for (int i=0; i<orderings.length; i++){
                PropertyValueImpl prop = (PropertyValueImpl)orderings[i].getOperand();
                propertyNamesList.add(prop.getPropertyName());
            }
            propertyNames = (String[])propertyNamesList.toArray(propertyNames);
        }
        return propertyNames;
    }

    public static List<String> getLanguageCodes(Properties properties){
        if ( properties == null ){
            return new ArrayList<String>();
        }
        List<String> result = JahiaTools.getTokensList(
                properties.getProperty(JahiaQueryObjectModelConstants.LANGUAGE_CODES)," *+, *+");
        if (result ==null){
            result = new ArrayList<String>();
        }
        return result;
    }

    public static JahiaFieldDefinition getFieldDefinitionForPropertyName(
            String propertyName, List<String> containerDefinitionNames,
            ProcessingContext jParams) throws JahiaException {
        // if (def.getDeclaringNodeType().isMixin() && (def.getDeclaringNodeType().isNodeType("jmix:contentmetadata") ||
        // def.getDeclaringNodeType().isNodeType("mix:created") ||
        // def.getDeclaringNodeType().isNodeType("mix:createdBy") || def.getDeclaringNodeType().isNodeType("jmix:lastPublished") ||
        // def.getDeclaringNodeType().isNodeType("jmix:categorized") || def.getDeclaringNodeType().isNodeType("mix:lastModified"))) {
        // new metadata
        // JahiaFieldDefinition contentDefinition = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(0,
        // StringUtils.substringAfter(def.getName(),":"));

        JahiaFieldDefinition fieldDef = JahiaFieldDefinitionsRegistry
                .getInstance().getDefinition(jParams.getSiteID(), propertyName);

        if (fieldDef == null) {
            for (String containerDefName : containerDefinitionNames) {
                fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                        .getDefinition(jParams.getSiteID(),
                                containerDefName + "_" + propertyName);
                if (fieldDef != null)
                    break;
            }
        }
        if (fieldDef == null) {
            // maybe it's a metadata
            fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                    .getDefinition(0, propertyName);
        }

        return fieldDef;
    }
    
    public static String getFieldNameForSearchEngine(String propertyName,
            boolean isMetadata, List<String> containerDefinitionNames,
            ProcessingContext jParams, int type) throws JahiaException {
        String fieldName = null;
        if (propertyName != null && propertyName.length() > 0) {
            if (FilterCreator.CONTENT_DEFINITION_NAME.equals(propertyName)) {
                fieldName = JahiaSearchConstant.DEFINITION_NAME;
            } else if (FilterCreator.PAGE_PATH.equals(propertyName)) {
                fieldName = JahiaSearchConstant.METADATA_PAGE_PATH;
            } else if (JahiaQueryObjectModelConstants.CATEGORY_LINKS
                    .equals(propertyName)) {
                fieldName = JahiaSearchConstant.CATEGORY_ID;
            } else if (isMetadata){                
                fieldName = JahiaSearchConstant.METADATA_PREFIX + propertyName.toLowerCase();                
            } else {
                JahiaFieldDefinition fieldDef = getFieldDefinitionForPropertyName(
                        propertyName, containerDefinitionNames, jParams);
                fieldName = propertyName.toLowerCase();
                if (fieldDef != null && fieldDef.getCtnType() != null) {
                    String prefix = JahiaSearchConstant.CONTAINER_FIELD_PREFIX;                    
                    if (fieldDef.getIsMetadata()) {
                        prefix = JahiaSearchConstant.METADATA_PREFIX;
                    } else {
                        fieldName = fieldDef.getCtnType().replaceAll("[ :]",
                                "_").toLowerCase();
                        if (type > 0
                                && fieldDef.getPropertyDefinition() != null) {
                            ExtendedPropertyDefinition propDef = fieldDef
                                    .getPropertyDefinition();
                            if (type == SORTING_TYPE && propDef.isSortable()) {
                                prefix = JahiaSearchConstant.CONTAINER_FIELD_SORT_PREFIX;
                            } else if (type == FACETING_TYPE
                                    && propDef.isFacetable()) {
                                prefix = JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX;
                            }
                        }
                    }
                    fieldName = prefix + fieldName;                    
                }
            }
        }
        return fieldName;
    }
}
