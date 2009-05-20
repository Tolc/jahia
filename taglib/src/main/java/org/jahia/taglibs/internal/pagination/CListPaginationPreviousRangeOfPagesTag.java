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
package org.jahia.taglibs.internal.pagination;

import javax.servlet.jsp.JspException;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.internal.uicomponents.AbstractButtonTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * <p>Title: This tag generates a previous range of page step button for scrollable container lists</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author NK
 * @version 1.0
 *
 * @jsp:tag name="cListPaginationPreviousRangeOfPages" body-content="empty"
 * description="Displays a button (full link) to jump to the previous range of pagination window.
 *
 * <p><attriInfo>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
 *
 * </attriInfo>"
 *
 * */

@SuppressWarnings("serial")
public class CListPaginationPreviousRangeOfPagesTag extends AbstractButtonTag {

    private JahiaContainerList containerList = null;
    private JahiaContainerListPagination cPagination = null;
    private CListPaginationTag cPaginationTag = null;
    private ContainerListTag parent = null;

    private String title = "&lt;&lt;Previous";
    private String style = "";
    private String method = "get";
    private String formName = "";

    /**
        * @jsp:attribute name="title" required="false" rtexprvalue="true"
        * description="The title of the button.
        *
        * <p><attriInfo>Defaulted to \"&lt;&lt;Previous\".
        * </attriInfo>"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
      *
      * @jsp:attribute name="style" required="false" rtexprvalue="true"
      * description="the CSS class to use for the style of the button.
      * <p><attriInfo>Defaulted to no CSS style at all.
      *  </attriInfo>"
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
        * @jsp:attribute name="method" required="false" rtexprvalue="true"
        * description="If you want to implement a Post ( form submission) request version, you need to set this attribute
        * to \"post\".
        * <p><attriInfo>By default, the method is \"get\".
        * </attriInfo>"
     */
    public void setMethod(String method) {
        if ( method != null )
        {
            this.method = method;
        }
    }

    /**
        * @jsp:attribute name="formName" required="false" rtexprvalue="true"
        * description="the form name needed to generate the form submit Javascript code.
        *
        * <p><attriInfo>The value must refers to the corrent enclosing Jahia Page Form Name.
        * It is mandatory when the method attribute is set to \"post\".
        * </attriInfo>"
     */
    public void setFormName(String formName) {
        if ( formName != null )
        {
            this.formName = formName.trim();
        }
    }

    public String getTitle() {
        return this.title;
    }

    public String getStyle() {
        return this.style;
    }

    public String getMethod() {
        return this.method;
    }

    public String getFormName() {
        return this.formName;
    }

    public boolean testRights (JahiaData jData) {

        cPaginationTag = (CListPaginationTag) findAncestorWithClass(this,CListPaginationTag.class);
        if ( cPaginationTag == null ){
            return false;
        }
        if ( cPaginationTag.getNbStepPerPage()<=0 ){
            return false;
        }
        parent = (ContainerListTag) findAncestorWithClass(this,ContainerListTag.class);
        if (parent != null) {
            this.containerList = parent.getContainerList();
            if ( this.containerList != null ){
                this.cPagination = this.containerList.getCtnListPagination();
                if ( this.cPagination != null && this.cPagination.isValid())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String getLauncher(JahiaData jData) throws JahiaException {

        String value = null;
        //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Started ---------------------------------------");
        //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Page Number :" + cPaginationTag.getPageNumber());
        //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Stop Page :" + cPaginationTag.getStopPageIndex());
        //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Total Pages :" + this.cPagination.getNbPages());

        if ( cPaginationTag.getStartPageIndex()>1 ){

            //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Yes starting generate next[X] button");

            value = jData.gui().drawContainerListPreviousWindowPageURL( containerList, cPagination.getCurrentPageIndex()-cPaginationTag.getStartPageIndex()+1, this.cPagination.getWindowSize(), this.method.equals("post"), parent.getId() );
            //JahiaConsole.println("CListPaginationPreviousRangeOfPagesTag.getLauncher","Values is :" + value);

            if ( value != null && this.method.equals("post") )
            {
                StringBuffer buff = new StringBuffer("javascript:changePage(document.");
                buff.append(getFormName());
                buff.append(",document.");
                buff.append(getFormName());
                buff.append(".").append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER);
                buff.append(parent.getId() != null ? parent.getId() + "_" : "");
                buff.append(containerList.getDefinition().getName());
                buff.append(",'");
                buff.append(value);
                buff.append("');");
                value = buff.toString();
            }
        }
        return value;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        containerList = null;
        parent = null;
        cPagination = null;
        cPaginationTag = null;

        title = "Next&gt;&gt;";
        style = "";
        method = "get";
        formName = "";
        return EVAL_PAGE;
    }

}
