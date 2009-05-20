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
package org.jahia.data.fields;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ObjectKey;
import org.jahia.content.PropertiesInterface;
import org.jahia.data.containers.ContainerFacadeInterface;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.TextHtml;
import org.jahia.utils.textdiff.HunkTextDiffVisitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedList;

import name.fraser.neil.plaintext.DiffMatchPatch;

public abstract class JahiaField implements Cloneable, Serializable,
        ACLResourceInterface, PropertiesInterface, Comparable<JahiaField> {

    private static final long serialVersionUID = -4633138077897092761L;

    public static final String MULTIPLE_VALUES_SEP = "$$$";
    
    public static final String[] EMPTY_STRING_ARRAY = new String[]{""};
    
    private static Logger logger = Logger.getLogger(JahiaField.class);

    
    
    protected int ID;
    protected int jahiaID;
    protected int pageID;
    protected int ctnid;
    protected int fieldDefID;
    protected int fieldType;
    protected int connectType;
    protected String fieldValue = "";
    protected String fieldRawValue = "";
    protected int rank;
    protected int aclID;
    protected int versionID;
    protected int workflowState;
    protected String languageCode;
    protected Object objectItem;     // can be used to pass any object
    protected Properties properties;
    private boolean propertiesLoaded = false;
    protected boolean hasChanged = true;

    protected boolean isMetadata = false;

    protected boolean forComparisonOnly = false;

    protected ObjectKey metadataOwnerObjectKey;

    // an external order ( i.e: when used as metadata )
    // used as comparable
    private int order;

    public static final String NULL_STRING_MARKER = "<empty>";

    protected JahiaField(Integer ID,
                         Integer jahiaID,
                         Integer pageID,
                         Integer ctnid,
                         Integer fieldDefID,
                         Integer fieldType,
                         Integer connectType,
                         String fieldValue,
                         Integer rank,
                         Integer aclID,
                         Integer versionID,
                         Integer workflowState,
                         String languageCode) {
        setID(ID.intValue());
        this.jahiaID = jahiaID.intValue();
        this.pageID = pageID.intValue();
        this.fieldDefID = fieldDefID.intValue();
        this.ctnid = ctnid.intValue();
        this.fieldType = fieldType.intValue();
        this.connectType = connectType.intValue();
        this.fieldValue = fieldValue;
        this.fieldRawValue = fieldValue;
        this.rank = rank.intValue();
        this.aclID = aclID.intValue();
        this.versionID = versionID.intValue();
        this.workflowState = workflowState.intValue();
        this.languageCode = languageCode;
        this.properties = null;
    }

    /**
     * returns the field id.
     * EV    31.10.2000
     */
    public int getID() {
        return ID;
    }

    public int getJahiaID() {
        return jahiaID;
    }

    /**
     * returns the id of the site containing the field.
     */
    public int getSiteID() {
        return jahiaID;
    } // FIXME_MULTISITE Hollis humm jahiaID or siteID ?

    /**
     * returns the id of the page containing the field.
     */
    public int getPageID() {
        return pageID;
    }

    public int getFieldDefID() {
        return fieldDefID;
    }

    public int getctnid() {
        return ctnid;
    }

    /**
     * returns the field type.
     */
    public int getType() {
        return fieldType;
    }

    /**
     * returns the type of connection (external or internal).
     */
    public int getConnectType() {
        return connectType;
    }

    /**
     * returns the field rank.
     */
    public int getRank() {
        return rank;
    }

    public final int getAclID() {
        if (getID() > 0) {
            ContentField field = getContentField();
            if (field != null) {
                return field.getAclID();
            }
        }
        return aclID;
    }

    public int getVersionID() {
        return versionID;
    }

    public int getWorkflowState() {
        return workflowState;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public final JahiaBaseACL getACL() {
        JahiaBaseACL acl = null;
        try {
            acl = JahiaBaseACL.getACL(getAclID());
//            acl = new JahiaBaseACL(getAclID());
        } catch (Exception t) {
            logger.warn("Error getting ACL-ID", t);
        }
        return acl;
    }

    /**
     * returns the field object (for non-text fields).
     *
     * @return Object
     */
    public Object getObject() {
        return objectItem;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setAclID(int aclID) {
        this.aclID = aclID;
    }

    public void setType(int fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Sets the value. Usually we make a call to FormDataManager.decode before
     * setting this value.
     *
     * @param fieldValue
     */
    public void setValue(String fieldValue) {
        if ((fieldValue != null && !fieldValue.equals(this.fieldValue)) ||
                (fieldValue == null && this.fieldValue != null))
            registerChange();
        this.fieldValue = fieldValue;
    }

    public void setctnid(int ctnid) {
        if (ctnid != this.ctnid)
            registerChange();
        this.ctnid = ctnid;
    }

    public void setFieldDefID(int fieldDefID) {
        if (fieldDefID != this.fieldDefID)
            registerChange();
        this.fieldDefID = fieldDefID;
    }

    public void setConnectType(int connectType) {
        if (connectType != this.connectType)
            registerChange();
        this.connectType = connectType;
    }

    public void setObject(Object objectItem) {
        if ((objectItem != null && !objectItem.equals(this.objectItem)) ||
                (objectItem == null && this.objectItem != null))
            registerChange();
        this.objectItem = objectItem;
    }

    // end accessor methods
    /**
     * returns the field value (only for text fields).
     * @return String
     */
    public String getValue() {
        return fieldValue;
    }

    /**
     * Returns a string[] tokens of values separated by separator JahiaField.MULTIPLE_VALUES_SEP
     *
     */
    public String[]  getValues() {
        return JahiaTools.getTokens(getValue(), JahiaField.MULTIPLE_VALUES_SEP);
    }

    /**
     * returns the value of the raw.
     *
     */
    public String getRawValue() {
        return fieldRawValue;
    }

    /**
     * Sets the internal raw value. This means that the value we set has never
     * been processed.
     *
     */
    public void setRawValue(String fieldRawValue) {
        if ((fieldRawValue != null && !fieldRawValue.equals(this.fieldRawValue)) ||
                (fieldRawValue == null && this.fieldRawValue != null))
            registerChange();
        this.fieldRawValue = fieldRawValue;
    }

    public boolean getIsMetadata() {
        return isMetadata;
    }

    public void setIsMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    public ObjectKey getMetadataOwnerObjectKey() {
        return metadataOwnerObjectKey;
    }

    public void setMetadataOwnerObjectKey(ObjectKey metadataOwnerObjectKey) {
        this.metadataOwnerObjectKey = metadataOwnerObjectKey;
    }

    public Properties getProperties() {
        if (propertiesLoaded) {
            return properties;
        } else {
            return null;
        }
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
        propertiesLoaded = true;
    } // end Properties methods

    // abstract methods
    // YG 17.07.2001
    // Must be implemented into inherited class
    // because they depends of field types

    public void load(int loadFlag, ProcessingContext jParams) throws JahiaException {
        load(loadFlag, jParams, (jParams != null) ? jParams.getEntryLoadRequest() : null);
    }

    /**
     * load the information specific to a type
     * set the Object parameter with a JahiaPage
     * or a file, for example.
     */
    public abstract void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException;

    /**
     * save the information specific to a type
     * save the Object in relation with the field
     * a JahiaPage or a file, for example.
     */
    public abstract boolean save(ProcessingContext jParams)
            throws JahiaException;

    /**
     * return the name of the engine
     * which correspond to the field type.
     */
    public abstract String getEngineName();

    /**
     * return the information
     * to display for the ranking.
     * the filename or the page name for example.
     */
    public abstract String getFieldContent4Ranking()
            throws JahiaException;

    /**
     * return the "off" icon name
     */
    public abstract String getIconNameOff();

    /**
     * return the "on" icon name
     */
    public abstract String getIconNameOn();

    /**
     * getDefinition
     */
    public JahiaFieldDefinition getDefinition()
            throws JahiaException {
        JahiaFieldDefinition theDef = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(fieldDefID);
        if (theDef != null) {
            return theDef;
        } else {
            String msg = "JahiaField definition " + fieldDefID + " not found in definitions registry !";
            throw new JahiaException("Synchronisation error in database",
                    msg, JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }
    } // end getDefinition


    //-------------------------------------------------------------------------
    /**
     * Check if the user has read access for the specified field. Read access means
     * displaying field data.
     *
     * @param    user    Reference to the user.
     *
     * @return Return true if the user has read access for the specified field,
     *           or false in any other case.
     */
    public final boolean checkReadAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.READ_RIGHTS);
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has Write access for the specified field. Write access means
     * updating field data.
     *
     * @param    user    Reference to the user.
     *
     * @return Return true if the user has write access for the specified field,
     *           or false in any other case.
     */
    public final boolean checkWriteAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.WRITE_RIGHTS);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the user has Admin access for the specified field. Admin access means
     * setting rights on data.
     *
     * @param    user    Reference to the user.
     *
     * @return Return true if the user has admin access for the specified field,
     *           or false in any other case.
     */
    public final boolean checkAdminAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.ADMIN_RIGHTS);
    }

    // Note : There are no Admin rights on fields.

    //-------------------------------------------------------------------------
    private boolean checkAccess(JahiaUser user, int permission) {
        if (user == null) {
            return false;
        }

        boolean result = false;
        try {
            // Try to instanciate the ACL.
            if (aclID > 0) {
                JahiaBaseACL fieldACL = getACL();

                if (fieldACL != null) {
                    // Test the access rights
                    result = fieldACL.getPermission(user, permission);
                }
            }
        } catch (JahiaException ex) {
            logger.error("JahiaException caught in checkAccess.", ex);
        }

        return result;
    }


    /**
     * Generate an html anchor composed by the fieldID
     *
     */
    public String getAnchor() {

        StringBuffer anchor = new StringBuffer("<A NAME=");
        anchor.append(this.getID());
        anchor.append("></A>");

        return anchor.toString();
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public abstract boolean isShared();

    private void registerChange() {
        this.hasChanged = true;
    }

    /**
     * Copy the internal value of current language to another language.
     * Must be implemented by conctrete field for specific implementation.
     *
     * @param aField A same field in another language
     */
    public abstract void copyValueInAnotherLanguage(JahiaField aField, ProcessingContext jParams)
            throws JahiaException;

    /**
     * Copy the internal value of this field to the passed field.
     * Should be implemented by concrete field for specific implementation.
     * <p/>
     * Can be used to restore versioned value to a staging field.
     *
     * @param aField A same field in another language
     */
    public void copyValueToAnotherField(JahiaField aField, ProcessingContext jParams)
            throws JahiaException {
        if (aField != null) {
            aField.setValue(this.getValue());
            aField.setRawValue(this.getValue());
            aField.setObject(this.getObject());
        }
    }

    /**
     * Returns an Hashmap of language_code/values used by search index engine
     *
     */
    public Map<String, String[]> getValuesForSearch() throws JahiaException {

        String lang = this.getLanguageCode();
        if (this.isShared()) {
            lang = ContentField.SHARED_LANGUAGE;
        }

        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(lang, getValuesForSearch(lang, null, true));
        return map;
    }

    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context) throws JahiaException {
        return getValuesForSearch(languageCode, context, true);
    }
    
    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {

        String[] values = this.getValues();
        if (values == null || values.length == 0) {
            values = EMPTY_STRING_ARRAY;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = TextHtml.html2text(values[i]);
        }
        return values;
    }    
    
    public void setVersionID(int versionID) {
        this.versionID = versionID;
    }

    public void setWorkflowState(int workflowState) {
        this.workflowState = workflowState;
    }

    public void setlanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * Try to get the content object from the Jahia field
     *
     * @return The content field if success, otherwise null.
     */
    public ContentField getContentField() {
        ContentField contentField = null;
        try {
            contentField = ContentField.getField(ID);
        } catch (JahiaException je) {
            logger.debug(je);
        }
        return contentField;
    }


    /**
     * Return the value with highlighted differences
     *
     * @param jParams
     */
    public String getHighLightDiffValue(ProcessingContext jParams) {
        return getHighLightDiffValue(jParams.getDiffVersionID(), jParams);
    }

    /**
     * Return the value with highlighted differences
     *
     * @param jParams
     */
    public String getHighLightDiffValue(int diffVersionID, ProcessingContext jParams) {

        if (diffVersionID == 0) {
            return this.getValue();
        }

        String oldValue = this.getValue();
        if (oldValue==null){
            oldValue = "";
        }
        String newValue = "";
        String mergedValue = "";

        try {
            EntryLoadRequest loadVersion =
                    EntryLoadRequest.getEntryLoadRequest(diffVersionID,
                            this.languageCode);

            JahiaField jahiaField = ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(this.getID(), LoadFlags.ALL,
                            jParams, loadVersion);

            int newValueWorkflowState = this.getWorkflowState();
            if (jahiaField != null) {
                newValue = jahiaField.getValue();
                newValueWorkflowState = jahiaField.getWorkflowState();
            }

            // Highlight text diff
            DiffMatchPatch hunkTextDiffV = new DiffMatchPatch();
            LinkedList<DiffMatchPatch.Diff> diffs;
            if ( this.isForComparisonOnly() ){
                // does not exists
                return HunkTextDiffVisitor.getDeletedText(oldValue);
            } else if ( this.getVersionID() == -1 && newValueWorkflowState==1){
                // currently marked for delete compared with active
                return HunkTextDiffVisitor.getDeletedText(oldValue);
            } else if (this.getWorkflowState() < newValueWorkflowState) {
                diffs = hunkTextDiffV.diff_main(oldValue, newValue);
            } else {
                diffs = hunkTextDiffV.diff_main(newValue, oldValue);
            }
            hunkTextDiffV.diff_cleanupSemantic(diffs);
            mergedValue = hunkTextDiffV.diff_prettyHtml(diffs);
        } catch (Exception t) {
            logger.warn("Error getting highlight diff value", t);
        }
        return mergedValue;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public int compareTo(JahiaField f) {
        int result = 0;
        if (f != null) {
            result = new Integer(this.getOrder())
                    .compareTo(new Integer(f.getOrder()));
        }
        return result;
    }

    protected boolean isMandatory() throws JahiaException {
        return getDefinition().getItemDefinition().isMandatory();
    }

    public ValidationError validate(
            ContainerFacadeInterface jahiaContentContainerFacade,
            EngineLanguageHelper elh,
            ProcessingContext jParams) throws JahiaException {
        return isMandatory() && StringUtils.isBlank(this.getValue()) ? new ValidationError(
                this, "Value required")
                : null;
    }

    public boolean isForComparisonOnly() {
        return forComparisonOnly;
    }

    public void setForComparisonOnly(boolean forComparisonOnly) {
        this.forComparisonOnly = forComparisonOnly;
    }

}