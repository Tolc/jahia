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
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaSitePropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * identifier field
     */
    private String name;

    /**
     * full constructor
     */
    public JahiaSitePropPK(Integer idJahiaSite, String nameJahiaSiteProp) {
        this.id = idJahiaSite;
        this.name = nameJahiaSiteProp;
    }

    /**
     * default constructor
     */
    public JahiaSitePropPK() {
    }

    /**
     * @hibernate.property column="id_jahia_site"
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        updated();
        this.id = id;
    }

    /**
     * @hibernate.property column="name_jahia_site_prop"
     * length="255"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("id="+getId())
                .append("name="+getName())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSitePropPK castOther = (JahiaSitePropPK) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getName(), castOther.getName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getName())
                .toHashCode();
    }

}
